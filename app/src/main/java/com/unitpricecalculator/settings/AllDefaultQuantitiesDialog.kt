package com.unitpricecalculator.settings

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.squareup.otto.Bus
import com.squareup.otto.Subscribe
import com.unitpricecalculator.R
import com.unitpricecalculator.databinding.AllDefaultQuantitiesDialogViewBinding
import com.unitpricecalculator.databinding.ViewDefaultQuantityRowBinding
import com.unitpricecalculator.dialog.DialogDelegate
import com.unitpricecalculator.unit.DefaultQuantityDialogFragment
import com.unitpricecalculator.unit.UnitType
import com.unitpricecalculator.unit.Units
import com.unitpricecalculator.unit.Units.DefaultQuantityChangedEvent
import javax.inject.Inject

class AllDefaultQuantitiesDialog @Inject constructor(
    private val fragment: Fragment,
    private val units: Units,
    private val bus: Bus
): DialogDelegate {

    private val defaultQuantityRowViews = mutableMapOf<UnitType, DefaultQuantityRowView>()

    override fun createDialog(context: Context, extras: Bundle?): Dialog {
        val inflater = LayoutInflater.from(context)
        val view = AllDefaultQuantitiesDialogViewBinding.inflate(inflater).root

        UnitType.values().forEach { unitType ->
            val rowView = ViewDefaultQuantityRowBinding.inflate(inflater, view, false).root
            rowView.setData(units.getDefaultQuantity(unitType))
            rowView.setOnClickListener {
                DefaultQuantityDialogFragment.show(
                    fragment.childFragmentManager,
                    units.getDefaultQuantity(unitType)
                )
            }
            view.addView(rowView)
            defaultQuantityRowViews[unitType] = rowView
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.default_unit_quantity)
            .setView(view)
            .create()
            .also { dialog ->
                dialog.setOnShowListener {
                    bus.register(this)
                }
                dialog.setOnDismissListener {
                    defaultQuantityRowViews.clear()
                    bus.unregister(this)
                }
            }
    }

    @Subscribe
    fun onDefaultQuantityChanged(event: DefaultQuantityChangedEvent) {
        defaultQuantityRowViews.forEach { (unit, row) ->
            row.setData(units.getDefaultQuantity(unit))
        }
    }
}