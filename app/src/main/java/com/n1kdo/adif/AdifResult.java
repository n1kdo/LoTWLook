package com.n1kdo.adif;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

public class AdifResult {
    private final static String ADIF_EOH = "<eoh>";
    private final static String ADIF_EOR = "<eor>";
    private final static String USERNAME_PASSWORD_INCORRECT = "<I>Username/password incorrect</I>";
    private final static String LOGIN_FORM = "<FORM ACTION=\"/lotwuser/lotwreport.adi\" METHOD=\"post\">";

    enum LOTW_READ_STATE {
        HEADER, ADIF_HEADER, ADIF_RECORDS, HTML
    }

    private final AdifHeader header;
    private final ArrayList<AdifRecord> records;

    private AdifResult(AdifHeader header, ArrayList<AdifRecord> records) {
        this.header = header;
        this.records = records;
    }

    public final AdifHeader getHeader() {
        return header;
    }

    public final ArrayList<AdifRecord> getRecords() {
        return records;
    }

    @SuppressLint("DefaultLocale")
    public static AdifResult readAdif(BufferedReader in) throws AdifResultException {
        LOTW_READ_STATE readState = LOTW_READ_STATE.HEADER;
        AdifRecord adifRecord = new AdifRecord();
        AdifHeader adifHeader = new AdifHeader();
        ArrayList<AdifRecord> adifRecords = new ArrayList<>();
        String line;
        // this service sucks because it does not have a reasonable error response, instead, it 
        // returns the LoTW login page.  There is logic here to detect that.

        boolean foundLoginForm = false;

        try {
            while ((line = in.readLine()) != null) {
                //System.out.println(line);
                line = line.trim();
                switch (readState) {
                    case HEADER:
                        if (line.toUpperCase().startsWith("<HTML")) {
                            readState = LOTW_READ_STATE.HTML;
                        }

                        if (line.length() == 0) {
                            readState = LOTW_READ_STATE.ADIF_HEADER;
                        }
                        break;
                    case ADIF_HEADER:
                        if (ADIF_EOH.equalsIgnoreCase(line)) {
                            readState = LOTW_READ_STATE.ADIF_RECORDS;
                        } else {
                            adifHeader.parseLine(line);
                        }
                        break;
                    case ADIF_RECORDS:
                        if (ADIF_EOR.equalsIgnoreCase(line)) {
                            adifRecords.add(adifRecord);
                            adifRecord = new AdifRecord();
                        } else {
                            adifRecord.parseLine(line);
                        }
                        break;
                    case HTML:
                        if (line.contains(USERNAME_PASSWORD_INCORRECT)) {
                            throw new AdifResultException(AdifResultException.INVALID_CREDENTIALS_EXCEPTION);
                        }
                        if (line.contains(LOGIN_FORM)) {
                            foundLoginForm = true;
                        }
                } // switch
            } // while
        } // try
        catch (IOException e) {
            throw new AdifResultException(AdifResultException.IO_EXCEPTION, e);
        }
        if (foundLoginForm) {
            throw new AdifResultException(AdifResultException.LOGIN_FAILED_EXCEPTION);
        }
        return new AdifResult(adifHeader, adifRecords);
    } // readAdif()


} // AdifResult
