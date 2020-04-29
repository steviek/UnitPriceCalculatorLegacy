package com.unitpricecalculator.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.common.base.Optional

fun Double.isIntegral() = toInt().toDouble() == this


inline fun <T : Fragment> T.withArguments(modifier: Bundle.() -> Unit): T {
  arguments = Bundle().apply(modifier)
  return this
}

val Optional<*>.isNotPresent: Boolean
  get() = !isPresent
