package com.unitpricecalculator.locale

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import com.unitpricecalculator.R
import com.unitpricecalculator.dialog.DialogDelegate
import java.text.Collator
import javax.inject.Inject

class AppLocaleDialog @Inject internal constructor(
  private val manager: AppLocaleManager
): DialogDelegate {
  override fun createDialog(context: Context, extras: Bundle?): Dialog {
    val collator = Collator.getInstance()
    val localesSortedByDisplayName = AppLocale.values().sortedWith(object: Comparator<AppLocale> {
      override fun compare(o1: AppLocale, o2: AppLocale): Int {
        return when {
          o1 == AppLocale.MATCH_DEVICE -> -1
          o2 == AppLocale.MATCH_DEVICE -> 1
          else -> {
            collator.compare(o1.getDisplayName(context), o2.getDisplayName(context))
          }
        }
      }
    })

    val displayNames = localesSortedByDisplayName.map { it.getDisplayName(context) }.toTypedArray()
    val currentIndex = localesSortedByDisplayName.indexOf(manager.current)

    return AlertDialog.Builder(context)
      .setTitle(R.string.language_title)
      .setSingleChoiceItems(
        displayNames,
        currentIndex
      ) { dialog, which ->
        manager.current = localesSortedByDisplayName[which]
        dialog.dismiss()
      }
      .create()
  }
}