package com.unitpricecalculator.comparisons

import android.content.Context
import com.unitpricecalculator.unit.DefaultUnit
import com.unitpricecalculator.unit.Systems
import com.unitpricecalculator.unit.UnitType
import com.unitpricecalculator.unit.Units
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject
import javax.inject.Provider

class UnitArrayAdapterFactory @Inject constructor(
    @ActivityContext private val context: Provider<Context>,
    private val systems: Provider<Systems>,
    private val units: Provider<Units>
){
    fun create(unitType: UnitType): UnitArrayAdapter {
        return UnitArrayAdapter(context.get(), systems.get(), units.get(), unitType)
    }

    fun create(selected: DefaultUnit): UnitArrayAdapter {
        return UnitArrayAdapter(context.get(), systems.get(), units.get(), selected)
    }
}
