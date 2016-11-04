package com.pemc.crss.metering.dao;

import com.pemc.crss.metering.dto.BCQData;
import com.pemc.crss.metering.dto.BCQUploadFile;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.inject.Inject;
import java.sql.PreparedStatement;
import java.sql.Timestamp;

@RequiredArgsConstructor(onConstructor = @__(@Inject))
@Repository
public class JdbcBCQDao implements BCQDao {

    @NonNull
    private final JdbcTemplate jdbcTemplate;

    @Override
    public long saveBCQUploadFile(long transactionID, BCQUploadFile bcqUploadFile) {
        String INSERT_SQL = "INSERT INTO TXN_BCQ_UPLOAD_FILE (FILE_ID, TRANSACTION_ID, FILE_NAME, FILE_SIZE, STATUS)" +
                " VALUES (NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                    ps.setLong(1, transactionID);
                    ps.setString(2, bcqUploadFile.getFileName());
                    ps.setLong(3, bcqUploadFile.getFileSize());
                    ps.setString(4, bcqUploadFile.getStatus().toString());

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }

    @Override
    public long saveBCQData(long fileID, BCQData bcqData) {
        String INSERT_SQL = "INSERT INTO TXN_BCQ_DATA (BCQ_DATA_ID, FILE_ID, SELLING_PARTICIPANT_NAME," +
                " SELLING_MTN, BUYING_PARTICIPANT_ID, REFERENCE_MTN, START_TIME, END_TIME, BCQ)" +
                " VALUES (NEXTVAL('HIBERNATE_SEQUENCE'), ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(
                connection -> {
                    PreparedStatement ps = connection.prepareStatement(INSERT_SQL);
                    ps.setLong(1, fileID);
                    ps.setString(2, bcqData.getSellingParticipantName());
                    ps.setString(3, bcqData.getSellingMTN());
                    ps.setLong(4, bcqData.getBuyingParticipantId());
                    ps.setString(5, bcqData.getReferenceMTN());
                    ps.setTimestamp(6, new Timestamp(bcqData.getStartTime().getTime()));
                    ps.setTimestamp(7, new Timestamp(bcqData.getEndTime().getTime()));
                    ps.setDouble(8, bcqData.getBcq());

                    return ps;
                },
                keyHolder);

        return keyHolder.getKey().longValue();
    }
}
