package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import lombok.Data;
import lombok.ToString;

import java.util.List;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;

@Data
@ToString
public class BcqDeclaration {

    private ParticipantSellerDetails sellerDetails;
    private List<BcqHeaderDetails> headerDetailsList;
    private BcqValidationResult validationResult;

    public BcqDeclaration(ParticipantSellerDetails sellerDetails, List<BcqHeaderDetails> headerDetailsList) {
        this.sellerDetails = sellerDetails;
        this.headerDetailsList = headerDetailsList;
        this.validationResult = accepted();
    }

    public BcqDeclaration(ParticipantSellerDetails sellerDetails) {
        this.sellerDetails = sellerDetails;
    }

    public BcqDeclaration withHeaderDetailsList(List<BcqHeaderDetails> headerDetailsList) {
        this.headerDetailsList = headerDetailsList;
        validationResult = accepted();
        return this;
    }

    public BcqDeclaration withValidationResult(BcqValidationResult validationResult) {
        this.validationResult = validationResult;
        headerDetailsList = null;
        return this;
    }

}
