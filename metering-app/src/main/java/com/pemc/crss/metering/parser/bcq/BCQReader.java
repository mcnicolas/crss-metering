package com.pemc.crss.metering.parser.bcq;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.parser.bcq.util.BCQParserUtil;
import com.pemc.crss.metering.validator.BCQValidator;
import com.pemc.crss.metering.validator.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.stereotype.Component;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Slf4j
@Component
public class BCQReader {

    private static final long DEFAULT_INTERVAL_CONFIG = TimeUnit.MINUTES.toMillis(5);
    private static final long DEFAULT_TIMEFRAME_CONFIG = TimeUnit.DAYS.toMillis(1);
    private static final int QUARTERLY_DIVISOR = 3;
    private static final int HOURLY_DIVISOR = 12;

    public List<BCQData> readData(InputStream inputStream) throws IOException, ValidationException {
        Map<Pair<String, String>, List<BCQData>> dataListMap = new HashMap<>();
        BCQInterval interval;

        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            String intervalString = reader.read().get(1);
            BCQValidator.validateInterval(intervalString);
            interval = BCQInterval.fromDescription(intervalString);

            //skip header
            reader.read();

            List<String> row;
            while ((row = reader.read()) != null) {
                int currentLineNo = reader.getLineNumber();
                BCQValidator.validateLine(row, currentLineNo, DEFAULT_TIMEFRAME_CONFIG);
                BCQData data = getData(row, interval);

                String sellingMTN = data.getSellingMTN();
                String buyingParticipant = data.getBuyingParticipant();
                Pair<String, String> sellerBuyerKey = new ImmutablePair<>(sellingMTN, buyingParticipant);

                if (!dataListMap.containsKey(sellerBuyerKey)) {
                    dataListMap.put(sellerBuyerKey, new ArrayList<>());
                }

                BCQValidator.validateNextData(dataListMap.get(sellerBuyerKey), data, interval, currentLineNo);

                dataListMap.get(sellerBuyerKey).add(data);
            }
        }

        List<BCQData> mergedDataList = dataListMap.values()
                .stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        return finalizeData(mergedDataList, interval);
    }

    private List<BCQData> finalizeData(List<BCQData> dataList, BCQInterval interval) {
        final List<BCQData> finalizedDataList = new ArrayList<>();

        if (interval == BCQInterval.FIVE_MINUTES_PERIOD) {
            return dataList;
        } else {
            dataList.forEach(data -> finalizedDataList.addAll(divideDataByInterval(data, interval)));
        }

        return finalizedDataList;
    }

    private List<BCQData> divideDataByInterval(BCQData data, BCQInterval interval) {
        List<BCQData> dividedDataList = new ArrayList<>();
        Date currentStartTime = data.getStartTime();
        double currentBCQ = data.getBcq();
        int divisor;

        if (interval == BCQInterval.QUARTERLY) {
            divisor = QUARTERLY_DIVISOR;
        } else {
            divisor = HOURLY_DIVISOR;
        }

        for (int count = 1; count <= divisor; count ++) {
            BCQData partialData = new BCQData();
            partialData.setSellingMTN(data.getSellingMTN());
            partialData.setBuyingParticipant(data.getBuyingParticipant());
            partialData.setReferenceMTN(data.getReferenceMTN());
            partialData.setStartTime(currentStartTime);
            partialData.setEndTime(new Date(currentStartTime.getTime() + DEFAULT_INTERVAL_CONFIG));
            partialData.setBcq(currentBCQ / divisor);
            dividedDataList.add(partialData);

            currentStartTime = partialData.getEndTime();
        }

        return dividedDataList;
    }

    private BCQData getData(List<String> row, BCQInterval interval) {
        Date endTime = BCQParserUtil.parseDateTime(row.get(3));
        BCQData data = new BCQData();

        data.setSellingMTN(row.get(0));
        data.setBuyingParticipant(row.get(1));
        data.setReferenceMTN(row.get(2));
        data.setStartTime(getStartTime(endTime, interval));
        data.setEndTime(endTime);
        data.setBcq(getBCQ(row.get(4)));

        return data;
    }

    private Date getStartTime(Date date, BCQInterval interval) {
        return new Date(date.getTime() - interval.getTimeInMillis());
    }

    private double getBCQ(String data) {
        return Double.parseDouble(data);
    }
}
