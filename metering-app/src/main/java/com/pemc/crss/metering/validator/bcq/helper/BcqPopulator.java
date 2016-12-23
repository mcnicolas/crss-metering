package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.pemc.crss.metering.utils.BcqDateUtils.parseDateTime;
import static com.pemc.crss.metering.utils.DateTimeUtils.isStartOfDay;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay;
import static org.apache.commons.lang3.time.DateUtils.addDays;

public class BcqPopulator {

    private static final int SELLING_MTN_INDEX = 0;
    private static final int BILLING_ID_INDEX = 1;
    private static final int REFERENCE_MTN_INDEX = 2;
    private static final int DATE_INDEX = 3;
    private static final int BCQ_INDEX = 4;
    private static final int START_LINE_OF_DATA = 2;

    public List<BcqHeader> populate(List<List<String>> csv, BcqInterval interval) {
        List<BcqHeader> headerList = new ArrayList<>();
        for (List<String> line : csv.subList(START_LINE_OF_DATA, csv.size())) {
            BcqHeader header = populateHeader(line);
            List<BcqData> dataList;

            if (headerList.contains(header)) {
                header = headerList.get(headerList.indexOf(header));
                dataList = header.getDataList();
            } else {
                dataList = new ArrayList<>();
                headerList.add(header);
                header.setDataList(dataList);
            }

            BcqData data = populateData(line, interval);
            dataList.add(data);
        }

        for (BcqHeader header : headerList) {
            header.getDataList().sort((d1, d2) -> d1.getEndTime().compareTo(d2.getEndTime()));
        }

        return headerList;
    }

    private BcqHeader populateHeader(List<String> line) {
        BcqHeader header = new BcqHeader();
        String sellingMtn = line.get(SELLING_MTN_INDEX);
        String billingId = line.get(BILLING_ID_INDEX);
        Date tradingDate = getTradingDate(line.get(DATE_INDEX));

        header.setSellingMtn(sellingMtn);
        header.setBillingId(billingId);
        header.setTradingDate(tradingDate);

        return header;
    }

    private BcqData populateData(List<String> line, BcqInterval interval) {
        BcqData data = new BcqData();
        Date endTime = parseDateTime(line.get(DATE_INDEX));

        data.setReferenceMtn(line.get(REFERENCE_MTN_INDEX));
        data.setStartTime(getStartTime(endTime, interval));
        data.setEndTime(endTime);
        data.setBcq(new BigDecimal(line.get(BCQ_INDEX)));

        return data;
    }

    private Date getTradingDate(String dateString) {
        Date tradingDate = parseDateTime(dateString);

        if (tradingDate == null) {
            return null;
        }

        if (isStartOfDay(tradingDate)) {
            return addDays(tradingDate, -1);
        }

        return startOfDay(tradingDate);
    }

    private Date getStartTime(Date endTime, BcqInterval interval) {
        return new Date(endTime.getTime() - interval.getTimeInMillis());
    }

}
