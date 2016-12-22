package com.pemc.crss.metering.validator.bcq.handler;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.resource.template.ResourceTemplate;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.CsvValidator;
import com.pemc.crss.metering.validator.bcq.HeaderListValidator;
import com.pemc.crss.metering.validator.bcq.RedeclarationValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.pemc.crss.metering.constants.BcqInterval.fromDescription;
import static com.pemc.crss.metering.constants.ValidationStatus.REJECTED;
import static com.pemc.crss.metering.validator.bcq.helper.BcqPopulator.populate;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
public class BcqValidationHandler {

    private static final String SELLER_URL = "/reg/bcq/seller";
    private static final String VALIDATE_URL = "/reg/bcq/validate";
    private static final int INTERVAL_ROW_INDEX = 0;
    private static final int INTERVAL_COLUMN_INDEX = 1;

    private final CsvValidator csvValidator;
    private final HeaderListValidator headerListValidator;
    private final RedeclarationValidator redeclarationValidator;
    private final ResourceTemplate resourceTemplate;

    @Autowired
    public BcqValidationHandler(CsvValidator csvValidator,
                                HeaderListValidator headerListValidator,
                                RedeclarationValidator redeclarationValidator,
                                ResourceTemplate resourceTemplate) {

        this.csvValidator = csvValidator;
        this.headerListValidator = headerListValidator;
        this.redeclarationValidator = redeclarationValidator;
        this.resourceTemplate = resourceTemplate;
    }

    public BcqDeclaration processAndValidate(List<List<String>> csv) {
        ParticipantSellerDetails sellerDetails = getSellerDetails();
        BcqDeclaration declaration = new BcqDeclaration(sellerDetails);
        BcqValidationResult validationResult = csvValidator.validate(csv);
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        BcqInterval interval = fromDescription(csv.get(INTERVAL_ROW_INDEX).get(INTERVAL_COLUMN_INDEX));
        List<BcqHeader> headerList = populate(csv, interval);
        validationResult = headerListValidator.validate(headerList, interval);
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        BcqParticipantDetails participantDetails = getAndValidate(getUniqueItems(headerList));
        if (participantDetails.getValidationResult().getStatus() == REJECTED) {
            return declaration.withValidationResult(participantDetails.getValidationResult());
        }

        headerList = addParticipantDetailsToHeaderList(headerList, sellerDetails, participantDetails);

        validationResult = redeclarationValidator.validate(headerList,
                sellerDetails.getShortName(),
                headerList.get(0).getTradingDate());
        if (validationResult.getStatus() == REJECTED) {
            return declaration.withValidationResult(validationResult);
        }

        List<BcqHeaderDetails> headerDetailsList = new ArrayList<>();
        headerList.forEach(header -> headerDetailsList.add(new BcqHeaderDetails(header)));

        return declaration.withHeaderDetailsList(headerDetailsList);
    }

    private List<BcqItem> getUniqueItems(List<BcqHeader> headerList) {
        return headerList.stream().
                map(header -> {
                    List<String> referenceMtns = header.getDataList().stream()
                            .map(BcqData::getReferenceMtn)
                            .distinct()
                            .collect(toList());
                    BcqItem item = new BcqItem();
                    item.setSellingMtn(header.getSellingMtn());
                    item.setBillingId(header.getBillingId());
                    item.setReferenceMtns(referenceMtns);
                    return item;
                })
                .distinct()
                .collect(toList());
    }

    private List<BcqHeader> addParticipantDetailsToHeaderList(List<BcqHeader> headerList,
                                                              ParticipantSellerDetails sellerDetails,
                                                              BcqParticipantDetails participantDetails) {

        return headerList.stream().map(header -> {
            header.setSellingParticipantName(sellerDetails.getName());
            header.setBuyingParticipantShortName(sellerDetails.getShortName());
            ParticipantBuyerDetails buyerDetails =
                    participantDetails.getBuyerDetailsList().stream()
                            .filter(buyer -> buyer.getBillingId().equals(header.getBillingId()))
                            .collect(toList())
                            .get(0);
            header.setBuyingParticipantName(buyerDetails.getName());
            header.setBuyingParticipantShortName(buyerDetails.getShortName());
            return header;
        }).collect(toList());
    }

    private ParticipantSellerDetails getSellerDetails() {
        return resourceTemplate.get(SELLER_URL, ParticipantSellerDetails.class);
    }

    private BcqParticipantDetails getAndValidate(List<BcqItem> itemList) {
        return resourceTemplate.post(VALIDATE_URL, BcqParticipantDetails.class, itemList);
    }

}