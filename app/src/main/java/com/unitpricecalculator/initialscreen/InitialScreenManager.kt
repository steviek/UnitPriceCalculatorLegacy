package com.unitpricecalculator.initialscreen

import androidx.fragment.app.FragmentManager
import com.squareup.otto.Bus
import com.unitpricecalculator.dialog.DelegatingDialogFragment
import com.unitpricecalculator.dialog.DialogId.INITIAL_SCREEN_DIALOG
import com.unitpricecalculator.initialscreen.InitialScreen.NEW_COMPARISON
import com.unitpricecalculator.util.prefs.Keys
import com.unitpricecalculator.util.prefs.Prefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InitialScreenManager @Inject internal constructor(
  private val prefs: Prefs,
  private val bus: Bus
) {

  private var _initialScreen: InitialScreen? = null

  var initialScreen: InitialScreen
    get() {
      _initialScreen?.let { return it }
      val prefsValue = prefs.getInt(Keys.INITIAL_SCREEN, NEW_COMPARISON.value)
      return InitialScreen.values().first { it.value == prefsValue }.also { _initialScreen = it }
    }
    set(value) {
      if (value == _initialScreen) return
      _initialScreen = value
      prefs.putInt(Keys.INITIAL_SCREEN, value.value)
      bus.post(InitialScreenChangedEvent(value))
    }

  fun showDialog(fragmentManager: FragmentManager) {
    DelegatingDialogFragment.show(fragmentManager, INITIAL_SCREEN_DIALOG)
  }
}