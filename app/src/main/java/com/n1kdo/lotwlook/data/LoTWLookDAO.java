package com.n1kdo.lotwlook.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.n1kdo.adif.AdifRecord;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class LoTWLookDAO {
    private static final String TAG = LoTWLookDAO.class.getSimpleName();

    private static final String SELECT_BY_ID = AdifRecordEntry._ID + " = ?";
    private static final String SELECT_BEFORE_ID = AdifRecordEntry._ID + " <= ?";

    private static final String SELECT_DUP_QSL =
            AdifRecordEntry.COLUMN_NAME_STATION_CALLSIGN + " = ? AND " + // 0
                    AdifRecordEntry.COLUMN_NAME_CALL + " = ? AND " + // 1
                    AdifRecordEntry.COLUMN_NAME_BAND + " = ? AND " + // 2
                    AdifRecordEntry.COLUMN_NAME_MODE + " = ? AND " + // 3
                    AdifRecordEntry.COLUMN_NAME_QSO_DATE + " = ? AND " + // 4
                    AdifRecordEntry.COLUMN_NAME_TIMEON + " = ?"; // 5

    private static final String[] allColumns = {
            AdifRecordEntry._ID, // 0
            AdifRecordEntry.COLUMN_NAME_BAND, // 1
            AdifRecordEntry.COLUMN_NAME_CALL, // 2
            AdifRecordEntry.COLUMN_NAME_COUNTRY, // 3
            AdifRecordEntry.COLUMN_NAME_COUNTY, // 4
            AdifRecordEntry.COLUMN_NAME_CQZONE, // 5
            AdifRecordEntry.COLUMN_NAME_CREDIT_GRANTED, // 6
            AdifRecordEntry.COLUMN_NAME_DXCC, // 7
            AdifRecordEntry.COLUMN_NAME_FREQ, // 8
            AdifRecordEntry.COLUMN_NAME_FREQ_RX, // 9
            AdifRecordEntry.COLUMN_NAME_GRIDSQUARE, // 10
            AdifRecordEntry.COLUMN_NAME_IOTA, // 11
            AdifRecordEntry.COLUMN_NAME_ITUZONE, // 12
            AdifRecordEntry.COLUMN_NAME_LOTW_2XQSL, // 13
            AdifRecordEntry.COLUMN_NAME_LOTW_CREDIT_GRANTED, // 14
            AdifRecordEntry.COLUMN_NAME_LOTW_DXCC_ENTITY_STATUS, // 15
            AdifRecordEntry.COLUMN_NAME_LOTW_MODEGROUP, // 16
            AdifRecordEntry.COLUMN_NAME_LOTW_OWNCALL, // 17
            AdifRecordEntry.COLUMN_NAME_LOTW_QSL_MODE, // 18
            AdifRecordEntry.COLUMN_NAME_MODE, // 19
            AdifRecordEntry.COLUMN_NAME_PREFIX, // 20
            AdifRecordEntry.COLUMN_NAME_QSL_RECEIVED, // 21
            AdifRecordEntry.COLUMN_NAME_QSLRDATE, // 22
            AdifRecordEntry.COLUMN_NAME_QSO_DATE, // 23
            AdifRecordEntry.COLUMN_NAME_STATE, // 24
            AdifRecordEntry.COLUMN_NAME_STATION_CALLSIGN, // 25
            AdifRecordEntry.COLUMN_NAME_TIMEON // 26
    };

    private SQLiteDatabase database;
    private final LoTWLookDbHelper dbHelper;

    public LoTWLookDAO(Context context) {
        dbHelper = new LoTWLookDbHelper(context);
    }

    public final void open() throws SQLException {
        Log.d(TAG, "opening database");
        database = dbHelper.getWritableDatabase();
    }

    public final void close() {
        Log.d(TAG, "closing database");
        dbHelper.close();
        database = null;
    }

    private AdifRecord findMatch(AdifRecord adifRecord) {
        AdifRecord match = null;
        String[] selectionArgs = {
                adifRecord.getStationCallsign(),
                adifRecord.getCall(),
                adifRecord.getBand(),
                adifRecord.getMode(),
                Long.toString(adifRecord.getQsoDate().getTime()),
                adifRecord.getTimeOn()
        };

        Cursor cursor = null;
        try {
            cursor = database.query(AdifRecordEntry.TABLE_NAME, allColumns, SELECT_DUP_QSL, selectionArgs, null, null, null);
            if (cursor != null && cursor.getCount() != 0) {
                cursor.moveToFirst();
                while (!cursor.isAfterLast()) {
                    match = cursorToAdifRecord(cursor);
                    cursor.moveToNext();
                }
            }
        } // try
        finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return match;
    }

    /* unused method
    public final void deleteAdifRecord(long id)
    {
        String [] selectionArgs = { String.valueOf(id) };
        database.delete(AdifRecordEntry.TABLE_NAME, SELECT_BY_ID, selectionArgs);
        Log.d(TAG, "deleted AdifRecord with id " + id);
    }
    END unused method */

    public final void deleteOldAdifRecords(long id) {
        int rowsDeleted;
        String[] selectionArgs = {String.valueOf(id)};
        rowsDeleted = database.delete(AdifRecordEntry.TABLE_NAME, SELECT_BEFORE_ID, selectionArgs);
        Log.d(TAG, "deleted " + rowsDeleted + " AdifRecord data record with id < " + id);
    }

    public final List<AdifRecord> getAllAdifRecords() {
        List<AdifRecord> adifRecordsList = new ArrayList<>();
        Cursor cursor = database.query(AdifRecordEntry.TABLE_NAME, allColumns, null, null, null, null, AdifRecordEntry._ID + " DESC");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            AdifRecord adifRecord = cursorToAdifRecord(cursor);
            adifRecordsList.add(adifRecord);
            cursor.moveToNext();
        }
        cursor.close();
        Log.d(TAG, "fetched " + adifRecordsList.size() + " adifRecords records.");
        return adifRecordsList;
    }

    private static AdifRecord cursorToAdifRecord(Cursor cursor) {
        AdifRecord adifRecord = new AdifRecord();
        adifRecord.setId(cursor.getLong(0));
        adifRecord.setBand(cursor.getString(1));
        adifRecord.setCall(cursor.getString(2));
        adifRecord.setCountry(cursor.getString(3));
        adifRecord.setCounty(cursor.getString(4));
        adifRecord.setCqZone(cursor.getInt(5));
        adifRecord.setCreditGrantedFromString(cursor.getString(6));
        adifRecord.setDxcc(cursor.getInt(7));
        adifRecord.setFreq(cursor.getString(8));
        adifRecord.setFreqRx(cursor.getString(9));
        adifRecord.setGridSquare(cursor.getString(10));
        adifRecord.setIota(cursor.getString(11));
        adifRecord.setItuZone(cursor.getInt(12));
        adifRecord.setLotw2xQsl(cursor.getInt(13) != 0);
        adifRecord.setLotwCreditGrantedFromString(cursor.getString(14));
        adifRecord.setLotwDxccEntityStatus(cursor.getString(15));
        adifRecord.setLotwModeGroup(cursor.getString(16));
        adifRecord.setLotwOwnCall(cursor.getString(17));
        adifRecord.setLotwQslMode(cursor.getString(18));
        adifRecord.setMode(cursor.getString(19));
        adifRecord.setPrefix(cursor.getString(20));
        adifRecord.setQslReceived(cursor.getInt(21) != 0);
        adifRecord.setQslRDate(cursor.getLong(22));
        adifRecord.setQsoDate(cursor.getLong(23));
        adifRecord.setState(cursor.getString(24));
        adifRecord.setStationCallsign(cursor.getString(25));
        adifRecord.setTimeOn(cursor.getString(26));
        return adifRecord;
    } // cursorToAdifRecord()

    private void saveAdifRecord(AdifRecord adifRecord) {
        String[] selectionArgs;
        ContentValues values = new ContentValues();
        values.put(AdifRecordEntry.COLUMN_NAME_BAND, adifRecord.getBand());
        values.put(AdifRecordEntry.COLUMN_NAME_CALL, adifRecord.getCall());
        values.put(AdifRecordEntry.COLUMN_NAME_COUNTRY, adifRecord.getCountry());
        values.put(AdifRecordEntry.COLUMN_NAME_COUNTY, adifRecord.getCounty());
        values.put(AdifRecordEntry.COLUMN_NAME_CQZONE, adifRecord.getCqZone());
        values.put(AdifRecordEntry.COLUMN_NAME_CREDIT_GRANTED, adifRecord.getCreditGrantedString());
        values.put(AdifRecordEntry.COLUMN_NAME_DXCC, adifRecord.getDxcc());
        values.put(AdifRecordEntry.COLUMN_NAME_FREQ, adifRecord.getFreq());
        values.put(AdifRecordEntry.COLUMN_NAME_FREQ_RX, adifRecord.getFreqRx());
        values.put(AdifRecordEntry.COLUMN_NAME_GRIDSQUARE, adifRecord.getGridSquare());
        values.put(AdifRecordEntry.COLUMN_NAME_IOTA, adifRecord.getIota());
        values.put(AdifRecordEntry.COLUMN_NAME_ITUZONE, adifRecord.getItuZone());
        values.put(AdifRecordEntry.COLUMN_NAME_LOTW_2XQSL, adifRecord.isLotw2xQsl());
        values.put(AdifRecordEntry.COLUMN_NAME_LOTW_CREDIT_GRANTED, adifRecord.getLotwCreditGrantedString());
        values.put(AdifRecordEntry.COLUMN_NAME_LOTW_DXCC_ENTITY_STATUS, adifRecord.getLotwDxccEntityStatus());
        values.put(AdifRecordEntry.COLUMN_NAME_LOTW_MODEGROUP, adifRecord.getLotwModeGroup());
        values.put(AdifRecordEntry.COLUMN_NAME_LOTW_OWNCALL, adifRecord.getLotwOwnCall());
        values.put(AdifRecordEntry.COLUMN_NAME_LOTW_QSL_MODE, adifRecord.getLotwQslMode());
        values.put(AdifRecordEntry.COLUMN_NAME_MODE, adifRecord.getMode());
        values.put(AdifRecordEntry.COLUMN_NAME_PREFIX, adifRecord.getPrefix());
        values.put(AdifRecordEntry.COLUMN_NAME_QSL_RECEIVED, adifRecord.isQslReceived());
        values.put(AdifRecordEntry.COLUMN_NAME_QSLRDATE, adifRecord.getQslRDate().getTime());
        values.put(AdifRecordEntry.COLUMN_NAME_QSO_DATE, adifRecord.getQsoDate().getTime());
        values.put(AdifRecordEntry.COLUMN_NAME_STATE, adifRecord.getState());
        values.put(AdifRecordEntry.COLUMN_NAME_STATION_CALLSIGN, adifRecord.getStationCallsign());
        values.put(AdifRecordEntry.COLUMN_NAME_TIMEON, adifRecord.getTimeOn());
        long id = adifRecord.getId();
        if (id != 0) { // update record
            //values.put(AdifRecordEntry._ID, id);
            selectionArgs = new String[]{String.valueOf(id)};
            int updated = database.update(AdifRecordEntry.TABLE_NAME, values, SELECT_BY_ID, selectionArgs);
            if (updated != 1) {
                Log.w(TAG, "Failed to update AdifRecord " + adifRecord);
            }
        } else { // insert
            id = database.insert(AdifRecordEntry.TABLE_NAME, null, values);
            adifRecord.setId(id);
            Log.d(TAG, "inserted AdifRecord " + adifRecord);
        }
        /* DON'T
        // read the record back
        selectionArgs = new String [] { String.valueOf(id) };
        Cursor cursor = database.query(AdifRecordEntry.TABLE_NAME, allColumns, SELECT_BY_ID, selectionArgs, null, null, null);
        cursor.moveToFirst();
        adifRecord = cursorToAdifRecord(cursor);
        cursor.close();
        return adifRecord;
        */
    } // saveAdifRecord()

    /**
     * update the database, adding new QSL records.  Don't add duplicates.
     * delete any old QSL records for updated QSLs.
     *
     * @param adifRecords the new records
     * @return the list of records added to the database.
     */
    public final List<AdifRecord> updateDatabase(Iterable<AdifRecord> adifRecords) {
        List<AdifRecord> newRecords = new LinkedList<>();
        AdifRecord match;
        for (AdifRecord adifRecord : adifRecords) {
            // is this QSL already in the database?
            match = findMatch(adifRecord);
            if (match != null) { // yes, the QSL is already in the database, update the received date.
                match.setQslRDate(adifRecord.getQslRDate());
                saveAdifRecord(match);
                Log.d(TAG, adifRecord + " is already in the database, updated QslRDate.");
            } else { // not already in the database, must be new.
                Log.d(TAG, adifRecord + " added to the database.");
                saveAdifRecord(adifRecord);
                newRecords.add(adifRecord);
            }
        }
        return newRecords;
    }
}
