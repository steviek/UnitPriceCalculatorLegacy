package com.unitpricecalculator.mode

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.squareup.otto.Bus
import com.unitpricecalculator.BaseDialogFragment
import com.unitpricecalculator.R
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DarkModeDialogFragment : BaseDialogFragment() {

  @Inject lateinit var bus: Bus
  @Inject lateinit var manager: DarkModeManager

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context  = requireContext()
    val states = DarkModeState.values().sortedBy { it.index }
    val stateLabels = states.map { context.getString(it.labelResId) }.toTypedArray()
    val selectedState = states.indexOf(manager.currentDarkModeState)
    return AlertDialog.Builder(context)
        .setTitle(R.string.dark_mode)
        .setSingleChoiceItems(stateLabels, selectedState) { dialog, which ->
          manager.currentDarkModeState = states[which]
          dialog.dismiss()
        }
        .create()
  }

  companion object {

    private const val TAG = "DarkModeDialogFragment"

    @JvmStatic
    fun show(fragmentManager: FragmentManager) {
      DarkModeDialogFragment().show(fragmentManager, TAG)
    }
  }
}

