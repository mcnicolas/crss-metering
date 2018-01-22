ALTER TABLE metering.txn_bcq_data DROP CONSTRAINT uk_bcq_data;

ALTER TABLE metering.txn_bcq_data ADD CONSTRAINT uk_bcq_data UNIQUE (header_id, end_time, reference_mtn);

ALTER TABLE metering.txn_bcq_data ADD buyer_mtn VARCHAR(255);