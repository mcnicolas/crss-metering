package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.dto.mq.MeterDataHeader;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.QuantityReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.pemc.crss.metering.utils.DateTimeUtils.parseDateAsLong;
import static java.math.RoundingMode.HALF_UP;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;
import static org.supercsv.prefs.CsvPreference.STANDARD_PREFERENCE;

@Slf4j
public class MeterQuantityCSVReader implements QuantityReader {

    @Override
    public MeterData readData(FileManifest fileManifest, InputStream inputStream) throws ParseException {
        MeterData meterData = new MeterData();

        try (ICsvListReader reader = new CsvListReader(new InputStreamReader(inputStream), STANDARD_PREFERENCE)) {
            List<MeterDataDetail> meterDataDetails = new ArrayList<>();

            MeterDataHeader header = readHeader(reader.getHeader(true));

            List<String> row;
            while ((row = reader.read()) != null) {
                if (row.size() == 1) {
                    throw new ParseException("Cannot parse CSV file. It might be an invalid CSV file or malformed.");
                }

                if (isBlank(row)) {
                    continue;
                }

                MeterDataDetail detail = populateBean(row, header);
                detail.setFileID(fileManifest.getFileID());
                detail.setUploadType(fileManifest.getUploadType());
                detail.setMspShortName(fileManifest.getMspShortName());
                detail.setCreatedDateTime(fileManifest.getUploadDateTime());

                meterDataDetails.add(detail);
            }

            meterData.setHeader(header);
            meterData.setDetails(meterDataDetails);
        } catch (Exception e) {
            throw new ParseException(e.getMessage(), e);
        }

        return meterData;
    }

    private boolean isBlank(List<String> row) {
        boolean retVal = false;

        if (StringUtils.isBlank(row.get(0))) {
            retVal = true;
        }

        return retVal;
    }

    private MeterDataHeader readHeader(String[] header) {
        MeterDataHeader meterDataHeader = new MeterDataHeader();
        meterDataHeader.setColumnNames(Arrays.asList(header));

        // Validation:
        // 1. Should not contain empty headers

        return meterDataHeader;
    }

    private Map<String, String> convertMeterData(List<String> row, MeterDataHeader header) throws ParseException {
        List<String> columnNames = header.getColumnNames();

        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < columnNames.size(); i++) {
            if (columnNames.get(i) == null) {
                throw new ParseException("Invalid column header");
            }

            String columnName = UPPER_UNDERSCORE.to(LOWER_CAMEL, columnNames.get(i));
            String value = row.get(i);
            map.put(columnName, value);
        }

        return map;
    }

    private MeterDataDetail populateBean(List<String> row, MeterDataHeader header) throws ParseException {
        Map<String, String> map = convertMeterData(row, header);

        MeterDataDetail retVal = new MeterDataDetail();
        retVal.setSein(map.get("seil"));
        retVal.setReadingDateTime(parseDateAsLong(map.get("bdate"), map.get("time")));
        retVal.setKwd(getNumericValue(map.get("kwDel")));
        retVal.setKwhd(getNumericValue(map.get("kwhDel")));
        retVal.setKvarhd(getNumericValue(map.get("kvarhDel")));
        retVal.setKwr(getNumericValue(map.get("kwRec")));
        retVal.setKwhr(getNumericValue(map.get("kwhRec")));
        retVal.setKvarhr(getNumericValue(map.get("kvarhRec")));
        retVal.setEstimationFlag(map.get("estimationFlag"));

        return retVal;
    }

    private BigDecimal getNumericValue(String value) throws ParseException {
        BigDecimal retVal = null;

        if (value != null) {
            if (isParsable(value)) {
                retVal = new BigDecimal(value).setScale(17, HALF_UP);
            } else {
                throw new ParseException("Meter reading is not a number. [" + value + "]");
            }
        }

        return retVal;
    }

    private BigDecimal getNumericValue(List<String> row, int index) throws ParseException {
        BigDecimal retVal = null;

        if (row.size() > index) {
            String value = row.get(index);
            if (value != null) {
                if (isParsable(value)) {
                    retVal = new BigDecimal(value).setScale(17, HALF_UP);
                } else {
                    throw new ParseException("Meter reading is not a number.");
                }
            }
        }

        return retVal;
    }

}
