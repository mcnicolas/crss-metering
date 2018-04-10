-- execute using postgres user
grant usage on schema meterprocess to crss_metering;
grant select on meterprocess.txn_meter_validated_daily to crss_metering;
grant select on meterprocess.txn_meter_validated_monthly to crss_metering;

grant usage on schema registration to crss_metering;
grant select on registration.txn_participant to crss_metering;
grant select on registration.txn_applicant to crss_metering;
grant select on registration.user_applicant_link to crss_metering;

DROP VIEW IF EXISTS metering.vw_mq_extraction_daily;
DROP VIEW IF EXISTS metering.vw_mq_extraction_monthly;
drop view if exists metering.VW_TP_LDAP_USER_MAP;

create or replace view metering.vw_mq_extraction_daily as

  SELECT
    distinct
    mq.msp_shortname,
    mq.sein,
    vald.trade_participant_short_name,
    mq.reading_datetime,
    vald.kwhd,
    vald.kvarhd,
    vald.kwd,
    vald.kwhr,
    vald.kvarhr,
    vald.kwr,
    vald.estimation_flag,
    mqf.upload_datetime,
    vald.created_date_time,
    mqh.transaction_id
  FROM metering.txn_meter_data_daily mq
    JOIN metering.txn_mq_manifest_file mqf
      ON mq.file_id = mqf.file_id
    JOIN metering.txn_mq_manifest_header mqh
      ON mqf.header_id = mqh.header_id and mqh.category in ('CORRECTED_DAILY', 'DAILY')
    INNER JOIN meterprocess.txn_meter_validated_daily vald
      ON vald.sein = mq.sein AND mq.msp_shortname = vald.msp_shortname AND to_timestamp(cast(mq.reading_datetime AS VARCHAR),
                                                                                        'yyyymmddHH24mi') = vald.reading_datetime;



create or replace view metering.vw_mq_extraction_monthly as
  SELECT
    distinct
    mq.msp_shortname,
    mq.sein,
    vald.trade_participant_short_name,
    mq.reading_datetime,
    vald.kwhd,
    vald.kvarhd,
    vald.kwd,
    vald.kwhr,
    vald.kvarhr,
    vald.kwr,
    vald.estimation_flag,
    mqf.upload_datetime,
    vald.created_date_time,
    mqh.transaction_id
  FROM metering.txn_meter_data_monthly mq
    JOIN metering.txn_mq_manifest_file mqf
      ON mq.file_id = mqf.file_id
    JOIN metering.txn_mq_manifest_header mqh
      ON mqf.header_id = mqh.header_id and mqh.category in ('CORRECTED_MONTHLY', 'MONTHLY')
    INNER JOIN meterprocess.txn_meter_validated_monthly vald
      ON vald.sein = mq.sein AND mq.msp_shortname = vald.msp_shortname AND to_timestamp(cast(mq.reading_datetime AS VARCHAR),
                                                                                        'yyyymmddHH24mi') = vald.reading_datetime;



create or replace view metering.VW_TP_LDAP_USER_MAP as
select distinct tp.short_name, ual.linked_users from registration.txn_participant tp
  join registration.txn_applicant ap on tp.applicant_id = ap.id
  join registration.user_applicant_link ual on ap.id = ual.applicant_id;



alter table metering.vw_mq_extraction_daily OWNER TO crss_metering;
alter table metering.vw_mq_extraction_monthly OWNER TO crss_metering;
alter table metering.VW_TP_LDAP_USER_MAP OWNER TO crss_metering;

