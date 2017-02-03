package com.pemc.crss.metering.dto.bcq.mapper;

import com.pemc.crss.metering.dto.bcq.specialevent.BcqSpecialEvent;
import com.pemc.crss.metering.utils.ResultSetUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class BcqSpecialEventMapper implements RowMapper<BcqSpecialEvent> {

    @Override
    public BcqSpecialEvent mapRow(ResultSet rs, int rowNum) throws SQLException {
        BcqSpecialEvent specialEvent = new BcqSpecialEvent();
        specialEvent.setEventId(rs.getLong("event_id"));
        specialEvent.setDeadlineDate(rs.getDate("deadline_date"));
        specialEvent.setRemarks(rs.getString("remarks"));
        specialEvent.setTradingDates(ResultSetUtils.getListFromRsArray(rs, "trading_dates", Date.class));
        specialEvent.setTradingParticipants(ResultSetUtils.getListFromRsArray(rs, "trading_participants", String.class));
        return specialEvent;
    }
}
