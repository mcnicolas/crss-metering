package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.dto.BCQUploadFile;
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
import java.util.List;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Repository
@Slf4j
public class JdbcBCQDao implements BCQDao {

    @Value("${bcq.manifest}")
    private String insertManifest;

    @Value("${bcq.data}")
    private String insertData;

    @NonNull
    private final JdbcTemplate jdbcTemplate;

    @Override
    public long saveBCQUploadFile(String transactionID, BCQUploadFile bcqUploadFile) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(insertManifest, new String[]{"file_id"});
                    ps.setString(1, transactionID);
                    ps.setString(2, bcqUploadFile.getFileName());
                    ps.setLong(3, bcqUploadFile.getFileSize());
                    ps.setString(4, bcqUploadFile.getStatus().toString());

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void saveBCQData(long fileID, List<BCQData> dataList) {
        jdbcTemplate.batchUpdate(insertData, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                BCQData bcqData = dataList.get(i);
                ps.setLong(1, fileID);
                ps.setString(2, bcqData.getSellingMTN());
                ps.setString(3, bcqData.getBuyingParticipant());
                ps.setString(4, bcqData.getReferenceMTN());
                ps.setTimestamp(5, new Timestamp(bcqData.getStartTime().getTime()));
                ps.setTimestamp(6, new Timestamp(bcqData.getEndTime().getTime()));
                ps.setDouble(7, bcqData.getBcq());
            }

            @Override
            public int getBatchSize() {
                return dataList.size();
            }
        });
    }
}
