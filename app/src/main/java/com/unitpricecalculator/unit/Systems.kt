package com.unitpricecalculator.unit

import com.squareup.otto.Bus
import com.unitpricecalculator.events.SystemChangedEvent
import com.unitpricecalculator.unit.System.*
import com.unitpricecalculator.util.logger.Logger
import com.unitpricecalculator.util.prefs.Keys
import com.unitpricecalculator.util.prefs.Prefs
import dagger.Reusable
import java.util.Arrays
import java.util.Locale
import javax.inject.Inject

@Reusable
class Systems @Inject internal constructor(private val prefs: Prefs, private val bus: Bus) {

  private val defaultOrder: String
    get() {
      val country = Locale.getDefault().country
      return if (
        country.equals("US", ignoreCase = true) || country.equals("USA", ignoreCase = true)
      ) {
        IMPERIAL_US.name + "," + METRIC.name + "," + IMPERIAL_UK.name
      } else {
        METRIC.name + "," + IMPERIAL_UK.name + "," + IMPERIAL_US.name
      }
    }

  var preferredOrder: Array<System>
    get() {
      return prefs.getNonNullString(Keys.SYSTEM_ORDER, defaultOrder)
        .split(',')
        .map(System::valueOf)
        .toTypedArray()
    }
    set(order) {
      require(order.size == 3)
      prefs.putString(Keys.SYSTEM_ORDER, order[0].name + "," + order[1].name + "," + order[2].name)
      Logger.d("Set preferred order: %s", Arrays.toString(order))
      bus.post(SystemChangedEvent(order, includedSystems))
    }

  private var _includedSystems: Set<System> =
    prefs.getStringSet(Keys.INCLUDED_SYSTEMS, fallback = null)
      ?.map(System::valueOf)
      ?.toSet()
      ?: setOf(METRIC, IMPERIAL_US, IMPERIAL_UK)
  var includedSystems: Set<System>
    get() = _includedSystems
    set(value) {
      require(value.isNotEmpty())
      _includedSystems = value
      prefs.putStringSet(Keys.INCLUDED_SYSTEMS, value.map { it.name }.toSet())
      bus.post(SystemChangedEvent(preferredOrder, includedSystems))
    }

  companion object {
    @JvmStatic
    fun Collection<System>.includes(system: System): Boolean {
      return contains(system) || (
        system == IMPERIAL && (contains(IMPERIAL_UK) || contains(IMPERIAL_US))
        )
    }
  }
}