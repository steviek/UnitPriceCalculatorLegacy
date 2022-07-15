package com.unitpricecalculator.locale

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import com.unitpricecalculator.R
import java.util.*

enum class AppLocale(val number: Int, private val localeSupplier: () -> Locale) {
  MATCH_DEVICE(1, {Locale.getDefault()}),
  EN(2, "en"),
  DE(3, "de"),
  ES(4, "es"),
  FR(5, "fr"),
  PT(6, "pt"),
  RO(7, "ro"),
  AR(8, "ar");

  constructor(number: Int, languageCode: String) : this(
    number,
    {Locale(languageCode, Locale.getDefault().country)}
  )

  fun getDisplayName(context: Context): String {
    return if (this == MATCH_DEVICE) {
      context.getString(R.string.match_device)
    } else {
      toLocale().getDisplayLanguage(context.resources.configuration.locale)
    }
  }

  fun toLocale(): Locale {
    return localeSupplier()
  }

  fun apply(context: Context): Context {
    if (this == MATCH_DEVICE) return context
    val newConfiguration = Configuration().also { it.setLocale(toLocale()) }
    return context.createConfigurationContext(newConfiguration)
  }

  companion object {
    fun forNumber(number: Int): AppLocale {
      return values().firstOrNull { it.number == number } ?: MATCH_DEVICE
    }
  }
}