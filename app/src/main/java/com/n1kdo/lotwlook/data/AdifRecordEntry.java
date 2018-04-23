package com.n1kdo.lotwlook.data;

import android.provider.BaseColumns;

/*
this interface just defines labels for columns and the sql table name.
 */
public interface AdifRecordEntry extends BaseColumns {
    String TABLE_NAME = "lotw_adif_records";

    String COLUMN_NAME_BAND = "band";
    String COLUMN_NAME_CALL = "call";
    String COLUMN_NAME_COUNTRY = "country";
    String COLUMN_NAME_COUNTY = "county";
    String COLUMN_NAME_CQZONE = "cqZone";
    String COLUMN_NAME_CREDIT_GRANTED = "credit_granted";
    String COLUMN_NAME_DXCC = "dxcc";
    String COLUMN_NAME_FREQ = "freq";
    String COLUMN_NAME_FREQ_RX = "freq_rx";
    String COLUMN_NAME_GRIDSQUARE = "gridsquare";
    String COLUMN_NAME_IOTA = "iota";
    String COLUMN_NAME_ITUZONE = "ituzone";
    String COLUMN_NAME_LOTW_2XQSL = "lotw_2xqsl";
    String COLUMN_NAME_LOTW_CREDIT_GRANTED = "lotw_credit_granted";
    String COLUMN_NAME_LOTW_DXCC_ENTITY_STATUS = "lotw_dxcc_entity_status";
    String COLUMN_NAME_LOTW_MODEGROUP = "lotw_modegroup";
    String COLUMN_NAME_LOTW_OWNCALL = "lotw_owncall";
    String COLUMN_NAME_LOTW_QSL_MODE = "lotw_qslmode";
    String COLUMN_NAME_MODE = "qso_mode";
    String COLUMN_NAME_PREFIX = "prefix";
    String COLUMN_NAME_QSL_RECEIVED = "qsl_received";
    String COLUMN_NAME_QSLRDATE = "qslrdate";
    String COLUMN_NAME_QSO_DATE = "qso_date";
    String COLUMN_NAME_STATE = "state";
    String COLUMN_NAME_STATION_CALLSIGN = "station_callsign";
    String COLUMN_NAME_TIMEON = "timeon";
}
