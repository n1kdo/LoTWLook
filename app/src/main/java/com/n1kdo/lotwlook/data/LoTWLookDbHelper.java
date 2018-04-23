package com.n1kdo.lotwlook.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.n1kdo.lotwlook.PreferencesActivity;

class LoTWLookDbHelper extends SQLiteOpenHelper {
    private static final String TAG = "LoTWLookDbHelper";

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "LoTWLook.db";

    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES_ADIF_RECORD_ENTRY = "CREATE TABLE " + AdifRecordEntry.TABLE_NAME
            + " ("
            + AdifRecordEntry._ID + INTEGER_TYPE + " PRIMARY KEY" + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_BAND + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_CALL + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_COUNTRY + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_COUNTY + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_CQZONE + INTEGER_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_CREDIT_GRANTED + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_DXCC + INTEGER_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_FREQ + INTEGER_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_FREQ_RX + INTEGER_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_GRIDSQUARE + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_IOTA + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_ITUZONE + INTEGER_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_LOTW_2XQSL + INTEGER_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_LOTW_CREDIT_GRANTED + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_LOTW_DXCC_ENTITY_STATUS + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_LOTW_MODEGROUP + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_LOTW_OWNCALL + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_LOTW_QSL_MODE + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_MODE + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_PREFIX + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_QSL_RECEIVED + INTEGER_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_QSLRDATE + INTEGER_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_QSO_DATE + INTEGER_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_STATE + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_STATION_CALLSIGN + TEXT_TYPE + COMMA_SEP
            + AdifRecordEntry.COLUMN_NAME_TIMEON + TEXT_TYPE
            + " );";

    private static final String SQL_DELETE_ENTRIES_ADIF_RECORD_ENTRY = "DROP TABLE IF EXISTS " + AdifRecordEntry.TABLE_NAME;

    private final Context context;

    LoTWLookDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public final void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "running " + SQL_CREATE_ENTRIES_ADIF_RECORD_ENTRY);
        db.execSQL(SQL_CREATE_ENTRIES_ADIF_RECORD_ENTRY);

        // reset the user preference for last seen...
        PreferencesActivity.updateLastQslSeen(context, 0L);
    }

    public final void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES_ADIF_RECORD_ENTRY);
        onCreate(db);
    }

    public final void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
