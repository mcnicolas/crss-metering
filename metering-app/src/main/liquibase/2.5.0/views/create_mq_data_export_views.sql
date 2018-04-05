-- execute using postgres user
grant usage on schema meterprocess to crss_metering;
grant select on meterprocess.txn_meter_validated_daily to crss_metering;
grant select on meterprocess.txn_meter_validated_monthly to crss_metering


create or replace view metering.vw_mq_extraction_daily as

SELECT
  distinct
  mqh.category,
  mq.msp_shortname,
  mq.sein,
  vald.trade_participant_short_name,
  vald.reading_datetime,
  vald.kwhd,
  vald.kvarhd,
  vald.kwd,
  vald.kwhr,
  vald.kvarhr,
  vald.kwr,
  vald.estimation_flag,
  mqf.upload_datetime,
  vald.created_date_time,
FROM metering.txn_meter_data_daily mq
  JOIN metering.txn_mq_manifest_file mqf
    ON mq.file_id = mqf.file_id
  JOIN metering.txn_mq_manifest_header mqh
    ON mqf.header_id = mqh.header_id
  INNER JOIN meterprocess.txn_meter_validated_daily vald
    ON vald.sein = mq.sein AND mq.msp_shortname = vald.msp_shortname AND to_timestamp(cast(mq.reading_datetime AS VARCHAR),
                                                                                      'yyyymmddHH24mi') = vald.reading_datetime;



create or replace view metering.vw_mq_extraction_monthly as
  SELECT
  distinct
  mqh.category,
  mq.msp_shortname,
  mq.sein,
  vald.trade_participant_short_name,
  vald.reading_datetime,
  vald.kwhd,
  vald.kvarhd,
  vald.kwd,
  vald.kwhr,
  vald.kvarhr,
  vald.kwr,
  vald.estimation_flag,
  mqf.upload_datetime,
  vald.created_date_time
FROM metering.txn_meter_data_monthly mq
  JOIN metering.txn_mq_manifest_file mqf
    ON mq.file_id = mqf.file_id
  JOIN metering.txn_mq_manifest_header mqh
    ON mqf.header_id = mqh.header_id
  INNER JOIN meterprocess.txn_meter_validated_monthly vald
    ON vald.sein = mq.sein AND mq.msp_shortname = vald.msp_shortname AND to_timestamp(cast(mq.reading_datetime AS VARCHAR),
                                                                                      'yyyymmddHH24mi') = vald.reading_datetime;

alter table metering.vw_mq_extraction_daily OWNER TO crss_metering;
alter table metering.vw_mq_extraction_monthly OWNER TO crss_metering;