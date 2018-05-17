alter table metering.txn_bcq_data drop constraint if exists uk_bcq_data;


create unique index uk_bcq_data_nobm ON metering.txn_bcq_data (header_id, end_time) where buyer_mtn is null;
create unique index uk_bcq_data_bm ON metering.txn_bcq_data (header_id, end_time, buyer_mtn) where buyer_mtn is not null;