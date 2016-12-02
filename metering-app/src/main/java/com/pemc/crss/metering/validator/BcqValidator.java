package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.parser.bcq.BcqInterval;
import com.pemc.crss.metering.parser.bcq.util.BCQParserUtil;
import com.pemc.crss.metering.utils.DateTimeUtils;
import com.pemc.crss.metering.validator.exception.ValidationException;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.pemc.crss.metering.constants.BcqValidationRules.*;
import static com.pemc.crss.metering.parser.bcq.BcqInterval.HOURLY;
import static com.pemc.crss.metering.parser.bcq.BcqInterval.QUARTERLY;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;

public class BcqValidator {//TODO Cleanup

    private final static int VALID_NO_OF_COLUMNS = 5;

    private int intervalConfig;
    private Date validTradingDate;
    private Set<String> uniqueDataSet = new HashSet<>();

    public BcqValidator(int intervalConfig, Date validTradingDate) {
        this.intervalConfig = intervalConfig;
        this.validTradingDate = validTradingDate;
    }

    public List<BcqHeader> getAndValidateBcq(List<List<String>> csv) throws ValidationException {
        validateNotEmpty(csv);
        BcqInterval interval = getAndValidateInterval(csv.get(0));
        validateColumnHeader(csv.get(1));

        List<BcqHeader> headerList = new ArrayList<>();

        for (List<String> line : csv.subList(2, csv.size())) {
            BcqHeader header = getAndValidateBcqHeader(line);
            List<BcqData> dataList = new ArrayList<>();

            if (headerList.contains(header)) {
                header = headerList.get(headerList.indexOf(header));
                dataList = header.getDataList();
            } else {
                headerList.add(header);
                header.setDataList(dataList);
            }

            BcqData data = getAndValidateData(line, interval);
            dataList.add(data);
        }

        for (BcqHeader header : headerList) {
            validateDataSize(header, interval);
            List<BcqData> dataList = header.getDataList();
            dataList.sort((d1, d2) -> d1.getEndTime().compareTo(d2.getEndTime()));
            header.setDataList(getAndValidateDataList(dataList, interval));
        }

        return headerList;
    }

    private void validateNotEmpty(List<List<String>> csv) throws ValidationException {
        if (csv.size() < 3) {
            throw new ValidationException(EMPTY.getErrorMessage());
        }
    }

    private void validateColumnHeader(List<String> line) throws ValidationException {
        if (line.size() != VALID_NO_OF_COLUMNS) {
            throw new ValidationException(INCORRECT_COLUMN_HEADER_COUNT.getErrorMessage());
        }
    }

    private BcqInterval getAndValidateInterval(List<String> line) throws ValidationException {
        String intervalString = line.get(1);
        BcqInterval interval = BcqInterval.fromDescription(intervalString);

        if (interval == null || (interval == BcqInterval.FIVE_MINUTES_PERIOD && intervalConfig != 5)) {
            throw new ValidationException(
                    String.format(INCORRECT_DECLARED_INTERVAL.getErrorMessage(), intervalString));
        }

        return interval;
    }

    private List<BcqData> getAndValidateDataList(List<BcqData> dataList, BcqInterval interval)
            throws ValidationException {

        List<BcqData> finalizedDataList = new ArrayList<>();
        Date previousDate = null;

        for (BcqData data : dataList) {
            validateTimeInterval(data.getEndTime(), previousDate, interval);
            previousDate = data.getEndTime();
            finalizedDataList.addAll(divideDataByInterval(data, interval));
        }

        return finalizedDataList;
    }

    private BcqHeader getAndValidateBcqHeader(List<String> line) throws ValidationException {
        BcqHeader header = new BcqHeader();

        String sellingMtn = getAndValidateSellingMtn(line.get(0));
        String buyingParticipant = getAndValidateBuyingParticipant(line.get(1));
        Date tradingDate = getAndValidateDate(line.get(3));

        header.setSellingMtn(sellingMtn);
        header.setBuyingParticipant(buyingParticipant);
        header.setTradingDate(tradingDate);

        if (!uniqueDataSet.add(sellingMtn + "," + buyingParticipant + "," + tradingDate)) {
            throw new ValidationException(
                    String.format(DUPLICATE_DATE.getErrorMessage(),
                            header.getTradingDate(),
                            header.getSellingMtn(),
                            header.getBuyingParticipant()));
        }

        header.setTradingDate(getAndValidateTradingDate(header.getTradingDate()));

        return header;
    }

    private String getAndValidateSellingMtn(String sellingMtn) throws ValidationException {
        if (sellingMtn == null || sellingMtn.isEmpty()) {
            throw new ValidationException(MISSING_SELLING_MTN.getErrorMessage());
        }

        return sellingMtn;
    }

    private String getAndValidateBuyingParticipant(String buyingParticipant) throws ValidationException {
        if (buyingParticipant == null || buyingParticipant.isEmpty()) {
            throw new ValidationException(MISSING_BUYING_PARTICIPANT.getErrorMessage());
        }

        return buyingParticipant;
    }

