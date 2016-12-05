package com.pemc.crss.metering.parser.meterquantity;

import com.pemc.crss.metering.dto.mq.MeterData;
import com.pemc.crss.metering.dto.mq.MeterDataDetail;
import com.pemc.crss.metering.dto.mq.MeterDataHeader;
import com.pemc.crss.metering.parser.QuantityReader;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static java.util.Calendar.HOUR_OF_DAY;
import static java.util.Calendar.MINUTE;
import static org.apache.poi.ss.usermodel.DateUtil.getJavaCalendar;

@Slf4j
public class MeterQuantityExcelReader implements QuantityReader {

    @Override
    public MeterData readData(InputStream inputStream) throws IOException {
        MeterData retVal = new MeterData();

        // TODO: Use poi eventmodel for faster processing
        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (InvalidFormatException e) {
            throw new IOException(e);
        }

        Sheet sheet = workbook.getSheetAt(0);

        Iterator<Row> rowIterator = sheet.rowIterator();

        MeterDataHeader header = readHeader(rowIterator.next());

        List<MeterDataDetail> meterDataList = new ArrayList<>();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if (row.getCell(0) == null) {
                continue;
            }

            MeterDataDetail meterData = new MeterDataDetail();

            meterData.setSein(row.getCell(0).getStringCellValue());

            Calendar readingDateTime = getDateValue(row.getCell(1));
            Calendar time = getTimeValue(row.getCell(2));

            readingDateTime.set(HOUR_OF_DAY, time.get(HOUR_OF_DAY));
            readingDateTime.set(MINUTE, time.get(MINUTE));

            meterData.setReadingDateTime(readingDateTime.getTime());

            meterData.setKwd(getNumericValue(row.getCell(3)));
            meterData.setKwhd(getNumericValue(row.getCell(4)));
            meterData.setKvarhd(getNumericValue(row.getCell(5)));
            meterData.setKwr(getNumericValue(row.getCell(6)));
            meterData.setKwhr(getNumericValue(row.getCell(7)));
            meterData.setKvarhr(getNumericValue(row.getCell(8)));
            meterData.setEstimationFlag(getStringValue(row.getCell(9)));

            meterDataList.add(meterData);
        }

        retVal.setHeader(header);
        retVal.setDetails(meterDataList);

        return retVal;
    }

    private MeterDataHeader readHeader(Row row) {
        MeterDataHeader retVal = new MeterDataHeader();

        List<String> columns = new ArrayList<>();
        row.forEach(cell -> columns.add(cell.getStringCellValue()));

        retVal.setColumnNames(columns);

        return retVal;
    }

    private String getStringValue(Cell cell) {
        if (cell != null) {
            return cell.getStringCellValue();
        } else {
            return null;
        }
    }

    private Calendar getDateValue(Cell cell) {
        Calendar retVal = Calendar.getInstance();

        switch (CellType.forInt(cell.getCellType())) {
            case STRING:
                retVal.setTime(parseDate(cell.getStringCellValue()));

                break;
            case NUMERIC:
                retVal = getJavaCalendar(cell.getNumericCellValue());
        }

        return retVal;
    }

    private Date parseDate(String dateString) {
        String[] formats = {
                "MM-dd-yyyy",
                "MM/dd/yyyy"
        };

        for (String formatString : formats) {
            try {
                return new SimpleDateFormat(formatString).parse(dateString);
            } catch (ParseException e) {
            }
        }

        return null;
    }

    private Calendar getTimeValue(Cell cell) {
        Calendar retVal = Calendar.getInstance();

        switch (CellType.forInt(cell.getCellType())) {
            case STRING:
                String[] value = cell.getStringCellValue().split(":");
                retVal.set(HOUR_OF_DAY, Integer.parseInt(value[0]));
                retVal.set(MINUTE, Integer.parseInt(value[1]));

                break;
            case NUMERIC:
                retVal = getJavaCalendar(cell.getNumericCellValue());
        }

        return retVal;
    }

    private double getNumericValue(Cell cell) {
        double retVal = 0.0;

        if (cell != null) {
            switch (CellType.forInt(cell.getCellType())) {
                case STRING:
                    retVal = Double.valueOf(cell.getStringCellValue());
                    break;
                case NUMERIC:
                    retVal = cell.getNumericCellValue();
            }
        }

        return retVal;
    }

}
