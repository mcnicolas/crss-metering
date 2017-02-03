package com.pemc.crss.metering.utils;

import lombok.extern.slf4j.Slf4j;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class ResultSetUtils {

    public static <T> List<T> getListFromRsArray(ResultSet rs, String arrayCol, Class<T> clazz) throws SQLException {
        List<T> result = new ArrayList<>();
        java.sql.Array arr = rs.getArray(arrayCol);

        for (Object obj : (Object[])arr.getArray()) {
            try {
                T item = clazz.cast(obj);
                result.add(item);
            } catch (ClassCastException e) {
                log.error("Cannot cast column " + arrayCol + " to " + clazz.getTypeName());
            }
        }

        return result;
    }
}
