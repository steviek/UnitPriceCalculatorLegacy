package com.unitpricecalculator.util.logger;

import android.util.Log;

import com.unitpricecalculator.BuildConfig;
import java.util.Locale;

/**
 * Convenience methods for writing to Logcat.
 */
public final class Logger {

    private static final String TAG = "UnitPriceCalculator";
    private static final boolean SHOULD_LOG = BuildConfig.DEBUG;

    public static void d(String format, Object... args) {
        if (shouldLog()) {
            Log.d(TAG, String.format(Locale.US, format, args));
        }
    }

    public static void e(String format, Object... args) {
        if (shouldLog()) {
            Log.e(TAG, String.format(Locale.US, format, args));
        }
    }

    public static void e(Throwable e) {
        if (shouldLog()) {
            Log.e(TAG, e.toString());
        }
    }

    public static void i(String format, Object... args) {
        if (shouldLog()) {
            Log.i(TAG, String.format(Locale.US, format, args));
        }
    }

    public static void v(String format, Object... args) {
        if (shouldLog()) {
            Log.v(TAG, String.format(Locale.US, format, args));
        }
    }

    public static void w(String format, Object... args) {
        if (shouldLog()) {
            Log.w(TAG, String.format(Locale.US, format, args));
        }
    }

    private static boolean shouldLog() {
        return SHOULD_LOG;
    }

}