    private String getAndValidateReferenceMtn(String referenceMtn) throws ValidationException {
        if (referenceMtn == null || referenceMtn.isEmpty()) {
            throw new ValidationException(MISSING_REFERENCE_MTN.getErrorMessage());
        }

        return referenceMtn;
    }

    private Date getAndValidateDate(String dateString) throws ValidationException {
        Date date = BCQParserUtil.parseDateTime(dateString);

        if (date == null) {
            throw new ValidationException(String.format(INCORRECT_FORMAT.getErrorMessage(),
                    "Date ", BCQParserUtil.DATE_TIME_FORMATS[0]));
        }

        return date;
    }

    private Date getAndValidateTradingDate(Date tradingDate) throws ValidationException {
        Date tradingDateNoTime;

        if (DateTimeUtils.isStartOfDay(tradingDate)) {
            tradingDateNoTime = DateUtils.addDays(tradingDate, -1);
        } else {
            tradingDateNoTime = DateTimeUtils.startOfDay(tradingDate);
        }

        if (validTradingDate == null) {
            Date today = new Date();
            Date yesterday = DateTimeUtils.startOfDay(DateUtils.addDays(today, -1));
            Date tomorrow = DateTimeUtils.startOfDay(DateUtils.addDays(today, 1));

            if (tradingDate.after(tomorrow) || tradingDate.before(yesterday)) {
                throw new ValidationException(
                        String.format(CLOSED_TRADING_DATE.getErrorMessage(),
                                formatAndGetDate(tradingDateNoTime, false)));
            }
        } else {
            if (tradingDate.after(validTradingDate) || tradingDate.before(validTradingDate)) {
                throw new ValidationException(
                        String.format(CLOSED_TRADING_DATE.getErrorMessage(),
                                formatAndGetDate(tradingDateNoTime, false)));
            }
        }

        return tradingDateNoTime;
    }

    private BcqData getAndValidateData(List<String> line, BcqInterval interval) throws ValidationException {
        BcqData data = new BcqData();
        Date endTime = getAndValidateDate(line.get(3));

        data.setReferenceMtn(getAndValidateReferenceMtn(line.get(2)));
        data.setStartTime(getStartTime(endTime, interval));
        data.setEndTime(endTime);
        data.setBcq(getAndValidateBcq(line.get(4)));

        return data;
    }

    private BigDecimal getAndValidateBcq(String bcqString) throws ValidationException {
        if (!NumberUtils.isParsable(bcqString)) {
            throw new ValidationException(
                    String.format(INCORRECT_FORMAT.getErrorMessage(), bcqString, "of decimal"));
        }

        return new BigDecimal(bcqString);
    }

    private void validateTimeInterval(Date date, Date previousDate, BcqInterval interval) throws ValidationException {
        long diff;

        if (previousDate == null) {
            Date startOfDay = DateTimeUtils.startOfDay(date);
            diff = date.getTime() - startOfDay.getTime();
        } else {
            diff = date.getTime() - previousDate.getTime();
        }

        if (diff != interval.getTimeInMillis()) {
            throw new ValidationException(
                    String.format(INCORRECT_TIME_INTERVALS.getErrorMessage(),
                            formatAndGetDate(date, true),
                            interval.getDescription()));
        }
    }

    private Date getStartTime(Date date, BcqInterval interval) {
        return new Date(date.getTime() - interval.getTimeInMillis());
    }

    private void validateDataSize(BcqHeader header, BcqInterval interval) throws ValidationException {
        int validBcqSize = interval.getValidNoOfRecords();

        if (header.getDataList().size() != validBcqSize) {
            throw new ValidationException(
                    String.format(INCOMPLETE_ENTRIES.getErrorMessage(),
                            formatAndGetDate(header.getTradingDate(), false),
                            header.getSellingMtn(),
                            header.getBuyingParticipant(),
                            validBcqSize));
        }
    }

    private String formatAndGetDate(Date date, boolean withTime) {
        String format = withTime ? BCQParserUtil.DATE_TIME_FORMATS[0] : BCQParserUtil.DATE_FORMATS[0];
        DateFormat dateFormat = new SimpleDateFormat(format);

        return dateFormat.format(date);
    }

    private List<BcqData> divideDataByInterval(BcqData data, BcqInterval interval) {
        List<BcqData> dividedDataList = new ArrayList<>();
        Date currentStartTime = data.getStartTime();
        BigDecimal currentBcq = data.getBcq();
        int divisor;

        if (interval == QUARTERLY) {
            divisor = intervalConfig == 5 ? 3 : 1;
        } else if (interval == HOURLY) {
            divisor = intervalConfig == 5 ? 12 : 4;
        } else {
            return singletonList(data);
        }

        for (int count = 1; count <= divisor; count ++) {
            BcqData partialData = new BcqData();
            partialData.setReferenceMtn(data.getReferenceMtn());
            partialData.setStartTime(currentStartTime);
            partialData.setEndTime(new Date(currentStartTime.getTime() + MINUTES.toMillis(intervalConfig)));
            partialData.setBcq(currentBcq.divide(BigDecimal.valueOf(divisor), 9, ROUND_HALF_UP));
            dividedDataList.add(partialData);

            currentStartTime = partialData.getEndTime();
        }

        return dividedDataList;
    }
}
