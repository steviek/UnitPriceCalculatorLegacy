@file:JvmName("MoreLocalization")

package com.unitpricecalculator.util

import android.annotation.TargetApi
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Build.VERSION_CODES
import com.unitpricecalculator.locale.AppLocaleManager
import java.util.Locale


@TargetApi(VERSION_CODES.N)
fun Double?.toLocalizedString(): String = when {
  this == null -> toString()
  isIntegral() -> String.format(AppLocaleManager.getInstance().currentLocale, "%d", toInt())
  Build.VERSION.SDK_INT < VERSION_CODES.N -> toDouble().toString()
  else -> DecimalFormat.getInstance(AppLocaleManager.getInstance().currentLocale).format(this)
}