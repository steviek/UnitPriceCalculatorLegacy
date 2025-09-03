package com.unitpricecalculator.comparisons

import com.google.common.truth.Truth.assertThat
import com.unitpricecalculator.unit.DefaultUnit
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

@RunWith(JUnit4::class)
class SavedUnitEntryRowTest {
    @Test
    fun pricePerBaseUnit() {
        val entryRow = SavedUnitEntryRow(
            cost = "4",
            quantity = "",
            size = "200",
            unit = DefaultUnit.MILLILITRE,
            note = null,
        )

        assertThat(entryRow.pricePerBaseUnit).isEqualTo(20)
    }

    @Test
    fun `pricePerBaseUnit with quantity`() {
        val entryRow = SavedUnitEntryRow(
            cost = "4",
            quantity = "2.5",
            size = "200",
            unit = DefaultUnit.MILLILITRE,
            note = null,
        )

        assertThat(entryRow.pricePerBaseUnit).isEqualTo(8)
    }
}