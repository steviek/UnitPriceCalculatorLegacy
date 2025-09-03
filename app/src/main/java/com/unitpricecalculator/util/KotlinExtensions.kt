package com.unitpricecalculator.util

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.common.base.Optional

fun Double.isIntegral() = toInt().toDouble() == this

inline fun <reified T> Any.takeIfIsInstance(): T? = if (this is T) this else null

inline fun <T : Fragment> T.withArguments(modifier: Bundle.() -> Unit): T {
  arguments = Bundle().apply(modifier)
  return this
}

val Optional<*>.isNotPresent: Boolean
  get() = !isPresent

inline fun <T, C: Comparable<C>> Iterable<T>.isSortedBy(
  descending: Boolean = false,
  crossinline mapper: (T) -> C
): Boolean {
  return map(mapper).isSorted(descending)
}

fun <T: Comparable<T>> Iterable<T>.isSorted(descending: Boolean = false): Boolean {
  var lastValue: T? = null
  forEach {
    lastValue?.let { last ->
      if ((last > it && !descending) || (last < it && descending)) return false
    }
    lastValue = it
  }
  return true
}

inline fun <T, R : Comparable<R>> Iterable<T>.minByOrNullNotNull(selector: (T) -> R?): T? {
  var min: T? = null
  var minValue: R? = null
  for (item in this) {
    val value = selector(item) ?: continue
    if (minValue == null || value < minValue) {
      min = item
      minValue = value
    }
  }
  return min
}
