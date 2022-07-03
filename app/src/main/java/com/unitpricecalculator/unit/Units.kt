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
import kotlin.math.pow

/** Collection of utility functions for [Unit]. */
@Reusable
class Units @Inject internal constructor(
    private val prefs: Prefs,
    private val bus: Bus,
    private val systems: Systems
) {

    private val currencyToFormatter = mutableMapOf<String, CostFormatter>()
    private var _currency: Currency? = null

    var currentUnitType: UnitType
        get() = UnitType.valueOf(prefs.getNonNullString(Keys.UNIT_TYPE, UnitType.WEIGHT.name))
        set(unitType) {
            prefs.putString(Keys.UNIT_TYPE, unitType.name)
            bus.post(UnitTypeChangedEvent(unitType))
        }

    val formatter: CostFormatter
        get() = getCostFormatter()

    fun getCostFormatter(currency: Currency? = null): CostFormatter {
        val effectiveCurrency = currency ?: this.currency
        return currencyToFormatter.getOrPut(effectiveCurrency.symbol) {
            createFormatter(effectiveCurrency)
        }
    }

    fun getUnitsForType(unitType: UnitType): List<DefaultUnit> {
        return unitMap.getOrPut(unitType) {
            DefaultUnit.values().filter { it.unitType == unitType }
        }
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

            return (prefs.getString(KEY_CURRENCY)
                ?.let { Currencies.parseCurrencySafely(it).orNull() }
                ?: Currencies.getSafeDefaultCurrency())
                .also {
                    _currency = it
                }
        }
        set(value) {
            this._currency = value
            prefs.putString(KEY_CURRENCY, value.currencyCode)
        }

    private fun createFormatter(currency: Currency): CostFormatter {
        val noFractionFormat = NumberFormat.getCurrencyInstance()
        noFractionFormat.currency = currency
        noFractionFormat.minimumFractionDigits = 0
        noFractionFormat.maximumFractionDigits = 0

        val defaultForCurrency = NumberFormat.getCurrencyInstance()
        defaultForCurrency.currency = currency

        return CostFormatter { input ->
            if (input.isIntegral()) {
                return@CostFormatter noFractionFormat.format(input)
            }

            val lotsOfDigits = NumberFormat.getCurrencyInstance()
            lotsOfDigits.currency = currency
            lotsOfDigits.minimumFractionDigits = maxOf(lotsOfDigits.minimumFractionDigits, 2)

            val firstSignificantFractionIndex = input.firstSignificantFractionIndex
            if (firstSignificantFractionIndex >= 0) {
                lotsOfDigits.maximumFractionDigits =
                    minOf(
                        8, maxOf(
                            lotsOfDigits.maximumFractionDigits,
                            input.firstSignificantFractionIndex + 4
                        )
                    )
            } else {
                lotsOfDigits.maximumFractionDigits = lotsOfDigits.minimumFractionDigits
            }

            lotsOfDigits.format(input)
        }
    }

    private val Double.firstSignificantFractionIndex: Int
        get() = ((0 until 9).firstOrNull { this >= 10.0.pow(-it.toDouble()) } ?: 9) - 1

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

    fun interface CostFormatter {
        fun format(cost: Double): String
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
