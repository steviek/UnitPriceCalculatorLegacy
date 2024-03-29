package com.unitpricecalculator.time

import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeHelper {

  fun toMonthDateString(timestamp: Long): String {
    val format = DateFormat.getBestDateTimePattern(Locale.getDefault(), "d MMM")
    return SimpleDateFormat(format).format(Date(timestamp))
  }
}