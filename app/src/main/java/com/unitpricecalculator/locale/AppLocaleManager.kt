package com.unitpricecalculator.locale

import androidx.fragment.app.FragmentManager
import com.squareup.otto.Bus
import com.unitpricecalculator.dialog.DelegatingDialogFragment
import com.unitpricecalculator.dialog.DialogId.LOCALE_DIALOG
import com.unitpricecalculator.events.AppLocaleChangedEvent
import com.unitpricecalculator.util.prefs.Prefs
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLocaleManager @Inject constructor(
  private val prefs: Prefs,
  private val bus: Bus
) {

  init {
    _instance = this
  }

  private var _current: AppLocale? = null

  var current: AppLocale
    get() {
      _current?.let { return it }
      return AppLocale.forNumber(prefs.getInt(PREFS_KEY, AppLocale.MATCH_DEVICE.number)).also {
        _current = it
      }
    }
    set(value) {
      if (value == _current) return
      _current = value
      prefs.putInt(PREFS_KEY, value.number)
      bus.post(AppLocaleChangedEvent)
    }

  val currentLocale: Locale
    get() = current.toLocale()

  fun showSelectionDialog(fragmentManager: FragmentManager) {
    DelegatingDialogFragment.show(fragmentManager, LOCALE_DIALOG)
  }

  companion object {

    private const val PREFS_KEY = "app_locale"

    private var _instance: AppLocaleManager? = null

    @JvmStatic
    fun getInstance(): AppLocaleManager {
      return _instance!!
    }
  }
}