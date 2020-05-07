package com.unitpricecalculator.util.prefs

import android.content.Context
import android.content.SharedPreferences
import com.unitpricecalculator.inject.ApplicationContext
import com.unitpricecalculator.json.ObjectMapper
import dagger.Reusable
import java.util.ArrayList
import java.util.HashSet
import javax.inject.Inject

@Reusable
class Prefs @Inject internal constructor(
  @ApplicationContext context: Context,
  private val objectMapper: ObjectMapper
) {

  private val prefs: SharedPreferences =
    context.getSharedPreferences(context.packageName + "_prefs", Context.MODE_PRIVATE)

  fun getInt(key: String): Int? {
    if (prefs.contains(key)) {
      return prefs.getInt(key, -1)
    } else {
      return null
    }
  }

  fun getInt(key: String, fallback: Int): Int {
    return prefs.getInt(key, fallback)
  }

  fun putInt(key: String, value: Int?) {
    prefs.edit()
      .also {
        if (value == null) {
          it.remove(key)
        } else {
          it.putInt(key, value)
        }
      }.apply()
  }

  fun getDouble(key: String) = if (prefs.contains(key)) getDouble(key, -1.0) else null

  fun getDouble(key: String, fallback: Double): Double {
    return Double.fromBits(prefs.getLong(key, fallback.toRawBits()))
  }

  fun putDouble(key: String, value: Double) {
    prefs.edit().putLong(key, value.toRawBits()).apply()
  }

  @JvmOverloads
  fun getString(key: String, fallback: String? = null): String? {
    return prefs.getString(key, fallback)
  }

  fun getNonNullString(key: String, fallback: String): String {
    return prefs.getString(key, fallback)!!
  }

  fun putString(key: String, string: String) {
    prefs.edit().putString(key, string).apply()
  }


  fun getStringSet(key: String, fallback: Set<String>?): Set<String>? {
    return prefs.getStringSet(key, fallback)
  }

  fun putStringSet(key: String, set: Set<String>) {
    prefs.edit().putStringSet(key, set).apply()
  }

  fun <T> getList(clazz: Class<T>, key: String): List<T> {
    return getList(clazz, key, ArrayList())
  }

  fun <T> getList(clazz: Class<T>, key: String, fallback: List<T>): List<T> {
    val set = getStringSet(key, null)
    return if (set == null) {
      fallback
    } else {
      try {
        val list = ArrayList<T>()
        for (s in set) {
          list.add(objectMapper.fromJson(clazz, s))
        }
        list
      } catch (e: Exception) {
        throw RuntimeException(e)
      }

    }
  }

  fun <T> putList(key: String, list: List<T>) {
    val stringSet = HashSet<String>()
    try {
      for (t in list) {
        stringSet.add(objectMapper.toJson(t))
      }
      putStringSet(key, stringSet)
    } catch (e: Exception) {
      throw RuntimeException(e)
    }

  }

  fun <T> addToList(clazz: Class<T>, key: String, value: T) {
    val list = getList(clazz, key).toMutableList()
    list.add(value)
    putList(key, list)
  }

  fun <T> getObject(clazz: Class<T>, key: String): T {
    return getObject(clazz, key, null)
  }

  fun <T> getObject(clazz: Class<T>, key: String, fallback: T?): T {
    return objectMapper.fromJsonOptional(clazz, getString(key)).or(fallback!!)
  }

  fun putObject(key: String, `object`: Any) {
    prefs.edit().putString(key, objectMapper.toJson(`object`)).apply()
  }

  fun getBoolean(key: String): Boolean {
    return prefs.getBoolean(key, false)
  }

  fun putBoolean(key: String, value: Boolean) {
    prefs.edit().putBoolean(key, value).apply()
  }

  fun remove(key: String) {
    prefs.edit().remove(key).apply()
  }
}