package com.pemc.crss.metering.dao;

import com.pemc.crss.commons.web.dto.datatable.PageableRequest;
import com.pemc.crss.metering.dto.*;
import com.pemc.crss.metering.parser.bcq.util.BCQParserUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.*;

@Repository
@Slf4j
public class JdbcBcqDao implements BcqDao {

    @Value("${bcq.manifest}")
    private String insertManifest;

    @Value("${bcq.header.insert}")
    private String insertHeader;

    @Value("${bcq.header.update}")
    private String updateHeader;

    @Value("${bcq.data.insert}")
    private String insertData;

    @Value("${bcq.data.update}")
    private String updateData;

    @Value("${bcq.header.count}")
    private String headerCount;

    @Value("${bcq.display.data}")
    private String displayData;

    @Value("${bcq.display.count}")
    private String displayCount;

    @Value("${bcq.display.pagination}")
    private String displayPagination;

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcBcqDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public long saveBcqUploadFile(String transactionID, BcqUploadFile bcqUploadFile) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        log.debug(insertManifest);
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertManifest, new String[]{"file_id"});
                    ps.setString(1, transactionID);
                    ps.setString(2, bcqUploadFile.getFileName());
                    ps.setLong(3, bcqUploadFile.getFileSize());
                    ps.setTimestamp(4, new Timestamp(bcqUploadFile.getSubmittedDate().getTime()));

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void saveBcqDeclaration(long fileID, List<BcqDeclaration> bcqDeclarationList) {
        for (BcqDeclaration bcqDeclaration : bcqDeclarationList) {
            BcqHeader header = bcqDeclaration.getHeader();
            boolean headerExists = headerExists(header);
            long headerId = saveBcqHeader(fileID, header, headerExists);
            List<BcqData> dataList = bcqDeclaration.getDataList();

            String sql = headerExists ? updateData : insertData;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    BcqData data = dataList.get(i);

                    if (headerExists) {
                        ps.setString(1, data.getReferenceMtn());
                        ps.setFloat(2, data.getBcq());
                        ps.setTimestamp(3, new Timestamp(data.getEndTime().getTime()));
                        ps.setLong(4, headerId);
                    } else {
                        ps.setLong(1, headerId);
                        ps.setString(2, data.getReferenceMtn());
                        ps.setTimestamp(3, new Timestamp(data.getStartTime().getTime()));
                        ps.setTimestamp(4, new Timestamp(data.getEndTime().getTime()));
                        ps.setFloat(5, data.getBcq());
                    }
                }

                @Override
                public int getBatchSize() {
                    return dataList.size();
                }
            });
        }
    }

    @Override
    public Page<BcqDeclarationDisplay> findAll(PageableRequest pageableRequest) {
        int totalRecords = getTotalRecords(pageableRequest);
        Map<String, String> params = pageableRequest.getMapParams();
        Date tradingDate = BCQParserUtil.parseDate(params.get("tradingDate"));
        String sellingParticipant = params.get("sellingParticipant");
        String sellingMtn = params.get("sellingMtn");
        String buyingParticipant = params.get("buyingParticipant");
        String status = params.get("status");

        BcqDisplayQueryBuilder builder = new BcqDisplayQueryBuilder(displayData, displayCount, displayPagination);
        BuilderData query = builder.countBcqDeclarations(tradingDate)
                .addBuyingParticipantFilter(buyingParticipant)
                .addSellingParticipantFilter(sellingParticipant)
                .addSellingMtnFilter(sellingMtn)
                .addStatusFilter(status)
                .orderBy(pageableRequest.getOrderList())
                .paginate(pageableRequest.getPageNo(), pageableRequest.getPageSize())
                .build();

        List<BcqDeclarationDisplay> bcqDeclarationList = jdbcTemplate.query(
                query.getSql(),
                query.getArguments(),
                rs -> {
                    List<BcqDeclarationDisplay> content = new ArrayList<>();
;
                    while (rs.next()) {
                        BcqDeclarationDisplay bcqDeclaration = new BcqDeclarationDisplay();

                        bcqDeclaration.setSellingParticipantName(rs.getString("selling_participant_name"));
                        bcqDeclaration.setSellingParticipantShortName(rs.getString("selling_participant_short_name"));
                        bcqDeclaration.setSellingMtn(rs.getString("selling_mtn"));
                        bcqDeclaration.setBuyingParticipant(rs.getString("buying_participant"));
                        bcqDeclaration.setTradingDate(rs.getString("trading_date"));
                        bcqDeclaration.setTransactionID("transaction_id");
                        bcqDeclaration.setSubmittedDate(rs.getString("submitted_date"));
                        bcqDeclaration.setUpdatedVia("");
                        bcqDeclaration.setStatus(rs.getString("status"));

                        content.add(bcqDeclaration);
                    }

                    return content;
                });

        return new PageImpl<>(
                bcqDeclarationList,
                pageableRequest.getPageable(),
                totalRecords);
    }

    /****************************************************
     * SUPPORT METHODS
     ****************************************************/

    private long saveBcqHeader(long fileId, BcqHeader header, boolean update) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = update ? updateHeader : insertHeader;

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"bcq_header_id"});

                    ps.setLong(1, fileId);

                    if (update) {
                        ps.setString(2, header.getSellingMtn());
                        ps.setString(3, header.getBuyingParticipant());
                        ps.setTimestamp(4, new Timestamp(header.getTradingDate().getTime()));
                    } else {
                        ps.setString(2, header.getSellingMtn());
                        ps.setString(3, header.getBuyingParticipant());
                        ps.setString(4, header.getSellingParticipantName());
                        ps.setString(5, header.getSellingParticipantShortName());
                        ps.setString(6, header.getStatus().toString());
                        ps.setTimestamp(7, new Timestamp(header.getTradingDate().getTime()));
                    }

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    private boolean headerExists(BcqHeader header) {
        return jdbcTemplate.queryForObject(headerCount,
                new Object[] {
                        header.getSellingMtn(),
                        header.getBuyingParticipant(),
                        header.getTradingDate()
        }, Integer.class) > 0;
    }

    private int getTotalRecords(PageableRequest pageableRequest) {
        Map<String, String> params = pageableRequest.getMapParams();
        Date tradingDate = BCQParserUtil.parseDate(params.get("tradingDate"));
        String sellingParticipant = params.get("sellingParticipant");
        String sellingMtn = params.get("sellingMtn");
        String buyingParticipant = params.get("buyingParticipant");
        String status = params.get("status");

        BcqDisplayQueryBuilder builder = new BcqDisplayQueryBuilder(displayData, displayCount, displayPagination);
        BuilderData query = builder.countBcqDeclarations(tradingDate)
                .addBuyingParticipantFilter(buyingParticipant)
                .addSellingParticipantFilter(sellingParticipant)
                .addSellingMtnFilter(sellingMtn)
                .addStatusFilter(status)
                .build();

        return jdbcTemplate.queryForObject(
                query.getSql(),
                query.getArguments(),
                Integer.class);
    }
}
