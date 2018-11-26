package com.unitpricecalculator.util.prefs;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import com.google.common.base.Throwables;
import com.unitpricecalculator.inject.ApplicationContext;
import com.unitpricecalculator.json.ObjectMapper;
import dagger.Reusable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

@Reusable
public final class Prefs {

    private final ObjectMapper objectMapper;
    private final SharedPreferences prefs;

    @Inject
    Prefs(@ApplicationContext Context context, ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.prefs = context.getSharedPreferences(context.getPackageName() + "_prefs",
                Context.MODE_MULTI_PROCESS);
    }

    public String getString(String key) {
        return getString(key, null);
    }

    public String getString(String key, String fallback) {
        return prefs.getString(key, fallback);
    }

    public void putString(String key, String string) {
        prefs.edit().putString(key, string).apply();
    }


    public Set<String> getStringSet(String key, Set<String> fallback) {
        return prefs.getStringSet(key, fallback);
    }

    public void putStringSet(String key, Set<String> set) {
        prefs.edit().putStringSet(key, set).apply();
    }

    public <T> List<T> getList(Class<T> clazz, String key) {
        return getList(clazz, key, new ArrayList<T>());
    }

    public <T> List<T> getList(Class<T> clazz, String key, List<T> fallback) {
        Set<String> set = getStringSet(key, null);
        Log.d("Stevie", "trying to get list: " + set);
        if (set == null) {
            return fallback;
        } else {
            try {
                List<T> list = new ArrayList<>();
                for (String s : set) {
                    list.add(objectMapper.fromJson(clazz, s));
                }
                return list;
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
        }
    }

    public <T> void putList(String key, List<T> list) {
        Set<String> stringSet = new HashSet<>();
        try {
            for (T t : list) {
                stringSet.add(objectMapper.toJson(t));
            }
            putStringSet(key, stringSet);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public <T> void addToList(Class<T> clazz, String key, T object) {
        List<T> list = getList(clazz, key);
        list.add(object);
        putList(key, list);
    }

    public <T> T getObject(Class<T> clazz, String key) {
        return getObject(clazz, key, null);
    }

    public <T> T getObject(Class<T> clazz, String key, T fallback) {
        return objectMapper.fromJsonOptional(clazz, getString(key)).or(fallback);
    }

    public void putObject(String key, Object object) {
        prefs.edit().putString(key, objectMapper.toJson(object)).apply();
    }

    public boolean getBoolean(String key) {
        return prefs.getBoolean(key, false);
    }

    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
    }

    public void remove(String key) {
        prefs.edit().remove(key).apply();
    }
}
