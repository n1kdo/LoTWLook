package com.n1kdo.adif;

import android.annotation.SuppressLint;

import java.util.Date;
import java.util.Locale;

/**
 * ADIF header record.  Not all fields are implemented.  This has the fields that are implemented by LOTW.  Maybe someday...
 * see http://www.adif.org/304/ADIF_304.htm
 *
 * @author n1kdo
 */
public class AdifHeader extends AdifBase {
    private enum AdifHeaderKeys {
        PROGRAMID, APP_LOTW_LASTQSL, APP_LOTW_NUMREC
    }

    private String programid;
    private Date lotw_lastqsl;
    private int lotw_numrec;

    AdifHeader() {
    }

    @SuppressLint("DefaultLocale")
    public final void parseLine(String line) {
        //System.out.println(line);
        String[] data = parseAdifLine(line);
        if (data.length == 2) {
            AdifHeaderKeys ahk;
            try {
                ahk = AdifHeaderKeys.valueOf(data[0].toUpperCase(Locale.US));
            } catch (IllegalArgumentException e) {
                System.out.println("missing case " + data[0]);
                return;
            }
            switch (ahk) {
                case PROGRAMID:
                    programid = data[1];
                    break;
                case APP_LOTW_LASTQSL:
                    lotw_lastqsl = getDate(data[1]);
                    break;
                case APP_LOTW_NUMREC:
                    lotw_numrec = getInt(data[1]);
                    break;
            }
        }
    }

    public final Date getLotw_lastqsl() {
        return lotw_lastqsl;
    }

    @SuppressLint("Unused")
    public final String getProgramid() {
        return programid;
    }

    @SuppressLint("Unused")
    public final int getLotw_numrec() {
        return lotw_numrec;
    }
}
