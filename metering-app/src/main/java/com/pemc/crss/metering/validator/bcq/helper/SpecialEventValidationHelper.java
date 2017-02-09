package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.dto.bcq.BcqHeader;
import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEventParticipant;
import com.pemc.crss.metering.service.BcqService;
import com.pemc.crss.metering.validator.bcq.BcqValidationErrorMessage;
import com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation;
import com.pemc.crss.metering.validator.bcq.validation.Validation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.function.Predicate;

import static com.pemc.crss.metering.constants.BcqValidationError.NO_SPECIAL_EVENT_FOUND;
import static com.pemc.crss.metering.constants.BcqValidationError.PARTICIPANTS_NOT_PRESENT_IN_SPECIAL_EVENT;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.validator.bcq.validation.HeaderListValidation.emptyInst;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SpecialEventValidationHelper {

    private final BcqService bcqService;

    public Validation<List<BcqHeader>> validSpecialEventUpload() {
        return participantsWithTradingDateExist();
    }

    private HeaderListValidation participantsWithTradingDateExist() {
        HeaderListValidation validation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            Date tradingDate = headerList.get(0).getTradingDate();
            List<BcqSpecialEventParticipant> participants = bcqService.findEventParticipantsByTradingDate(tradingDate);
            List<BcqSpecialEventParticipant> headerParticipants = headerList.stream().map(header -> {
                BcqSpecialEventParticipant eventParticipant = new BcqSpecialEventParticipant();
                eventParticipant.setParticipantName(header.getBuyingParticipantName());
                eventParticipant.setShortName(header.getBuyingParticipantShortName());
                return eventParticipant;
            }).collect(toList());
            if (isEmpty(participants)) {
                validation.setErrorMessage(new BcqValidationErrorMessage(NO_SPECIAL_EVENT_FOUND));
                return false;
            }
            headerParticipants.removeAll(participants);
            if (headerParticipants.size() > 0) {
                String notPresentParticipants = headerParticipants.stream()
                        .map(headerParticipant ->
                                "<b>" + headerParticipant.getParticipantName() + "("
                                        + headerParticipant.getShortName() + ")</b>")
                        .distinct()
                        .collect(joining(", "));
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(
                        PARTICIPANTS_NOT_PRESENT_IN_SPECIAL_EVENT,
                        asList(formatDate(tradingDate), notPresentParticipants));
                validation.setErrorMessage(errorMessage);
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

}
