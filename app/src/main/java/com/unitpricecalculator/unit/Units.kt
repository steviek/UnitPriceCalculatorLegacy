package com.unitpricecalculator.unit

import androidx.collection.ArrayMap
import com.google.common.base.Function
import com.squareup.otto.Bus
import com.unitpricecalculator.currency.Currencies
import com.unitpricecalculator.events.UnitTypeChangedEvent
import com.unitpricecalculator.unit.Systems.Companion.includes
import com.unitpricecalculator.unit.UnitType.QUANTITY
import com.unitpricecalculator.util.isIntegral
import com.unitpricecalculator.util.logger.Logger
import com.unitpricecalculator.util.prefs.Keys
import com.unitpricecalculator.util.prefs.Prefs
import dagger.Reusable
import java.text.NumberFormat
import java.util.Currency
import javax.inject.Inject

/** Collection of utility functions for [Unit]. */
@Reusable
class Units @Inject internal constructor(
  private val prefs: Prefs,
  private val bus: Bus,
  private val systems: Systems
) {

  private var _currency: Currency? = null
  private var costFormatter: Function<Double, String>? = null

  var currentUnitType: UnitType
    get() = UnitType.valueOf(prefs.getNonNullString(Keys.UNIT_TYPE, UnitType.WEIGHT.name))
    set(unitType) {
      prefs.putString(Keys.UNIT_TYPE, unitType.name)
      bus.post(UnitTypeChangedEvent(unitType))
    }

  val formatter: Function<Double, String>
    get() = (this.costFormatter ?: createFormatter()).also { this.costFormatter = it }

  fun getUnitsForType(unitType: UnitType): List<DefaultUnit> {
    return unitMap.getOrPut(unitType) { DefaultUnit.values().filter { it.unitType == unitType } }
  }

  @JvmOverloads
  fun getIncludedUnitsForTypeSorted(
    unitType: UnitType,
    unitToAlwaysInclude: Unit? = null
  ): List<DefaultUnit> {
    val includedSystems = systems.includedSystems
    val preferredOrder = systems.preferredOrder
    return getUnitsForType(unitType)
      .filter {
        it == unitToAlwaysInclude || it.unitType == QUANTITY || includedSystems.includes(it.system)
      }
      .sortedBy {
        if (it.system == System.IMPERIAL) {
          if (preferredOrder[0] == System.METRIC) {
            1
          } else {
            0
          }
        } else {
          preferredOrder.indexOf(it.system)
        }
      }
  }

  var currency: Currency
    get() {
      _currency?.let { return it }

      costFormatter = null

      return (prefs.getString(KEY_CURRENCY)?.let { Currencies.parseCurrencySafely(it).orNull() }
        ?: Currencies.getSafeDefaultCurrency())
        .also {
          _currency = it
        }
    }
    set(value) {
      this._currency = value
      this.costFormatter = null
      prefs.putString(KEY_CURRENCY, value.currencyCode)
    }

  private fun createFormatter(): Function<Double, String> {
    val lotsOfDigits = NumberFormat.getCurrencyInstance()
    lotsOfDigits.currency = currency
    lotsOfDigits.minimumFractionDigits = 2
    lotsOfDigits.maximumFractionDigits = 8

    val noFractionFormat = NumberFormat.getCurrencyInstance()
    noFractionFormat.currency = currency
    noFractionFormat.minimumFractionDigits = 0
    noFractionFormat.maximumFractionDigits = 0

    val defaultForCurrency = NumberFormat.getCurrencyInstance()
    defaultForCurrency.currency = currency

    return Function { input ->
      if (input!!.isIntegral()) {
        return@Function noFractionFormat.format(input)
      }

      var formattedPricePer = lotsOfDigits.format(input)

      val parts =
        formattedPricePer.split("[.,]".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
      if (parts.size != 2) {
        return@Function formattedPricePer
      }

      // Attempt to account for rounding errors and excessive digits by checking for repeating
      // digits after the decimal.
      val decimalPortion = parts[1]
      var longestRunLength = 1
      var currentRunLength = 1
      var lastDigit = decimalPortion[0]
      for (i in 1 until decimalPortion.length) {
        if (decimalPortion[i] == lastDigit) {
          currentRunLength += 1
          if (currentRunLength > longestRunLength) {
            longestRunLength = currentRunLength
          }
        } else {
          currentRunLength = 1
          lastDigit = decimalPortion[i]
        }
      }

      if (longestRunLength >= 4) {
        formattedPricePer = defaultForCurrency.format(input)
      }

      formattedPricePer
    }
  }

  fun getDefaultQuantity(): Quantity {
    return getDefaultQuantity(currentUnitType)
  }

  fun getDefaultQuantity(unitType: UnitType): Quantity {
    val amount = prefs.getDouble(getDefaultQuantityAmountKey(unitType))
    val unit = prefs.getString(getDefaultQuantityUnitKey(unitType))
    if (amount == null || unit == null) {
      val targetUnit = getIncludedUnitsForTypeSorted(unitType).first()
      return Quantity(targetUnit.defaultQuantity.toDouble(), targetUnit)
    }

    return Quantity(amount, DefaultUnit.valueOf(unit))
  }

  object DefaultQuantityChangedEvent

  fun setDefaultQuantity(unitType: UnitType, quantity: Quantity) {
    prefs.putDouble(getDefaultQuantityAmountKey(unitType), quantity.amount)
    prefs.putString(getDefaultQuantityUnitKey(unitType), quantity.unit.name)
    bus.post(DefaultQuantityChangedEvent)
    Logger.d("New default quantity: %s, %s", unitType.name, quantity.toString())
  }

  companion object {

    private const val KEY_CURRENCY = "currency"

    private val unitMap = ArrayMap<UnitType, List<DefaultUnit>>()

    private fun getDefaultQuantityAmountKey(unitType: UnitType): String {
      return "default_quantity_amount:${unitType.name}"
    }

    private fun getDefaultQuantityUnitKey(unitType: UnitType): String {
      return "default_quantity_unit:${unitType.name}"
    }
  }
}
