package com.pemc.crss.metering.validator.bcq.handler;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqDeclaration;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.bcq.BcqHeaderDetails;
import com.pemc.crss.metering.dto.bcq.BcqItem;
import com.pemc.crss.metering.dto.bcq.BcqParticipantDetails;
import com.pemc.crss.metering.dto.bcq.ParticipantSellerDetails;
import com.pemc.crss.metering.resource.template.ResourceTemplate;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.CsvValidator;
import com.pemc.crss.metering.validator.bcq.HeaderListValidator;
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
    private final ResourceTemplate resourceTemplate;

    @Autowired
    public BcqValidationHandler(CsvValidator csvValidator,
                                HeaderListValidator headerListValidator,
                                ResourceTemplate resourceTemplate) {

        this.csvValidator = csvValidator;
        this.headerListValidator = headerListValidator;
        this.resourceTemplate = resourceTemplate;
    }

    public BcqDeclaration processAndValidate(List<List<String>> csv) {
        BcqDeclaration declaration = new BcqDeclaration(getSellerDetails());
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

        List<BcqHeaderDetails> headerDetailsList = new ArrayList<>();
        headerList.forEach(header -> headerDetailsList.add(new BcqHeaderDetails(header)));

        return declaration.withHeaderDetailsList(headerDetailsList);
    }

    private List<BcqItem> getUniqueItems(List<BcqHeader> headerList) {
        List<BcqItem> itemList = headerList.stream().
                map(header -> {
                    List<String> referenceMtns = header.getDataList().stream().
                            map(BcqData::getReferenceMtn).
                            distinct().
                            collect(toList());
                    BcqItem item = new BcqItem();
                    item.setSellingMtn(header.getSellingMtn());
                    item.setBillingId(header.getBillingId());
                    item.setReferenceMtns(referenceMtns);
                    return item;
                }).
                distinct().
                collect(toList());
        log.debug("ITEM LIST: {}", itemList);
        return itemList;
    }

    private ParticipantSellerDetails getSellerDetails() {
        return resourceTemplate.get(SELLER_URL, ParticipantSellerDetails.class);
    }

    private BcqParticipantDetails getAndValidate(List<BcqItem> itemList) {
        return resourceTemplate.post(VALIDATE_URL, BcqParticipantDetails.class, itemList);
    }

}
