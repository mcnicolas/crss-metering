package com.pemc.crss.metering.dto.bcq;

import com.pemc.crss.metering.validator.bcq.BcqValidationResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

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
    private String user;
    public BcqDeclaration(ParticipantSellerDetails sellerDetails) {
        this.sellerDetails = sellerDetails;
    }

    public BcqDeclaration withHeaderDetailsList(List<BcqHeaderDetails> headerDetailsList) {
        this.headerDetailsList = headerDetailsList;
        validationResult = new BcqValidationResult();
        return this;
    }

    public BcqDeclaration withValidationResult(BcqValidationResult<List<BcqHeader>> validationResult) {
        this.validationResult = validationResult;
        return this;
    }

}
