package com.pemc.crss.metering.validator;

import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqDetails;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.parser.bcq.BcqInterval;
import com.pemc.crss.metering.utils.DateTimeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.math.BigDecimal;
import java.util.*;

import static com.pemc.crss.metering.constants.BcqValidationRules.*;
import static com.pemc.crss.metering.parser.bcq.BcqInterval.*;
import static com.pemc.crss.metering.parser.bcq.util.BcqDateUtils.*;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.util.Collections.singletonList;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.apache.commons.lang3.StringUtils.isBlank;

public class BcqValidator {

    private static final int VALID_NO_OF_COLUMNS = 5;

    private int intervalConfig;
    private Date validTradingDate;
    private Set<String> uniqueDataSet = new HashSet<>();
    private String errorMessage;

    public BcqValidator(int intervalConfig) {
        this.intervalConfig = intervalConfig;
        validTradingDate = null;
    }

    public BcqDetails getAndValidateBcq(List<List<String>> csv) { //TODO Refactor
        BcqDetails details = new BcqDetails();

        validateNotEmpty(csv);
        BcqInterval interval = getAndValidateInterval(csv.get(0));
        validateColumnHeader(csv.get(1));

        if (errorMessage != null) {
            details.setErrorMessage(errorMessage);
            return details;
        }

        List<BcqHeader> headerList = new ArrayList<>();

        Date currentTradingDate = null;

        for (List<String> line : csv.subList(2, csv.size())) {
            BcqHeader header = getAndValidateBcqHeader(line, currentTradingDate);
            if (header != null) {
                List<BcqData> dataList = new ArrayList<>();

                if (headerList.contains(header)) {
                    header = headerList.get(headerList.indexOf(header));
                    dataList = header.getDataList();
                } else {
                    if (currentTradingDate == null) {
                        currentTradingDate = header.getTradingDate();
                    }
                    headerList.add(header);
                    header.setDataList(dataList);
                }

                BcqData data = getAndValidateData(line, interval);
                dataList.add(data);
            }

            if (errorMessage != null) {
                details.setErrorMessage(errorMessage);
                return details;
            }
        }

        for (BcqHeader header : headerList) {
            validateDataSize(header, interval);
            List<BcqData> dataList = header.getDataList();
            dataList.sort((d1, d2) -> d1.getEndTime().compareTo(d2.getEndTime()));
            header.setDataList(getAndValidateDataList(dataList, interval));

            if (errorMessage != null) {
                details.setErrorMessage(errorMessage);
                return details;
            }
        }
        details.setHeaderList(headerList);
        return details;
    }

    private void validateNotEmpty(List<List<String>> csv) {
        if (csv.size() < 3) {
            setErrorMessage(EMPTY.getErrorMessage());
        }
    }

    private void validateColumnHeader(List<String> line) {
        if (line.size() != VALID_NO_OF_COLUMNS) {
            setErrorMessage(INCORRECT_COLUMN_HEADER_COUNT.getErrorMessage());
        }
    }

    private BcqInterval getAndValidateInterval(List<String> line) {
        String intervalString = line.get(1);
        BcqInterval interval = BcqInterval.fromDescription(intervalString);

        if (interval == null ||
                intervalConfig == 5 && interval == QUARTERLY ||
                intervalConfig == 15 && interval == FIVE_MINUTES_PERIOD) {
            setErrorMessage(String.format(INCORRECT_DECLARED_INTERVAL.getErrorMessage(), intervalString));
        }

        return interval;
    }

    private List<BcqData> getAndValidateDataList(List<BcqData> dataList, BcqInterval interval) {
        List<BcqData> finalizedDataList = new ArrayList<>();
        Date previousDate = null;

        for (BcqData data : dataList) {
            validateTimeInterval(data.getEndTime(), previousDate, interval);
            previousDate = data.getEndTime();
            finalizedDataList.addAll(divideDataByInterval(data, interval));
        }

        return finalizedDataList;
    }

    private BcqHeader getAndValidateBcqHeader(List<String> line, Date currentTradingDate) {
        if (line.stream().allMatch(StringUtils::isBlank)) {
            setErrorMessage(EMPTY_LINE.getErrorMessage());
            return null;
        }

        BcqHeader header = new BcqHeader();

        String sellingMtn = getAndValidateSellingMtn(line.get(0));
        String billingId = getAndValidateBillingId(line.get(1));
        Date tradingDate = getAndValidateDate(line.get(3));

        header.setSellingMtn(sellingMtn);
        header.setBillingId(billingId);
        header.setTradingDate(tradingDate);

        if (!uniqueDataSet.add(sellingMtn + "," + billingId + "," + tradingDate)) {
            setErrorMessage(String.format(DUPLICATE_DATE.getErrorMessage(),
                            formatAndGetDate(header.getTradingDate(), false),
                            header.getSellingMtn(),
                            header.getBillingId()));
        }

        header.setTradingDate(getAndValidateTradingDate(header.getTradingDate()));

        if (currentTradingDate != null && !currentTradingDate.equals(header.getTradingDate())) {
            setErrorMessage(INVALID_TRADING_DATE.getErrorMessage());
        }

        return header;
    }

