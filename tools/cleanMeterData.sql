-- set serveroutput on;
-- execute cleanMeterData;

CREATE OR REPLACE PROCEDURE cleanMeterData AS

  daily_count   NUMBER;
  monthly_count NUMBER;

  BEGIN
    SELECT count(sein)
    INTO daily_count
    FROM txn_meter_data_daily;
    dbms_output.put_line('TXN_METER_DATA_DAILY record count:' || daily_count);

    dbms_output.put_line('Cleaning up TXN_METER_DATA_DAILY');
    WHILE daily_count > 0
    LOOP
      SELECT count(sein)
      INTO daily_count
      FROM txn_meter_data_daily;

      DELETE FROM TXN_METER_DATA_DAILY
      WHERE ROWNUM <= 50000;
      COMMIT;
    END LOOP;

    SELECT count(sein)
    INTO monthly_count
    FROM txn_meter_data_monthly;
    dbms_output.put_line('TXN_METER_DATA_MONTHLY record count:' || monthly_count);

    dbms_output.put_line('Cleaning up TXN_METER_DATA_MONTHLY');
    WHILE monthly_count > 0
    LOOP
      SELECT count(sein)
      INTO monthly_count
      FROM txn_meter_data_monthly;

      DELETE FROM TXN_METER_DATA_MONTHLY
      WHERE ROWNUM <= 50000;
      COMMIT;
    END LOOP;

    DELETE FROM TXN_MQ_MANIFEST_FILE;
    DELETE FROM TXN_MQ_MANIFEST_HEADER;
    COMMIT;

  END;
