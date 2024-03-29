@file:JvmName("Localization")

package com.unitpricecalculator.util

import android.icu.text.Normalizer2
import android.os.Build.VERSION
import android.text.Editable
import android.widget.EditText
import com.google.common.base.Optional
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher
import java.text.DecimalFormatSymbols
import java.text.Normalizer
import java.text.Normalizer.Form.NFD

fun EditText.addLocalizedKeyListener() {
  val localeSeparator = DecimalFormatSymbols.getInstance().monetaryDecimalSeparator
  if (localeSeparator == '.') return

  this.keyListener = LocalizedDigitsKeyListener.create()
  addTextChangedListener(object : AbstractTextWatcher() {
    override fun afterTextChanged(s: Editable) {
      var alreadyContainsSeparator = false
      for (i in s.indices) {
        val c = s[i]
        if (c == '.' || c == ',' || c == localeSeparator) {
          alreadyContainsSeparator = if (alreadyContainsSeparator) {
            s.replace(i, i + 1, "")
            break
          } else {
            true
          }
        }
      }
    }
  })
}

fun String.parseDoubleOrThrow(): Double {
  return try {
    parseDoubleSafely().get()
  } catch (e: Exception) {
    throw NullPointerException()
  }
}

fun String.parseDoubleSafely(): Optional<Double> {
  val separator = firstOrNull { !it.isDigit() } ?: return this.maybeParseDouble()
  return this.replace(separator, '.').maybeParseDouble()
}

fun String.parseDoubleOrNull(): Double? {
  val separator = firstOrNull { !it.isDigit() } ?: return this.maybeParseDouble().orNull()
  return this.replace(separator, '.').maybeParseDouble().orNull()
}

private fun String.maybeParseDouble(): Optional<Double> {
  return Optional.fromNullable(toDoubleOrNull())
}

fun String.stripAccents(): String {
  val normalizedForm = if (VERSION.SDK_INT >= 24) {
    Normalizer2.getNFDInstance().normalize(this)
  } else {
    Normalizer.normalize(this, NFD)
  }
  return normalizedForm.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
}