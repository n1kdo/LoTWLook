package com.n1kdo.adif;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.n1kdo.util.Utilities;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * ADIF QSO record. Not all fields are implemented. This has the fields that are
 * implemented by LOTW. Maybe someday... see
 * http://www.adif.org/305/ADIF_305.htm
 *
 * @author n1kdo
 */
public class AdifRecord extends AdifBase implements Parcelable {
    private static final String CREDIT_GRANTED_DELIMITER = ",";

    private enum AdifRecordKeys {
        APP_LOTW_2XQSL,
        APP_LOTW_CQZ_INFERRED, // prevents warning
        APP_LOTW_CREDIT_GRANTED,
        APP_LOTW_DXCC_ENTITY_STATUS,
        APP_LOTW_ITUZ_INFERRED, // prevents warning
        APP_LOTW_MODEGROUP,
        APP_LOTW_NPSUNIT, // prevents warning
        APP_LOTW_OWNCALL,
        APP_LOTW_QSLMODE,
        BAND,
        CALL,
        CNTY,
        COUNTRY,
        CQZ,
        CREDIT_GRANTED,
        DXCC,
        FREQ,
        FREQ_RX,
        GRIDSQUARE,
        IOTA,
        ITUZ,
        MODE,
        PFX,
        QSL_RCVD,
        QSLRDATE,
        QSO_DATE,
        STATE,
        STATION_CALLSIGN,
        TIME_ON,
        VUCC_GRIDS
    }

    private long _id; // NOT in ADIF. used for database ref.
    private String band;
    private String call;
    private String country;
    private String county;
    private int cqZone;
    private String credit_granted[];
    private int dxcc;
    private String freq;
    private String freq_rx;
    private String gridsquare;
    private String iota;
    private int ituZone;
    private boolean lotw_2xqsl;
    private String lotw_credit_granted[];
    private String lotw_dxcc_entity_status;
    private String lotw_modegroup;
    private String lotw_owncall;
    private String lotw_qslmode;
    private String mode;
    private String prefix;
    private boolean qsl_received;
    private Date qslrdate;
    private Date qso_date;
    private String state;
    private String station_callsign;
    private String timeon;

    public AdifRecord() {
    }

    public final String getBand() {
        return safe(band);
    }

    public final String getCall() {
        return safe(call);
    }

    public final String getCountry() {
        return safe(country);
    }

    public final String getCounty() {
        return safe(county);
    }

    public final int getCqZone() {
        return cqZone;
    }

    @SuppressLint("unused")
    public final String[] getCreditGranted() {
        return credit_granted;
    }

    public final String getCreditGrantedString() {
        return makeListFromArray(credit_granted);
    }

    public final int getDxcc() {
        return dxcc;
    }

    public final String getGridSquare() {
        return safe(gridsquare);
    }

    public final long getId() {
        return _id;
    }

    public final String getIota() {
        return safe(iota);
    }

    public final int getItuZone() {
        return ituZone;
    }

    public final String getLotwQslMode() {
        return lotw_qslmode;
    }

    @SuppressLint("Unused")
    public final String[] getLotwCreditGranted() {
        return lotw_credit_granted;
    }

    public final String getLotwCreditGrantedString() {
        return makeListFromArray(lotw_credit_granted);
    }

    public final String getLotwDxccEntityStatus() {
        return safe(lotw_dxcc_entity_status);
    }

    public final String getLotwModeGroup() {
        return safe(lotw_modegroup);
    }

    public final String getLotwOwnCall() {
        return lotw_owncall;
    }

    public final String getMode() {
        return safe(mode);
    }

    public final String getPrefix() {
        return safe(prefix);
    }

    public final Date getQslRDate() {
        return qslrdate;
    }

    public final Date getQsoDate() {
        return qso_date;
    }

