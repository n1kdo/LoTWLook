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
import java.util.TimeZone;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;

public class QueryLoTW {
    private static final String TAG = QueryLoTW.class.getSimpleName();

    private static final String LOTW_URL = "https://lotw.arrl.org/lotwuser/lotwreport.adi";
    private static final int CONNECT_TIMEOUT_MILLIS = 15000;
    private static final int READ_TIMEOUT_MILLIS = 60000;
    // LoTW interprets all query dates/times as UTC, and the Date values here come from
    // UTC parsing of the ADIF response. Format in UTC; never in device-local time.
    // SimpleDateFormat is NOT thread-safe, and callLotw() can run concurrently on the
    // IntentService worker thread and the JobService query thread; each gets its own.
    private static final ThreadLocal<DateFormat> SINCE_FORMATTER =
            ThreadLocal.withInitial(() -> utcFormatter("yyyy-MM-dd HH:mm:ss"));
    private static final ThreadLocal<DateFormat> DATE_FORMATTER =
            ThreadLocal.withInitial(() -> utcFormatter("yyyy-MM-dd"));
    private static final ThreadLocal<DateFormat> TIME_FORMATTER =
            ThreadLocal.withInitial(() -> utcFormatter("HH:mm:ss"));

    private static SimpleDateFormat utcFormatter(String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern, Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter;
    }

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
        String since;
        if (sinceDate != null) {
            since = SINCE_FORMATTER.get().format(sinceDate);
        } else {
            since = "2020-01-01";
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
            String dateString = DATE_FORMATTER.get().format(qsoStartDate);
            String timeString = TIME_FORMATTER.get().format(qsoStartDate);
            addParameter(urlSB, "qso_startdate", dateString);
            addParameter(urlSB, "qso_starttime", timeString);
        }

        if (qsoEndDate != null) {
            String dateString = DATE_FORMATTER.get().format(qsoEndDate);
            String timeString = TIME_FORMATTER.get().format(qsoEndDate);
            addParameter(urlSB, "qso_enddate", dateString);
            addParameter(urlSB, "qso_endtime", timeString);
        }

        // qso_mydetail
        if (detail)
            addParameter(urlSB, "qso_qsldetail", "yes");
        addParameter(urlSB, "qso_withown", "yes");

        Log.d(TAG, "query is " + urlSB);

        // add authentication info AFTER logging query, don't want this in the log!
        addParameter(urlSB, "login", username);
        addParameter(urlSB, "password", password);

        BufferedReader in = null;
        HttpsURLConnection urlConnection = null;

        try {
            URL url = new URL(urlSB.toString());
            SSLContext sslContext = SSLContext.getInstance("TLSv1.2");
            sslContext.init(null, null, null);

            urlConnection = (HttpsURLConnection) url.openConnection();
            // avoid hanging forever on a half-dead network; the caller shows a
            // non-cancelable progress dialog while this is running.
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT_MILLIS);
            urlConnection.setReadTimeout(READ_TIMEOUT_MILLIS);
            urlConnection.addRequestProperty("User-Agent", "N1KDOs LoTWLook");
            urlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            int status = urlConnection.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                Log.d(TAG, "callLotw: HTTP status " + status);
                throw new AdifResultException(AdifResultException.IO_EXCEPTION, "HTTP status " + status);
            }
            in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            adifResult = AdifResult.readAdif(in);
        } catch (GeneralSecurityException | IOException e) {
            throw new AdifResultException(AdifResultException.IO_EXCEPTION, e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (Exception ignored) {
                }
            } // if in != null
            if (urlConnection != null) {
                urlConnection.disconnect();
            } // if urlConnection != null
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
