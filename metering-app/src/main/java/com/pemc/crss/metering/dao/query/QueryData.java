package com.pemc.crss.metering.dao.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

@Data
@AllArgsConstructor
public class QueryData {

    private String sql;
    private MapSqlParameterSource source;

}
