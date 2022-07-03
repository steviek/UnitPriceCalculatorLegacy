package com.unitpricecalculator.dialog

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.FragmentManager
import com.unitpricecalculator.BaseDialogFragment
import com.unitpricecalculator.dialog.DialogId.ALL_DEFAULT_QUANTITIES
import com.unitpricecalculator.dialog.DialogId.INITIAL_SCREEN_DIALOG
import com.unitpricecalculator.dialog.DialogId.LOCALE_DIALOG
import com.unitpricecalculator.dialog.DialogId.UNIT_SYSTEMS
import com.unitpricecalculator.initialscreen.InitialScreenDialog
import com.unitpricecalculator.locale.AppLocaleDialog
import com.unitpricecalculator.settings.AllDefaultQuantitiesDialog
import com.unitpricecalculator.unit.UnitSystemsDialog
import dagger.MapKey
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import javax.inject.Provider

@AndroidEntryPoint
class DelegatingDialogFragment : BaseDialogFragment() {

  @Inject lateinit var initialScreenDialog: Provider<InitialScreenDialog>
  @Inject lateinit var localeDialog: Provider<AppLocaleDialog>
  @Inject lateinit var unitSystemsDialog: Provider<UnitSystemsDialog>
  @Inject lateinit var allDefaultQuantitiesDialog: Provider<AllDefaultQuantitiesDialog>

  override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
    val context = requireActivity()
    val args = requireArguments()
    val delegate = when (DialogId.valueOf(args.getString(KEY_DIALOG_ID)!!)) {
      INITIAL_SCREEN_DIALOG -> initialScreenDialog.get()
      LOCALE_DIALOG -> localeDialog.get()
      UNIT_SYSTEMS -> unitSystemsDialog.get()
      ALL_DEFAULT_QUANTITIES -> allDefaultQuantitiesDialog.get()
    }
    val extras = args.getBundle(KEY_EXTRAS)
    return delegate.createDialog(context, extras)
  }

  companion object {
    private const val KEY_DIALOG_ID = "key_id"
    private const val KEY_EXTRAS = "key_extras"

    @JvmOverloads
    fun show(
      fragmentManager: FragmentManager,
      id: DialogId,
      populateExtras: (Bundle.() -> Unit)? = null
    ) {
      val bundle = Bundle()
      bundle.putString(KEY_DIALOG_ID, id.name)
      populateExtras?.let { bundle.putBundle(KEY_EXTRAS, Bundle().apply(populateExtras)) }
      DelegatingDialogFragment().also { it.arguments = bundle }
        .showNow(fragmentManager, /* tag= */ null)
    }
  }
}

interface DialogDelegate {
  fun createDialog(context: Context, extras: Bundle?): Dialog
}

enum class DialogId {
  INITIAL_SCREEN_DIALOG, LOCALE_DIALOG, UNIT_SYSTEMS, ALL_DEFAULT_QUANTITIES
}
