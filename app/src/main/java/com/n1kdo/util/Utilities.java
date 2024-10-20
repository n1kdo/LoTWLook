package com.n1kdo.util;

/*
 * Created by jeff on 4/20/2017.
 * collection of useful static methods
 */

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class Utilities {

    private static final DateFormat LONG_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
    public static final DateFormat SHORT_DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    static {
        LONG_DATE_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));
    }

    public static String formatDate(Date date) {
        return (date == null) ? "" : LONG_DATE_FORMAT.format(date);
    }

    public static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isEmptyString(String s) {
        return (s == null || s.isEmpty() || s.trim().isEmpty());
    }
}
