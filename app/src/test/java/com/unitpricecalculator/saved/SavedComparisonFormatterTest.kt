package com.unitpricecalculator.saved

import com.google.common.truth.Truth.assertThat
import com.unitpricecalculator.comparisons.SavedUnitEntryRow
import com.unitpricecalculator.unit.DefaultUnit
import com.unitpricecalculator.unit.Quantity
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Currency
import javax.inject.Inject

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33], application = HiltTestApplication::class)
class SavedComparisonFormatterTest {
    @get:Rule
    val hiltRule: HiltAndroidRule = HiltAndroidRule(this)

    @Inject
    lateinit var formatter: SavedComparisonFormatter

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun formatting() {
        val row = createRow(cost = "2", size = "3", unit = DefaultUnit.OUNCE)

        assertThat(formatter.formatSummary(row, USD)).isEqualTo("Best item: $2 per 3 oz")
    }

    private fun createRow(
        cost: String = "1.0",
        quantity: String = "",
        size: String = "1",
        unit: DefaultUnit = DefaultUnit.METRE
    ): SavedUnitEntryRow {
        return SavedUnitEntryRow(
            cost = cost,
            quantity = quantity,
            size = size,
            unit = unit,
            note = null,
        )
    }

    private companion object {
        val USD = Currency.getInstance("USD")!!
    }
}
