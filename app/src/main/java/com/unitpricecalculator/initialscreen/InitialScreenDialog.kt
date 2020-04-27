package com.unitpricecalculator.initialscreen

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.unitpricecalculator.R
import com.unitpricecalculator.dialog.DialogDelegate
import javax.inject.Inject

class InitialScreenDialog @Inject internal constructor(
  private val manager: InitialScreenManager
) : DialogDelegate {
  override fun createDialog(context: Context, extras: Bundle?): Dialog {
    val initialScreens = InitialScreen.values()
    val initialScreenLabels = initialScreens.map { context.getString(it.labelResId) }
    val currentInitialScreenIndex = initialScreens.indexOf(manager.initialScreen)
    return AlertDialog.Builder(context)
      .setTitle(R.string.initial_screen)
      .setSingleChoiceItems(
        initialScreenLabels.toTypedArray(),
        currentInitialScreenIndex
      ) { dialog, which ->
        manager.initialScreen = initialScreens[which]
        dialog.dismiss()
      }
      .create()
  }
}