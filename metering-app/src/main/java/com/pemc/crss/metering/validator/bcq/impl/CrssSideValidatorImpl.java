package com.pemc.crss.metering.validator.bcq.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pemc.crss.metering.dto.bcq.*;
import com.pemc.crss.metering.resource.template.ResourceTemplate;
import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import com.pemc.crss.metering.validator.bcq.CrssSideValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.pemc.crss.metering.constants.BcqStatus.FOR_CONFIRMATION;
import static com.pemc.crss.metering.constants.BcqStatus.FOR_NULLIFICATION;
import static com.pemc.crss.metering.constants.ValidationStatus.ACCEPTED;
import static java.util.stream.Collectors.toList;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CrssSideValidatorImpl implements CrssSideValidator {

    private final ResourceTemplate resourceTemplate;
    private static final String VALIDATE_URL = "/reg/bcq/validate";
    private static final String SETTLEMENT_VALIDATE_URL = "/reg/bcq/settlement/validate";

    @Override
    public BcqValidationResult<List<BcqHeader>> validate(List<BcqHeader> headerList,
                                                         ParticipantSellerDetails sellerDetails) {
        log.debug("Start validation in crss side");
        BcqValidationResult crssSideResult = validateInCrssSide(headerList);
        BcqValidationResult<List<BcqHeader>> result;

        List<ParticipantBuyerDetails> buyerDetailsList = new ObjectMapper().convertValue(
                crssSideResult.getProcessedObject(), new TypeReference<List<ParticipantBuyerDetails>>() {});

        if (crssSideResult.getStatus() == ACCEPTED) {
            headerList.forEach(header -> addParticipantDetailsToHeader(header, sellerDetails, buyerDetailsList));

            result = new BcqValidationResult<List<BcqHeader>>().withProcessedObject(headerList);
        } else {
            result = new BcqValidationResult<>(crssSideResult.getErrorMessage());
        }
        log.debug("Finish validation in crss side, Result: {}", result);
        return result;
    }

    @Override
    public BcqValidationResult<List<BcqHeader>> validateBySettlement(List<BcqHeader> headerList,
                                                                     ParticipantSellerDetails sellerDetails) {

        log.debug("Start validation in crss side by settlement");
        BcqValidationResult crssSideResult = validateInCrssSideBySettlement(sellerDetails, headerList);
        BcqValidationResult<List<BcqHeader>> result;

        List<ParticipantBuyerDetails> buyerDetailsList = new ObjectMapper().convertValue(
                crssSideResult.getProcessedObject(), new TypeReference<List<ParticipantBuyerDetails>>() {});

        if (crssSideResult.getStatus() == ACCEPTED) {
            headerList.forEach(header -> addParticipantDetailsToHeader(header, sellerDetails, buyerDetailsList));

            result = new BcqValidationResult<List<BcqHeader>>().withProcessedObject(headerList);
        } else {
            result = new BcqValidationResult<>(crssSideResult.getErrorMessage());
        }
        log.debug("Finish validation in crss side by settlement, Result: {}", result);
        return result;
    }

    private BcqValidationResult validateInCrssSide(List<BcqHeader> headerList) {
        return resourceTemplate.post(VALIDATE_URL, BcqValidationResult.class, getUniqueItems(headerList));

    }

    private BcqValidationResult validateInCrssSideBySettlement(
            ParticipantSellerDetails sellerDetails,
            List<BcqHeader> headerList) {

        return resourceTemplate.post(SETTLEMENT_VALIDATE_URL, BcqValidationResult.class,
                new SellerWithItems(sellerDetails, getUniqueItems(headerList)));
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
                    item.setTradingParticipantShortName(header.getBuyingParticipantShortName());
                    item.setReferenceMtns(referenceMtns);
                    item.setTradingDate(header.getTradingDate());
                    item.setBuyerMtn(header.getBuyerMtn());
                    return item;
                })
                .distinct()
                .collect(toList());
    }

    private BcqHeader addParticipantDetailsToHeader(BcqHeader header, ParticipantSellerDetails sellerDetails,
                                                    List<ParticipantBuyerDetails> buyerDetailsList) {

        ParticipantBuyerDetails buyerDetails = buyerDetailsList.stream()
                .filter(buyer -> buyer.getShortName().equalsIgnoreCase(header.getBuyingParticipantShortName()))
                .collect(toList()).get(0);

        header.setSellingParticipantName(sellerDetails.getName());
        header.setSellingParticipantShortName(sellerDetails.getShortName());
        header.setBuyingParticipantName(buyerDetails.getName());
        header.setStatus(buyerDetails.isBcqConfirmation() ? FOR_NULLIFICATION : FOR_CONFIRMATION);
        return header;
    }

}
