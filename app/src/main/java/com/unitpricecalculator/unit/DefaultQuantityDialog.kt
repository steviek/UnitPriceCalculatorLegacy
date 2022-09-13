@file:JvmName("DefaultQuantityDialog")

package com.unitpricecalculator.unit

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.WindowManager.LayoutParams
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.unitpricecalculator.BaseDialogFragment
import com.unitpricecalculator.R
import com.unitpricecalculator.locale.AppLocaleManager
import com.unitpricecalculator.locale.currentLocale
import com.unitpricecalculator.util.*
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class DefaultQuantityDialogFragment : BaseDialogFragment() {

  @Inject lateinit var units: Units
  @Inject lateinit var appLocaleManager: AppLocaleManager

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context = requireContext()
    val arguments = requireArguments()
    val unit = DefaultUnit.valueOf(arguments.getString(KEY_QUANTITY_UNIT)!!)
    val amount = arguments.getDouble(KEY_QUANTITY_AMOUNT)
    val view = LayoutInflater.from(context).inflate(R.layout.default_quantity_dialog_view, null)

    val amountEditText = view.findViewById<EditText>(R.id.amount_edit_text)
    amountEditText.setText(amount.toLocalizedString())
    amountEditText.addLocalizedKeyListener()

    val unitSpinner = view.findViewById<Spinner>(R.id.unit_spinner)
    val unitsInSpinner =
      units.getIncludedUnitsForTypeSorted(unit.unitType, unit)
    unitSpinner.adapter =
      ArrayAdapter<String>(
        context,
        R.layout.unit_spinner_dropdown_item,
        unitsInSpinner.map { it.getSymbol(context.resources) }
      )
    unitSpinner.setSelection(unitsInSpinner.indexOf(unit))

    val inputMethodManager =
      context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    return MaterialAlertDialogBuilder(context)
      .setTitle(
        resources.getString(
          R.string.default_quantity_dialog_header,
          resources.getString(unit.unitType.labelResId)
            .lowercase(appLocaleManager.currentLocale)
        )
      )
      .setView(view)
      .setPositiveButton(android.R.string.ok) { _, _ ->
        val newAmount = amountEditText.text.toString().parseDoubleSafely()
        if (newAmount.isNotPresent) return@setPositiveButton
        val newUnit = unitsInSpinner[unitSpinner.selectedItemPosition]
        units.setDefaultQuantity(newUnit.unitType, Quantity(newAmount.get(), newUnit))
        inputMethodManager.hideSoftInputFromWindow(amountEditText.windowToken, 0)
      }
      .setNegativeButton(android.R.string.cancel) { _, _ ->
        inputMethodManager.hideSoftInputFromWindow(amountEditText.windowToken, 0)
      }
      .create()
      .also { dialog ->
        dialog.window?.setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE)

        dialog.setOnShowListener {
          amountEditText.addTextChangedListener(object : AbstractTextWatcher() {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
              dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled =
                NumberUtils.parsesNicelyDouble(s.toString())
            }
          })
          amountEditText.requestFocus()
          amountEditText.selectAll()
        }
        dialog.setOnDismissListener {
          inputMethodManager.hideSoftInputFromWindow(amountEditText.windowToken, 0)
        }
      }
  }


  companion object {

    private const val TAG = "DefaultQuantityDialogFragment"
    private const val KEY_QUANTITY_UNIT = "key_quantity_unit"
    private const val KEY_QUANTITY_AMOUNT = "key_quantity_amount"

    @JvmStatic
    fun show(fragmentManager: FragmentManager, quantity: Quantity) {
      DefaultQuantityDialogFragment().withArguments {
        putString(KEY_QUANTITY_UNIT, quantity.unit.name)
        putDouble(KEY_QUANTITY_AMOUNT, quantity.amount)
      }.show(fragmentManager, TAG)
    }
  }
}