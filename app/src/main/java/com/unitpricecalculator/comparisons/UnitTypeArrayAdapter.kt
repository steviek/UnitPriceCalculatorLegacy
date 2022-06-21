package com.unitpricecalculator.comparisons

import android.app.Activity
import android.widget.ArrayAdapter
import com.unitpricecalculator.R
import com.unitpricecalculator.unit.UnitType
import javax.inject.Inject

class UnitTypeArrayAdapterFactory @Inject constructor(private val activity: Activity) {
    fun create(): ArrayAdapter<String> {
        return ArrayAdapter(
            activity,
            R.layout.unit_type_spinner_dropdown_item,
            UnitType.values().map { it.loadLabel(activity) }
        ).also {
            it.filter.filter(null)
        }
    }
}
