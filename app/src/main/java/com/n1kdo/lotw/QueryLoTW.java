package com.n1kdo.lotw;

import android.util.Log;

import com.n1kdo.adif.AdifResult;
import com.n1kdo.adif.AdifResultException;
import com.n1kdo.util.Utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.GeneralSecurityException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class QueryLoTW {
    private static final String TAG = QueryLoTW.class.getSimpleName();

    private static final String LOTW_URL = "https://lotw.arrl.org/lotwuser/lotwreport.adi";
    private static final DateFormat dateFormatForSince = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    private static final DateFormat dateFormatDate = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final DateFormat dateFormatTime = new SimpleDateFormat("HH:mm:ss", Locale.US);

    public static AdifResult callLotw(String username,
                                      String password,
                                      String owncall,
                                      String callsign,
                                      boolean detail,
                                      Date sinceDate,
                                      Date qsoStartDate,
                                      Date qsoEndDate,
                                      String mode,
                                      String band,
                                      int dxcc) throws AdifResultException {
        AdifResult adifResult;
        String since = null;
        if (sinceDate != null) {
            since = dateFormatForSince.format(sinceDate);
        }

        StringBuilder urlSB = new StringBuilder();
        urlSB.append(LOTW_URL);
        urlSB.append("?");

        addParameter(urlSB, "qso_query", "1");
        addParameter(urlSB, "qso_qsl", "yes");

        if (Utilities.notEmpty(since))
            addParameter(urlSB, "qso_qslsince", since);

        if (Utilities.notEmpty(owncall))
            addParameter(urlSB, "qso_owncall", owncall);

        if (Utilities.notEmpty(callsign))
            addParameter(urlSB, "qso_callsign", callsign);

        if (Utilities.notEmpty(mode))
            addParameter(urlSB, "qso_mode", mode);

        if (Utilities.notEmpty(band))
            addParameter(urlSB, "qso_band", band);

        if (dxcc != 0)
            addParameter(urlSB, "qso_dxcc", Integer.toString(dxcc));

        if (qsoStartDate != null) {
            String dateString = dateFormatDate.format(qsoStartDate);
            String timeString = dateFormatTime.format(qsoStartDate);
            addParameter(urlSB, "qso_startdate", dateString);
            addParameter(urlSB, "qso_starttime", timeString);
        }

        if (qsoEndDate != null) {
            String dateString = dateFormatDate.format(qsoEndDate);
            String timeString = dateFormatTime.format(qsoEndDate);
            addParameter(urlSB, "qso_enddate", dateString);
            addParameter(urlSB, "qso_endtime", timeString);
        }

        // qso_mydetail
        if (detail)
            addParameter(urlSB, "qso_qsldetail", "yes");
        addParameter(urlSB, "qso_withown", "yes");

        Log.d(TAG, "query is " + urlSB.toString());

        // add authentication info AFTER logging query, don't want this in the log!
        addParameter(urlSB, "login", username);
        addParameter(urlSB, "password", password);

        BufferedReader in = null;

        try {
            URL url = new URL(urlSB.toString());
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);

            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            int status = urlConnection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "callLotw: HTTP status " + status);
                throw new AdifResultException(AdifResultException.IO_EXCEPTION, "HTTP status " + status);
            }
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            adifResult = AdifResult.readAdif(in);
        } catch (GeneralSecurityException e) {
            throw new AdifResultException(AdifResultException.IO_EXCEPTION, e);
        } catch (IOException e) {
            throw new AdifResultException(AdifResultException.IO_EXCEPTION, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            } // if in != null
        } // finally
        return adifResult;
    }

    private static void addParameter(StringBuilder sb, String parameterName, String parameterValue) {
        if (sb.length() != 0)
            sb.append("&");
        sb.append(parameterName);
        sb.append("=");
        try {
            sb.append(URLEncoder.encode(parameterValue, "UTF-8"));
        } catch (Exception ignored) {
        }
    }
}
