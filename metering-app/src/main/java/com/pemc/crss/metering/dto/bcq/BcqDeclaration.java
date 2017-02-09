package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

import static com.pemc.crss.metering.validator.bcq.BcqValidationResult.accepted;

@Data
@ToString
@NoArgsConstructor
public class BcqDeclaration {

    private BcqUploadFileDetails uploadFileDetails;
    private ParticipantSellerDetails sellerDetails;
    private List<BcqHeaderDetails> headerDetailsList;
    private BcqValidationResult validationResult;
    private boolean isResubmission;
    private boolean isSpecialEvent;

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
