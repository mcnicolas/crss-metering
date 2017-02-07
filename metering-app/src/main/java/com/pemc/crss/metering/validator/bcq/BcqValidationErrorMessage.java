package com.pemc.crss.metering.validator.bcq;

import com.pemc.crss.metering.constants.BcqValidationError;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

@Data
@AllArgsConstructor
public class BcqValidationErrorMessage {

    private BcqValidationError validationError;
    private List<String> messageArgs;

    public BcqValidationErrorMessage(BcqValidationError validationError) {
        this.validationError = validationError;
        this.messageArgs = new ArrayList<>();
    }

    public String getFormattedMessage() {
        if (messageArgs != null && messageArgs.size() > 0) {
            return format(validationError.getErrorMessage(), messageArgs.toArray());
        }
        return validationError.getErrorMessage();
    }

}
