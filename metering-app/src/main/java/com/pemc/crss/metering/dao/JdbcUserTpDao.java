package com.pemc.crss.metering.dao;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Repository
public class JdbcUserTpDao implements UserTpDao {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    private final String TP_SHORTNAME_QUERY_BY_USER_ID = "SELECT A.tp_short_name FROM VW_TP_LDAP_USER_MAP A WHERE A.user_id = ?";

    @Autowired
    public JdbcUserTpDao(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
        JdbcExceptionTranslator exceptionTranslator = new JdbcExceptionTranslator();

        this.jdbcTemplate = jdbcTemplate;
        this.jdbcTemplate.setExceptionTranslator(exceptionTranslator);

        this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
        ((JdbcTemplate) this.namedParameterJdbcTemplate.getJdbcOperations()).setExceptionTranslator(exceptionTranslator);
    }

    @Override
    public String findBShortNameByTpId(Long userId) {
        List<String> tpShortNames = jdbcTemplate.query(TP_SHORTNAME_QUERY_BY_USER_ID, new Object[]{userId}, rs -> {
            List<String> results = new ArrayList<>();
            while (rs.next()) {

                results.add(rs.getString("tp_short_name"));
            }
            return results;
        });
        return CollectionUtils.isNotEmpty(tpShortNames) ? tpShortNames.get(0) : null;
    }
}
