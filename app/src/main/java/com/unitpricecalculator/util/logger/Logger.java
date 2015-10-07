package com.unitpricecalculator.util.logger;

import android.util.Log;

import java.util.Locale;

/**
 * Convenience methods for writing to Logcat.
 */
public final class Logger {

    private static String tag;
    private static boolean shouldLog;

    public static void initialize(String tag, boolean shouldLog) {
        Logger.tag = tag;
        Logger.shouldLog = shouldLog;
    }

    public static void d(String format, Object... args) {
        if (shouldLog()) {
            Log.d(tag, String.format(Locale.getDefault(), format, args));
        }
    }

    public static void e(String format, Object... args) {
        if (shouldLog()) {
            Log.e(tag, String.format(Locale.getDefault(), format, args));
        }
    }

    public static void e(Throwable e) {
        if (shouldLog()) {
            Log.e(tag, e.toString());
        }
    }

    public static void i(String format, Object... args) {
        if (shouldLog()) {
            Log.i(tag, String.format(Locale.getDefault(), format, args));
        }
    }

    public static void v(String format, Object... args) {
        if (shouldLog()) {
            Log.v(tag, String.format(Locale.getDefault(), format, args));
        }
    }

    public static void w(String format, Object... args) {
        if (shouldLog()) {
            Log.w(tag, String.format(Locale.getDefault(), format, args));
        }
    }

    private static boolean shouldLog() {
        return false;
        /*if (tag == null) {
            throw new IllegalStateException("Logger not initialized!");
        }
        return shouldLog;*/
    }

}
