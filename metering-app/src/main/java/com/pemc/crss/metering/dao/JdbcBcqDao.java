package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.BcqData;
import com.pemc.crss.metering.dto.BcqHeader;
import com.pemc.crss.metering.dto.BcqUploadFile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
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

    @Value("${bcq.header.exists}")
    private String headerExists;

    @NonNull
    private final JdbcTemplate jdbcTemplate;

    @Override
    public long saveBcqUploadFile(String transactionID, BcqUploadFile bcqUploadFile) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertManifest, new String[]{"file_id"});
                    ps.setString(1, transactionID);
                    ps.setString(2, bcqUploadFile.getFileName());
                    ps.setLong(3, bcqUploadFile.getFileSize());
                    ps.setTimestamp(5, new Timestamp(bcqUploadFile.getSubmittedDate().getTime()));

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void saveBcqData(long fileID, Map<BcqHeader, List<BcqData>> headerDataMap) {
        for (Map.Entry<BcqHeader, List<BcqData>> entry : headerDataMap.entrySet()) {
            BcqHeader header = entry.getKey();
            boolean headerExists = headerExists(header);
            long headerId = saveBcqHeader(fileID, header, headerExists);
            List<BcqData> dataList = new ArrayList<>(entry.getValue());

            String sql = headerExists ? updateData : insertData;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    BcqData data = dataList.get(i);

                    if (headerExists) {
                        ps.setString(1, data.getReferenceMTN());
                        ps.setFloat(2, data.getBcq());
                        ps.setTimestamp(3, new Timestamp(data.getEndTime().getTime()));
                        ps.setLong(4, headerId);
                    } else {
                        ps.setLong(1, headerId);
                        ps.setString(2, data.getReferenceMTN());
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

    private long saveBcqHeader(long fileId, BcqHeader header, boolean update) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String sql = update ? updateHeader : insertHeader;

        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(sql, new String[]{"bcq_header_id"});

                    ps.setLong(1, fileId);

                    if (!update) {
                        ps.setString(2, header.getSellingMTN());
                        ps.setString(3, header.getBuyingParticipant());
                        ps.setString(4, header.getSellingParticipantName());
                        ps.setString(5, header.getSellingParticipantShortName());
                        ps.setString(6, header.getStatus().toString());
                        ps.setTimestamp(7, new Timestamp(header.getDeclarationDate().getTime()));
                    }

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    private boolean headerExists(BcqHeader header) {
        return jdbcTemplate.queryForObject(headerExists,
                new Object[] {
                        header.getSellingMTN(),
                        header.getBuyingParticipant(),
                        header.getDeclarationDate()
        }, Boolean.class);
    }
}
