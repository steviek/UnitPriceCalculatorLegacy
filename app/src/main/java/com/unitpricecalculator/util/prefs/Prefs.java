package com.unitpricecalculator.util.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class Prefs {

    private static SharedPreferences prefs;
    private static Map<Class<?>, StringSerializer<?>> serializers;
    private static Map<Class<?>, StringDeserializer<?>> deserializers;

    public static void initialize(Context context,
                                  Map<Class<?>, StringSerializer<?>> serializers,
                                  Map<Class<?>, StringDeserializer<?>> deserializers) {
        prefs = context.getSharedPreferences(context.getPackageName() + "_prefs",
                Context.MODE_MULTI_PROCESS);
        Prefs.serializers = serializers;
        Prefs.deserializers = deserializers;
    }

    private static void checkInit() {
        if (prefs == null) {
            throw new IllegalStateException("Prefs not initialized!");
        }
    }

    private static void apply(SharedPreferences.Editor editor) {
        if (Build.VERSION.SDK_INT >= 9) {
            editor.apply();
        } else {
            editor.commit();
        }
    }

    public static String getString(String key) {
        return getString(key, null);
    }

    public static String getString(String key, String fallback) {
        checkInit();
        return prefs.getString(key, fallback);
    }

    public static void putString(String key, String string) {
        checkInit();
        apply(prefs.edit().putString(key, string));
    }

    public static int getInt(String key) {
        return getInt(key, 0);
    }

    public static int getInt(String key, int fallback) {
        checkInit();
        return prefs.getInt(key, fallback);
    }

    public static void putInt(String key, int i) {
        checkInit();
        apply(prefs.edit().putInt(key, i));
    }

    public static float getFloat(String key) {
        return getFloat(key, 0f);
    }

    public static float getFloat(String key, float fallback) {
        checkInit();
        return prefs.getFloat(key, fallback);
    }

    public static void putFloat(String key, float f) {
        checkInit();
        apply(prefs.edit().putFloat(key, f));
    }

    public static long getLong(String key) {
        return getLong(key, 0);
    }

    public static long getLong(String key, long fallback) {
        checkInit();
        return prefs.getLong(key, fallback);
    }

    public static void putLong(String key, long l) {
        checkInit();
        apply(prefs.edit().putLong(key, l));
    }

    public static boolean getBoolean(String key) {
        return getBoolean(key, false);
    }

    public static boolean getBoolean(String key, boolean fallback) {
        checkInit();
        return prefs.getBoolean(key, fallback);
    }

    public static void putBoolean(String key, boolean b) {
        checkInit();
        apply(prefs.edit().putBoolean(key, b));
    }

    public static Set<String> getStringSet(String key) {
        return getStringSet(key, new HashSet<String>());
    }

    public static Set<String> getStringSet(String key, Set<String> fallback) {
        checkInit();
        if (Build.VERSION.SDK_INT >= 11) {
            return prefs.getStringSet(key, fallback);
        } else {
            String serializedSet = prefs.getString(key, null);
            if (serializedSet != null) {
                try {
                    JSONArray array = new JSONArray(serializedSet);
                    Set<String> set = new HashSet<>();
                    int len = array.length();
                    for (int i = 0; i < len; i++) {
                        set.add(array.getString(i));
                    }
                    return set;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            } else {
                return fallback;
            }
        }
    }

    public static void putStringSet(String key, Set<String> set) {
        if (Build.VERSION.SDK_INT >= 11) {
            prefs.edit().putStringSet(key, set).apply();
        } else {
            if (set != null) {
                JSONArray array = new JSONArray();
                for (String s : set) {
                    array.put(s);
                }
                apply(prefs.edit().putString(key, array.toString()));
            } else {
                apply(prefs.edit().putString(key, null));
            }
        }
    }

    public static <T> void putStringSerializable(Class<T> clazz, String key, T object) {
        StringSerializer<T> serializer = (StringSerializer<T>) serializers.get(clazz);
        apply(prefs.edit().putString(key, serializer.serialize(object)));
    }

    public static <T> T getStringSerializable(Class<T> clazz, String key) {
        StringDeserializer<T> deserializer = (StringDeserializer<T>) deserializers.get(clazz);
        String serialized = prefs.getString(key, null);
        if (serialized == null) {
            return null;
        } else {
            return deserializer.deserialize(serialized);
        }
    }
}
