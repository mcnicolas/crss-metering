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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableMap.of;
import static com.pemc.crss.metering.constants.BcqUpdateType.MANUAL_OVERRIDE;
import static com.pemc.crss.metering.constants.BcqValidationError.*;
import static com.pemc.crss.metering.utils.BcqDateUtils.formatDate;
import static com.pemc.crss.metering.utils.DateTimeUtils.now;
import static com.pemc.crss.metering.validator.bcq.helper.BcqValidationHelperUtils.getFormattedSellingMtnAndBillingIdPair;
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
    private final List<BcqSpecialEventParticipant> eventParticipants = new ArrayList<>();

    public Validation<List<BcqHeader>> validSpecialEventUpload(String sellingParticipant) {
        return participantsWithTradingDateExist()
                .and(noManualOverridden(sellingParticipant))
                .and(deadlineDateNotPassed());
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
            eventParticipants.addAll(headerParticipants);
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

    private HeaderListValidation noManualOverridden(String sellingParticipant) {
        HeaderListValidation validation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            Date tradingDate = headerList.get(0).getTradingDate();
            List<BcqHeader> currentHeaderList = getCurrentHeaderList(sellingParticipant, tradingDate);
            List<BcqHeader> manualOverriddenHeaderList = currentHeaderList.stream()
                    .filter(header -> header.getUpdatedVia() != null && header.getUpdatedVia() == MANUAL_OVERRIDE)
                    .collect(toList());
            if (manualOverriddenHeaderList.size() > 0) {
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(OVERRIDDEN_ENTRIES,
                        asList(formatDate(tradingDate),
                                getFormattedSellingMtnAndBillingIdPair(manualOverriddenHeaderList)));
                validation.setErrorMessage(errorMessage);
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

    private HeaderListValidation deadlineDateNotPassed() {
        HeaderListValidation validation = emptyInst();
        Predicate<List<BcqHeader>> predicate = headerList -> {
            Date tradingDate = headerList.get(0).getTradingDate();
            StringJoiner eventParticipantsWithDeadlineDatePassed = new StringJoiner(", ");
            for (BcqSpecialEventParticipant eventParticipant : eventParticipants.stream().distinct().collect(toList())) {
                Date deadlineDate = bcqService.findEventDeadlineDateByTradingDateAndParticipant(tradingDate,
                        eventParticipant.getShortName());

                if (now().getTime() > deadlineDate.getTime()) {
                    String participantName = eventParticipant.getParticipantName() + " ("
                            + eventParticipant.getShortName() + ")";
                    eventParticipantsWithDeadlineDatePassed.add(participantName);
                }
            }
            if (eventParticipantsWithDeadlineDatePassed.length() > 0) {
                BcqValidationErrorMessage errorMessage = new BcqValidationErrorMessage(DEADLINE_DATE_PASSED,
                        asList(formatDate(tradingDate), eventParticipantsWithDeadlineDatePassed.toString()));
                validation.setErrorMessage(errorMessage);
                return false;
            }
            return true;
        };
        validation.setPredicate(predicate);
        return validation;
    }

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/
    private List<BcqHeader> getCurrentHeaderList(String sellingParticipant, Date tradingDate) {
        return bcqService.findAllHeaders(of(
                "sellingParticipant", sellingParticipant,
                "tradingDate", formatDate(tradingDate)
        ));
    }

}
