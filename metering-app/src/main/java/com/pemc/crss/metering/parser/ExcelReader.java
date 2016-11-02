package com.pemc.crss.metering.parser;

import com.pemc.crss.metering.dto.MeterData2;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;

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
public class ExcelReader implements MeterQuantityReader {

    @Override
    public List<MeterData2> readData(InputStream inputStream) throws IOException {
        // TODO: Use poi eventmodel for faster processing
        Workbook workbook;
        try {
            workbook = WorkbookFactory.create(inputStream);
        } catch (InvalidFormatException e) {
            throw new IOException(e);
        }

        Sheet sheet = workbook.getSheetAt(0);

        List<MeterData2> meterDataList = new ArrayList<>();

        Iterator<Row> rowIterator = sheet.rowIterator();

        // Skip first row
        rowIterator.next();

        while (rowIterator.hasNext()) {
            Row row = rowIterator.next();

            if (row.getCell(0) == null) {
                continue;
            }

            MeterData2 meterData = new MeterData2();
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
            meterData.setEstimationFlag(row.getCell(9).getStringCellValue());

            meterDataList.add(meterData);
        }

        return meterDataList;
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
