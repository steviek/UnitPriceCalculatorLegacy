@file:JvmName("MoreLocalization")

package com.unitpricecalculator.util

import android.annotation.TargetApi
import android.icu.text.DecimalFormat
import android.os.Build
import android.os.Build.VERSION_CODES
import java.util.Locale


@TargetApi(VERSION_CODES.N)
fun Double?.toLocalizedString(): String = when {
  this == null -> toString()
  isIntegral() -> toInt().toString()
  Build.VERSION.SDK_INT < VERSION_CODES.N -> toDouble().toString()
  else -> DecimalFormat.getInstance(Locale.getDefault()).format(this)
}