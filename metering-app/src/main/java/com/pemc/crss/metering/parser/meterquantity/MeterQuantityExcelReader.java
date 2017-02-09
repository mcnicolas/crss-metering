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
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static com.pemc.crss.metering.utils.DateTimeUtils.parseDateAsLong;
import static java.math.RoundingMode.HALF_UP;
import static org.apache.commons.io.IOUtils.closeQuietly;

@Slf4j
public class MeterQuantityExcelReader implements QuantityReader {

    @Override
    public MeterData readData(FileManifest fileManifest, InputStream inputStream) throws ParseException {
        log.debug("Parsing meter data fileName:{}", fileManifest.getFileName());

        MeterData retVal = new MeterData();

        Date parseDate = new Date();

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

                MeterDataDetail meterData = populateBean(row);
                meterData.setFileID(fileManifest.getFileID());
                meterData.setUploadType(fileManifest.getUploadType());
                meterData.setMspShortName(fileManifest.getMspShortName());
                meterData.setCreatedDateTime(parseDate);

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

    private MeterDataDetail populateBean(Row row) throws ParseException {
        MeterDataDetail retVal = new MeterDataDetail();

        retVal.setSein(row.getCell(0).getStringCellValue());

        String readingDate = getDateValue(row.getCell(1));
        String readingTime = getTimeValue(row.getCell(2));

        retVal.setReadingDateTime(parseDateAsLong(readingDate, readingTime));

        retVal.setKwd(getNumericValue(row.getCell(3)));
        retVal.setKwhd(getNumericValue(row.getCell(4)));
        retVal.setKvarhd(getNumericValue(row.getCell(5)));
        retVal.setKwr(getNumericValue(row.getCell(6)));
        retVal.setKwhr(getNumericValue(row.getCell(7)));
        retVal.setKvarhr(getNumericValue(row.getCell(8)));
        retVal.setEstimationFlag(getStringValue(row.getCell(9)));

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

    private BigDecimal getNumericValue(Cell cell) {
        BigDecimal retVal = null;

        if (cell != null) {
            switch (CellType.forInt(cell.getCellType())) {
                case STRING:
                    retVal = new BigDecimal(cell.getStringCellValue());
                    break;
                case NUMERIC:
                    retVal = new BigDecimal(String.valueOf(cell.getNumericCellValue()));
            }

            retVal.setScale(17, HALF_UP);
        }

        return retVal;
    }

}
