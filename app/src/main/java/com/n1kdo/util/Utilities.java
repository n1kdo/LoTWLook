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

    // SimpleDateFormat is NOT thread-safe, and these formatters are used from the main
    // thread and background services. Give each thread its own instance.
    private static final ThreadLocal<DateFormat> LONG_DATE_FORMAT = ThreadLocal.withInitial(() -> {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter;
    });
    // All dates in this app are UTC; format in UTC, never device-local time.
    private static final ThreadLocal<DateFormat> SHORT_DATE_FORMAT = ThreadLocal.withInitial(() -> {
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
        return formatter;
    });

    public static String formatDate(Date date) {
        return (date == null) ? "" : LONG_DATE_FORMAT.get().format(date);
    }

    public static String formatShortDate(Date date) {
        return (date == null) ? "" : SHORT_DATE_FORMAT.get().format(date);
    }

    public static boolean notEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isEmptyString(String s) {
        return (s == null || s.isEmpty() || s.trim().isEmpty());
    }
}
