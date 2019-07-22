CREATE UNIQUE index uk_meter_data_daily
	ON metering.txn_meter_data_daily (category, sein, reading_datetime);

CREATE UNIQUE index uk_meter_data_monthly
	ON metering.txn_meter_data_monthly (category, sein, reading_datetime);
