package com.unitpricecalculator.unit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.unitpricecalculator.R
import com.unitpricecalculator.databinding.ListDraggableBinding
import com.unitpricecalculator.databinding.UnitSystemsDialogViewBinding
import com.unitpricecalculator.dialog.DialogDelegate
import javax.inject.Inject

class UnitSystemsDialog @Inject constructor(
    private val systems: Systems
): DialogDelegate {
    override fun createDialog(context: Context, extras: Bundle?): Dialog {
        val layoutInflater = LayoutInflater.from(context)
        val dragLinearLayout = UnitSystemsDialogViewBinding.inflate(layoutInflater).root

        val includedSystems = HashSet(systems.includedSystems - System.NEUTRAL)
        for (system in systems.preferredOrder) {
            val rowViewBinding =
                ListDraggableBinding.inflate(layoutInflater, dragLinearLayout, false)
            val rowView = rowViewBinding.root
            val text = rowViewBinding.text
            text.text = context.getString(system.getName())
            rowView.tag = system
            dragLinearLayout.addDragView(rowView, rowViewBinding.handle)

            val checkBox = rowViewBinding.checkbox
            checkBox.isChecked = includedSystems.contains(system)
            text.setOnClickListener { checkBox.toggle() }
            checkBox.setOnCheckedChangeListener { compoundButton, isChecked ->
                val modificationRequired = isChecked != includedSystems.contains(system)
                if (!modificationRequired) {
                    return@setOnCheckedChangeListener
                }

                if (includedSystems.size == 1 && !isChecked) {
                    // If this is the last unit, don't allow it to be unchecked.
                    compoundButton.toggle()
                    return@setOnCheckedChangeListener
                }

                if (isChecked) {
                    includedSystems.add(system)
                } else {
                    includedSystems.remove(system)
                }
                systems.includedSystems = includedSystems
            }
        }

        dragLinearLayout.setOnViewSwapListener { _, firstPosition, _, secondPosition ->
            val order = systems.preferredOrder
            val temp = order[firstPosition]
            order[firstPosition] = order[secondPosition]
            order[secondPosition] = temp
            systems.preferredOrder = order
        }

        return MaterialAlertDialogBuilder(context)
            .setTitle(R.string.preferred_system)
            .setView(dragLinearLayout)
            .create()
    }
}