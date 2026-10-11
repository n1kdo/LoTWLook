package com.n1kdo.adif;

import android.annotation.SuppressLint;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Locale;

public class AdifResult {
    private final static String ADIF_EOH = "<eoh>";
    private final static String ADIF_EOR = "<eor>";
    // LoTW has no reasonable error response; on authentication problems it serves its
    // HTML login page. These fragments never appear in a valid ADIF response, so they
    // are checked on every line, case-insensitively. Keep them loose (fragments rather
    // than exact tags) so cosmetic markup changes don't silently break detection.
    private final static String USERNAME_PASSWORD_INCORRECT = "username/password incorrect";
    private final static String LOGIN_FORM_ACTION = "action=\"/lotwuser/lotwreport.adi\"";

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
                line = line.trim();
                String lowerLine = line.toLowerCase(Locale.US);

                // Detect the HTML login page on every line, in every state: a valid
                // ADIF response never contains these fragments.
                if (lowerLine.contains(USERNAME_PASSWORD_INCORRECT)) {
                    throw new AdifResultException(AdifResultException.INVALID_CREDENTIALS_EXCEPTION);
                }
                if (lowerLine.contains(LOGIN_FORM_ACTION)) {
                    foundLoginForm = true;
                }

                switch (readState) {
                    case HEADER:
                        // Enter HTML state on any line that looks like the start of an
                        // HTML/XML document, not just "<HTML".
                        if (lowerLine.startsWith("<html") || lowerLine.startsWith("<!doctype")
                                || lowerLine.startsWith("<?xml")) {
                            readState = LOTW_READ_STATE.HTML;
                        } else if (line.isEmpty()) {
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
                        // nothing to do; the login-page checks above run for every line.
                        break;
                } // switch
            } // while
        } // try
        catch (IOException e) {
            throw new AdifResultException(AdifResultException.IO_EXCEPTION, e);
        }
        // If the stream ended in the middle of a record (no trailing <eor>), keep it rather
        // than silently dropping the last QSL. A record without a CALL is not usable.
        if (readState == LOTW_READ_STATE.ADIF_RECORDS && !adifRecord.getCall().isEmpty()) {
            adifRecords.add(adifRecord);
        }
        if (foundLoginForm) {
            throw new AdifResultException(AdifResultException.LOGIN_FAILED_EXCEPTION);
        }
        return new AdifResult(adifHeader, adifRecords);
    } // readAdif()


} // AdifResult
