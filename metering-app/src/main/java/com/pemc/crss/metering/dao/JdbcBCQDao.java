package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.dto.BCQUploadFile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Repository
@Slf4j
public class JdbcBCQDao implements BCQDao {

    @Value("${bcq.manifest}")
    private String insertManifest;

    @Value("${bcq.data.insert}")
    private String insertData;

    @Value("${bcq.data.update}")
    private String updateData;

    @Value("${bcq.data.exists}")
    private String dataExists;

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
                    ps.setTimestamp(5, new Timestamp(bcqUploadFile.getSubmittedDate().getTime()));

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public void saveBCQData(long fileID, List<BCQData> dataList) {
        Map<Pair<String, String>, List<BCQData>> separatedDataListMap = separateListsBySellerAndBuyer(dataList);

        for (List<BCQData> partialDataList : separatedDataListMap.values()) {
            boolean dataExists = exists(partialDataList.get(0));
            String sql = dataExists ? updateData : insertData;

            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    BCQData data = dataList.get(i);

                    if (dataExists) {
                        ps.setLong(1, fileID);
                        ps.setString(2, data.getReferenceMTN());
                        ps.setFloat(3, data.getBcq());
                        ps.setString(4, data.getSellingMTN());
                        ps.setString(5, data.getBuyingParticipant());
                        ps.setTimestamp(6, new Timestamp(data.getEndTime().getTime()));
                    } else {
                        ps.setLong(1, fileID);
                        ps.setString(2, data.getSellingMTN());
                        ps.setString(3, data.getBuyingParticipant());
                        ps.setString(4, data.getReferenceMTN());
                        ps.setTimestamp(5, new Timestamp(data.getStartTime().getTime()));
                        ps.setTimestamp(6, new Timestamp(data.getEndTime().getTime()));
                        ps.setFloat(7, data.getBcq());
                    }
                }

                @Override
                public int getBatchSize() {
                    return partialDataList.size();
                }
            });
        }
    }

    private Map<Pair<String, String>, List<BCQData>> separateListsBySellerAndBuyer(List<BCQData> dataList) {
        Map<Pair<String, String>, List<BCQData>> dataListMap = new HashMap<>();

        dataList.forEach(data -> {
            Pair<String, String> key = Pair.of(data.getSellingMTN(), data.getBuyingParticipant());
            if (!dataListMap.containsKey(key)) {
                dataListMap.put(key, new ArrayList<>());
            }

            dataListMap.get(key).add(data);
        });

        return dataListMap;
    }

    private boolean exists(BCQData data) {
        return jdbcTemplate.queryForObject(dataExists,
                new Object[] {data.getSellingMTN(), data.getBuyingParticipant(), data.getEndTime()}, Boolean.class);
    }
}