    public final Date getQsoDateTime() {
        if (qso_date == null)
            return null;
        if (timeon == null || timeon.length() != 6) {
            return qso_date;
        }
        long d = qso_date.getTime();
        d = d + getMillisecondsFromTimeOn(timeon);
        return new Date(d);
    }

    private static long getMillisecondsFromTimeOn(String s) {
        long l = 0;
        if (s != null && s.length() == 6) {
            l = Integer.parseInt(s.substring(0,2)) * 3600000L;
            l += Integer.parseInt(s.substring(2,4)) * 60000L;
            l += Integer.parseInt(s.substring(4,6)) * 1000L;
        }
        return l;
    }
    public final String getState() {
        return safe(state);
    }

    public final String getStationCallsign() {
        return station_callsign;
    }

    public final String getTimeOn() {
        return safe(timeon);
    }

    public final boolean isLotw2xQsl() {
        return lotw_2xqsl;
    }

    public final boolean isQslReceived() {
        return qsl_received;
    }

    @SuppressLint("DefaultLocale")
    final void parseLine(String line) {
        String[] data = parseAdifLine(line);
        if (data.length == 2) {
            AdifRecordKeys ark;
            try {
                ark = AdifRecordKeys.valueOf(data[0].toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                System.out.println("AdifRecord: missing case " + data[0] + " with data " + data[1]);
                return;
            }
            switch (ark) {
                case CALL:
                    call = data[1];
                    break;
                case BAND:
                    band = data[1];
                    break;
                case MODE:
                    mode = data[1];
                    break;
                case COUNTRY:
                    country = deCapitalize(data[1]);
                    break;
                case APP_LOTW_DXCC_ENTITY_STATUS:
                    lotw_dxcc_entity_status = data[1];
                    break;
                case PFX:
                    prefix = data[1];
                    break;
                case APP_LOTW_2XQSL:
                    lotw_2xqsl = getBoolean(data[1]);
                    break;
                case QSO_DATE:
                    qso_date = getDate8(data[1]);
                    break;
                case TIME_ON:
                    timeon = data[1];
                    break;
                case QSL_RCVD:
                    qsl_received = getBoolean(data[1]);
                    break;
                case QSLRDATE:
                    qslrdate = getDate8(data[1]);
                    break;
                case DXCC:
                    dxcc = getInt(data[1]);
                    break;
                case CQZ:
                    cqZone = getInt(data[1]);
                    break;
                case ITUZ:
                    ituZone = getInt(data[1]);
                    break;
                case APP_LOTW_MODEGROUP:
                    lotw_modegroup = data[1];
                    break;
                case GRIDSQUARE:
                    gridsquare = data[1];
                    break;
                case IOTA:
                    iota = data[1];
                    break;
                case CNTY:
                    county = data[1];
                    break;
                case STATE:
                    state = data[1];
                    break;
                case APP_LOTW_QSLMODE:
                    lotw_qslmode = data[1];
                    break;
                case APP_LOTW_OWNCALL:
                    lotw_owncall = data[1];
                    break;
                case STATION_CALLSIGN:
                    station_callsign = data[1];
                    break;
                case CREDIT_GRANTED:
                    credit_granted = getStringArray(data[1]);
                    break;
                case APP_LOTW_CREDIT_GRANTED:
                    lotw_credit_granted = getStringArray(data[1]);
                    break;
                case FREQ:
                    freq = data[1];
                    break;
                case FREQ_RX:
                    freq_rx = data[1];
                    break;
                case VUCC_GRIDS:
                    if (Utilities.isEmptyString(gridsquare)) {
                        gridsquare = data[1];
                    }
                    break;
                default: // any other keys I don't care about.
                    break;
            }
        }
    }

    public final void setBand(String band) {
        this.band = band;
    }

    public final void setCall(String call) {
        this.call = call;
    }

    public final void setCountry(String country) {
        this.country = country;
    }

    public final void setCounty(String county) {
        this.county = county;
    }

    public final void setCqZone(int cqZone) {
        this.cqZone = cqZone;
    }

    @SuppressLint("Unused")
    public final void setCreditGranted(String[] credit_granted) {
        this.credit_granted = credit_granted;
    }

    public final void setCreditGrantedFromString(String credit_granted_string) {
        this.credit_granted = credit_granted_string.split(CREDIT_GRANTED_DELIMITER);
    }

    public final void setDxcc(int dxcc) {
        this.dxcc = dxcc;
    }

    public final void setGridSquare(String gridSquare) {
        this.gridsquare = gridSquare;
    }

    public final void setId(long id) {
        this._id = id;
    }

    public final void setIota(String iota) {
        this.iota = iota;
    }

    public final void setItuZone(int ituZone) {
        this.ituZone = ituZone;
    }

    public final void setLotw2xQsl(boolean lotw_2xqsl) {
        this.lotw_2xqsl = lotw_2xqsl;
    }

    public final void setLotwCreditGrantedFromString(String lotw_credit_granted_string) {
        this.lotw_credit_granted = lotw_credit_granted_string.split(CREDIT_GRANTED_DELIMITER);
    }

    public final void setLotwDxccEntityStatus(String lotw_dxcc_entity_status) {
        this.lotw_dxcc_entity_status = lotw_dxcc_entity_status;
    }

    public final void setLotwModeGroup(String lotw_modegroup) {
        this.lotw_modegroup = lotw_modegroup;
    }

    public final void setLotwOwnCall(String lotw_owncall) {
        this.lotw_owncall = lotw_owncall;
    }

    public final void setLotwQslMode(String lotw_qslmode) {
        this.lotw_qslmode = lotw_qslmode;
    }

    public final void setMode(String mode) {
        this.mode = mode;
    }

    public final void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public final void setQslReceived(boolean qsl_received) {
        this.qsl_received = qsl_received;
    }

    public final void setQslRDate(Date qslrdate) {
        this.qslrdate = qslrdate;
    }

    public final void setQslRDate(long qslrdate_int) {
        this.qslrdate = new Date(qslrdate_int);
    }

    @SuppressLint("unused")
    public final void setQsoDate(Date qso_date) {
        this.qso_date = qso_date;
    }

    public final void setQsoDate(long qso_date_int) {
        this.qso_date = new Date(qso_date_int);
    }

    public final void setState(String state) {
        this.state = state;
    }

    public final void setStationCallsign(String station_callsign) {
        this.station_callsign = station_callsign;
    }

    public final void setTimeOn(String timeon) {
        this.timeon = timeon;
    }

    public final String getFreq() {
        return freq;
    }

    public final void setFreq(String freq) {
        this.freq = freq;
    }

    public final String getFreqRx() {
        return freq_rx;
    }

    public final void setFreqRx(String freq_rx) {
        this.freq_rx = freq_rx;
    }

    private static String makeListFromArray(String[] ary) {
        if (ary == null || ary.length == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (String s : ary) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(s);
        }
        return sb.toString();
    }

    public final String toString() {
        return station_callsign + " " + call + " " +
                (getQsoDateTime() != null ? DATE_FORMATTER.format(getQsoDateTime()) : "") +
                " " + freq;
    }

    private AdifRecord(Parcel in) {
        _id = in.readLong();
        band = in.readString();
        call = in.readString();
        country = in.readString();
        county = in.readString();
        cqZone = in.readInt();
        credit_granted = in.createStringArray();
        dxcc = in.readInt();
        freq = in.readString();
        freq_rx = in.readString();
        gridsquare = in.readString();
        iota = in.readString();
        ituZone = in.readInt();
        lotw_2xqsl = in.readByte() != 0x00;
        lotw_credit_granted = in.createStringArray();
        lotw_dxcc_entity_status = in.readString();
        lotw_modegroup = in.readString();
        lotw_owncall = in.readString();
        lotw_qslmode = in.readString();
        mode = in.readString();
        prefix = in.readString();
        qsl_received = in.readByte() != 0x00;
        long tmpQslrdate = in.readLong();
        qslrdate = tmpQslrdate != -1 ? new Date(tmpQslrdate) : null;
        long tmpQso_date = in.readLong();
        qso_date = tmpQso_date != -1 ? new Date(tmpQso_date) : null;
        state = in.readString();
        station_callsign = in.readString();
        timeon = in.readString();
    }

    @Override
    public final int describeContents() {
        return 0;
    }

    @Override
    public final void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(_id);
        dest.writeString(band);
        dest.writeString(call);
        dest.writeString(country);
        dest.writeString(county);
        dest.writeInt(cqZone);
        dest.writeStringArray(credit_granted);
        dest.writeInt(dxcc);
        dest.writeString(freq);
        dest.writeString(freq_rx);
        dest.writeString(gridsquare);
        dest.writeString(iota);
        dest.writeInt(ituZone);
        dest.writeByte((byte) (lotw_2xqsl ? 0x01 : 0x00));
        dest.writeStringArray(lotw_credit_granted);
        dest.writeString(lotw_dxcc_entity_status);
        dest.writeString(lotw_modegroup);
        dest.writeString(lotw_owncall);
        dest.writeString(lotw_qslmode);
        dest.writeString(mode);
        dest.writeString(prefix);
        dest.writeByte((byte) (qsl_received ? 0x01 : 0x00));
        dest.writeLong(qslrdate != null ? qslrdate.getTime() : -1L);
        dest.writeLong(qso_date != null ? qso_date.getTime() : -1L);
        dest.writeString(state);
        dest.writeString(station_callsign);
        dest.writeString(timeon);
    }

