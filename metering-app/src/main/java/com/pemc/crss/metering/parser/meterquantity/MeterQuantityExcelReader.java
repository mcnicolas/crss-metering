package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.mq.FileManifest;
import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.dto.mq.MeterDataHeader;
import com.pemc.crss.metering.parser.ParseException;
import com.pemc.crss.metering.parser.QuantityReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.google.common.base.CaseFormat.LOWER_CAMEL;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.pemc.crss.metering.utils.DateTimeUtils.parseDateAsLong;
import static java.math.RoundingMode.HALF_UP;
import static org.apache.commons.io.IOUtils.closeQuietly;
import static org.apache.commons.lang3.math.NumberUtils.isParsable;

@Slf4j
public class MeterQuantityExcelReader implements QuantityReader {

    @Override
    public MeterData readData(FileManifest fileManifest, InputStream inputStream) throws ParseException {
        log.debug("Parsing meter data fileName:{}", fileManifest.getFileName());

        MeterData retVal = new MeterData();

        try {
            // TODO: Use poi eventmodel for faster processing
            Workbook workbook = WorkbookFactory.create(inputStream);

            Sheet sheet = workbook.getSheetAt(0);
            log.debug("Last row:{}", sheet.getLastRowNum());

            Iterator<Row> rowIterator = sheet.rowIterator();

            MeterDataHeader header = readHeader(rowIterator.next());

            List<MeterDataDetail> meterDataList = new ArrayList<>();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();

                if (isBlank(row)) {
                    continue;
                }

                MeterDataDetail meterData = populateBean(row, header);
                meterData.setFileID(fileManifest.getFileID());
                meterData.setUploadType(fileManifest.getUploadType());
                meterData.setMspShortName(fileManifest.getMspShortName());
                meterData.setCreatedDateTime(fileManifest.getUploadDateTime());

                meterDataList.add(meterData);
            }

            retVal.setHeader(header);
            retVal.setDetails(meterDataList);
        } catch (Exception e) {
            String message = e.getMessage();

            if (e instanceof NumberFormatException) {
                message = "Meter reading is not a number.";
            }

            throw new ParseException(message, e);
        } finally {
            closeQuietly(inputStream);
        }

        return retVal;
    }

    private Map<String, Cell> convertMeterData(Row row, MeterDataHeader header) {
        List<String> columnNames = header.getColumnNames();

        Map<String, Cell> map = new HashMap<>();
        for (int i = 0; i < columnNames.size(); i++) {
            String columnName = UPPER_UNDERSCORE.to(LOWER_CAMEL, columnNames.get(i));
            map.put(columnName, row.getCell(i));
        }

        return map;
    }

    private MeterDataDetail populateBean(Row row, MeterDataHeader header) throws ParseException {
        Map<String, Cell> map = convertMeterData(row, header);

        MeterDataDetail retVal = new MeterDataDetail();

        retVal.setSein(getStringValue(map.get("seil")));
        retVal.setReadingDateTime(parseDateAsLong(
                getDateValue(map.get("bdate")),
                getTimeValue(map.get("time"))));
        retVal.setKwd(getNumericValue(map.get("kwDel")));
        retVal.setKwhd(getNumericValue(map.get("kwhDel")));
        retVal.setKvarhd(getNumericValue(map.get("kvarhDel")));
        retVal.setKwr(getNumericValue(map.get("kwRec")));
        retVal.setKwhr(getNumericValue(map.get("kwhRec")));
        retVal.setKvarhr(getNumericValue(map.get("kvarhRec")));
        retVal.setEstimationFlag(getStringValue(map.get("estimationFlag")));

        return retVal;
    }

    private boolean isBlank(Row row) {
        boolean retVal = false;

        Cell firstCell = row.getCell(0);
        if (firstCell == null || StringUtils.isBlank(firstCell.getStringCellValue())) {
            retVal = true;
        }

        return retVal;
    }

    private MeterDataHeader readHeader(Row row) {
        MeterDataHeader retVal = new MeterDataHeader();

        List<String> columns = new ArrayList<>();
        row.forEach(cell -> columns.add(getCellValueAsString(cell)));

        retVal.setColumnNames(columns);

        return retVal;
    }

    private String getCellValueAsString(Cell cell) {
        String retVal = "";

        if (cell != null) {
            switch (cell.getCellTypeEnum()) {
                case NUMERIC:
                    retVal = String.valueOf(cell.getNumericCellValue());
                    break;
                case FORMULA:
                    retVal = cell.getCellFormula();
                    break;
                case BOOLEAN:
                    retVal = String.valueOf(cell.getBooleanCellValue());
                    break;
                case ERROR:
                    retVal = new String(new byte[]{cell.getErrorCellValue()});
                    break;
                case BLANK:
                case STRING:
                default:
                    retVal = cell.getStringCellValue();
            }
        }

        return retVal;
    }

    private String getStringValue(Cell cell) {
        if (cell != null) {
            return cell.getStringCellValue();
        } else {
            return null;
        }
    }

    private String getDateValue(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        return formatter.formatCellValue(cell);
    }

    private String getTimeValue(Cell cell) {
        DataFormatter formatter = new DataFormatter();
        String time = formatter.formatCellValue(cell);

        return StringUtils.leftPad(time, 5, "0");

        // TODO: Trap parsing exception and throw "Incorrect Time Format. Format should be HH:mm"
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

    private BigDecimal getNumericValue(Cell cell) {
        BigDecimal retVal = null;

        if (cell != null) {
            switch (CellType.forInt(cell.getCellType())) {
                case STRING:
                    retVal = new BigDecimal(cell.getStringCellValue());
                    break;
                case NUMERIC:
                    retVal = new BigDecimal(String.valueOf(cell.getNumericCellValue()));
                    break;
            }

            if (retVal != null) {
                retVal.setScale(17, HALF_UP);
            }
        }

        return retVal;
    }

}
