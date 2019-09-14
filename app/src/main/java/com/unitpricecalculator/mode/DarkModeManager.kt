package com.unitpricecalculator.mode

import com.squareup.otto.Bus
import com.unitpricecalculator.util.prefs.Prefs
import javax.inject.Inject

class DarkModeManager @Inject internal constructor(private val prefs: Prefs, private val bus: Bus) {

  var currentDarkModeState: DarkModeState
    get() = prefs.getString(PREFS_KEY)?.let(DarkModeState::valueOf) ?: DarkModeState.MATCH_DEVICE
    set(value) {
      prefs.putString(PREFS_KEY, value.name)
      bus.post(DarkModeStateChangedEvent(value))
    }

  private companion object {
    const val PREFS_KEY = "dark_mode"
  }
}