    public static final Parcelable.Creator<AdifRecord> CREATOR = new Parcelable.Creator<AdifRecord>() {
        @Override
        public AdifRecord createFromParcel(Parcel in) {
            return new AdifRecord(in);
        }

        @Override
        public AdifRecord[] newArray(int size) {
            return new AdifRecord[size];
        }
    };

    public static final Comparator<AdifRecord> NATURAL_COMPARE = new Comparator<AdifRecord>() {
        public int compare(AdifRecord first, AdifRecord second) {
            return ((Long) first._id).compareTo(second._id);
        }
    };

    public static final Comparator<AdifRecord> MYCALL_COMPARE = new Comparator<AdifRecord>() {
        public int compare(AdifRecord first, AdifRecord second) {
            return first.lotw_owncall.compareTo(second.lotw_owncall);
        }
    };

    public static final Comparator<AdifRecord> BAND_COMPARE = new Comparator<AdifRecord>() {
        public int compare(AdifRecord first, AdifRecord second) {
            return first.band.compareTo(second.band);
        }
    };

    public static final Comparator<AdifRecord> CALLSIGN_COMPARE = new Comparator<AdifRecord>() {
        public int compare(AdifRecord first, AdifRecord second) {
            return first.call.compareTo(second.call);
        }
    };

    public static final Comparator<AdifRecord> COUNTRY_COMPARE = new Comparator<AdifRecord>() {
        public int compare(AdifRecord first, AdifRecord second) {
            return first.country.compareTo(second.country);
        }
    };

    public static final Comparator<AdifRecord> MODE_COMPARE = new Comparator<AdifRecord>() {
        public int compare(AdifRecord first, AdifRecord second) {
            return first.mode.compareTo(second.mode);
        }
    };

    public static final Comparator<AdifRecord> QSO_DATE_COMPARE = new Comparator<AdifRecord>() {
        public int compare(AdifRecord first, AdifRecord second) {
            return first.qso_date.compareTo(second.qso_date);
        }
    };

    public static void sort(List<AdifRecord> list, Comparator<AdifRecord> comparator, boolean descending) {
        Collections.sort(list, comparator);
        if (descending) {
            Collections.reverse(list);
        }
    }
}