    private String getAndValidateSellingMtn(String sellingMtn) {
        if (sellingMtn == null || sellingMtn.isEmpty()) {
            setErrorMessage(MISSING_SELLING_MTN.getErrorMessage());
        }

        return sellingMtn;
    }

    private String getAndValidateBillingId(String billingId) {
        if (billingId == null || billingId.isEmpty()) {
            setErrorMessage(MISSING_BILLING_ID.getErrorMessage());
        }

        return billingId;
    }

    private String getAndValidateReferenceMtn(String referenceMtn) {
        if (referenceMtn == null || referenceMtn.isEmpty()) {
            setErrorMessage(MISSING_REFERENCE_MTN.getErrorMessage());
        }

        return referenceMtn;
    }

    private Date getAndValidateDate(String dateString) {
        if (isBlank(dateString)) {
            setErrorMessage(MISSING_DATE.getErrorMessage());
            return null;
        }

        Date date = parseDateTime(dateString);
        if (date == null) {
            setErrorMessage(String.format(INCORRECT_FORMAT.getErrorMessage(), "Date ", DATE_TIME_FORMAT));
        }

        return date;
    }

    private Date getAndValidateTradingDate(Date tradingDate) {
        if (tradingDate == null) {
            return null;
        }

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
                setErrorMessage(String.format(CLOSED_TRADING_DATE.getErrorMessage(),
                                formatAndGetDate(tradingDateNoTime, false)));
            }
        } else {
            if (tradingDate.after(validTradingDate) || tradingDate.before(validTradingDate)) {
                setErrorMessage(String.format(CLOSED_TRADING_DATE.getErrorMessage(),
                        formatAndGetDate(tradingDateNoTime, false)));
            }
        }

        return tradingDateNoTime;
    }

    private BcqData getAndValidateData(List<String> line, BcqInterval interval) {
        BcqData data = new BcqData();
        Date endTime = getAndValidateDate(line.get(3));

        data.setReferenceMtn(getAndValidateReferenceMtn(line.get(2)));
        data.setStartTime(getStartTime(endTime, interval));
        data.setEndTime(endTime);
        data.setBcq(getAndValidateBcq(line.get(4)));

        return data;
    }

    private BigDecimal getAndValidateBcq(String bcqString) {
        if (isBlank(bcqString)) {
            setErrorMessage(MISSING_BCQ.getErrorMessage());
            return null;
        }

        if (!NumberUtils.isParsable(bcqString)) {
            setErrorMessage(String.format(INCORRECT_FORMAT.getErrorMessage(), bcqString, "of decimal"));
        }

        BigDecimal bcq = new BigDecimal(bcqString);

        if (bcq.signum() == -1) {
            setErrorMessage(NEGATIVE_BCQ.getErrorMessage());
        }

        return new BigDecimal(bcqString);
    }

    private void validateTimeInterval(Date date, Date previousDate, BcqInterval interval) {
        long diff;

        if (previousDate == null) {
            Date startOfDay = DateTimeUtils.startOfDay(date);
            diff = date.getTime() - startOfDay.getTime();
        } else {
            diff = date.getTime() - previousDate.getTime();
        }

        if (diff != interval.getTimeInMillis()) {
            setErrorMessage(String.format(INCORRECT_TIME_INTERVALS.getErrorMessage(),
                            formatAndGetDate(date, true),
                            interval.getDescription()));
        }
    }

    private Date getStartTime(Date date, BcqInterval interval) {
        if (date == null) {
            return null;
        }
        return new Date(date.getTime() - interval.getTimeInMillis());
    }

    private void validateDataSize(BcqHeader header, BcqInterval interval) {
        int validBcqSize = interval.getValidNoOfRecords();

        if (header.getDataList().size() != validBcqSize) {
            setErrorMessage(String.format(INCOMPLETE_ENTRIES.getErrorMessage(),
                            formatAndGetDate(header.getTradingDate(), false),
                            header.getSellingMtn(),
                            header.getBillingId(),
                            validBcqSize));
        }
    }

    private String formatAndGetDate(Date date, boolean withTime) {
        return withTime ? formatDateTime(date) : formatDate(date);
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

    private void setErrorMessage(String errorMessage) {
        if (this.errorMessage == null) {
            this.errorMessage = errorMessage;
        }
    }
}
