package com.unitpricecalculator.comparisons

import android.os.Parcelable
import com.unitpricecalculator.currency.Currencies
import com.unitpricecalculator.unit.DefaultUnit
import com.unitpricecalculator.unit.UnitType
import com.unitpricecalculator.unit.Units
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import java.util.Currency

@Parcelize
data class SavedComparison(
  private val _key: String?,
  val name: String,
  val unitType: UnitType,
  val savedUnitEntryRows: List<SavedUnitEntryRow>,
  val finalQuantity: String,
  val finalUnit: DefaultUnit,
  val currencyCode: String?,
  val timestampMillis: Long?
) : Comparable<SavedComparison>, Parcelable {

  @IgnoredOnParcel
  val key = _key ?: run {
      var newKey = System.currentTimeMillis()
      if (newKey == lastKeyGenerated) {
        newKey++
      }
      lastKeyGenerated = newKey
      newKey.toString()
  }

  val currency: Currency?
    get() = Currencies.parseCurrencySafely(currencyCode).orNull()

  /**
   * Generates a copy of this comparison with the provided name.  The original object is unaffected.
   */
  fun rename(newName: String) = copy(name = newName)

  /**
   * Generates a copy of this comparison with the provided name.  The original object is unaffected.
   */
  fun addCurrency(currencyCode: String) = copy(currencyCode = currencyCode)

  /**
   * Generates a copy of this comparison with the provided timestamp.  The original object is
   * unaffected.
   */
  fun withTimestamp(timestamp: Long) = copy(timestampMillis = timestamp)

  override fun compareTo(other: SavedComparison) = when {
    timestampMillis != null && other.timestampMillis != null -> {
      timestampMillis.compareTo(other.timestampMillis)
    }
    timestampMillis != null -> 1
    other.timestampMillis != null -> -1
    else -> key.compareTo(other.key)
  }

  val isEmpty: Boolean
    get() = name.isBlank() && finalQuantity.isBlank() && savedUnitEntryRows.all { it.isEmpty }

  val isNotEmpty get() = !isEmpty

  companion object {
    private var lastKeyGenerated: Long = 0
  }
}
