package com.pemc.crss.metering.validator.bcq.helper;

import com.pemc.crss.metering.constants.BcqInterval;
import com.pemc.crss.metering.dto.bcq.BcqData;
import com.pemc.crss.metering.dto.bcq.BcqHeader;

import java.math.BigDecimal;
import java.util.*;

import static com.pemc.crss.metering.constants.BcqInterval.fromDescription;
import static com.pemc.crss.metering.utils.BcqDateUtils.parseDateTime;
import static com.pemc.crss.metering.utils.DateTimeUtils.isStartOfDay;
import static com.pemc.crss.metering.utils.DateTimeUtils.startOfDay;
import static org.apache.commons.lang3.time.DateUtils.addDays;

public class BcqPopulator {

    private static final int INTERVAL_ROW_INDEX = 0;
    private static final int INTERVAL_COLUMN_INDEX = 1;
    private static final int SELLING_MTN_INDEX = 0;
    private static final int BILLING_ID_INDEX = 1;
    private static final int REFERENCE_MTN_INDEX = 2;
    private static final int DATE_INDEX = 3;
    private static final int BCQ_INDEX = 4;
    private static final int START_LINE_OF_DATA = 2;

    public List<BcqHeader> populate(List<List<String>> csv) {
        BcqInterval interval = fromDescription(csv.get(INTERVAL_ROW_INDEX).get(INTERVAL_COLUMN_INDEX));
        List<BcqHeader> headerList = new ArrayList<>();
        Set<BcqHeader> headerSet = new HashSet<>();
        Map<BcqHeader, List<BcqData>> headerDataMap = new HashMap<>();

        for (List<String> line : csv.subList(START_LINE_OF_DATA, csv.size())) {
            BcqHeader header = populateHeader(line);
            List<BcqData> dataList;

            header.setInterval(interval);
            if (headerSet.add(header)) {
                dataList = new ArrayList<>();
                headerList.add(header);
                header.setDataList(dataList);
                headerDataMap.put(header, dataList);
            } else {
                dataList = headerDataMap.get(header);
            }

            BcqData data = populateData(line, interval);
            dataList.add(data);
        }

        for (BcqHeader header : headerList) {
            header.getDataList().sort(Comparator.comparing(BcqData::getEndTime));
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
        String referenceMtn = line.get(REFERENCE_MTN_INDEX);
        Date endTime = parseDateTime(line.get(DATE_INDEX));

        data.setReferenceMtn(referenceMtn);
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
