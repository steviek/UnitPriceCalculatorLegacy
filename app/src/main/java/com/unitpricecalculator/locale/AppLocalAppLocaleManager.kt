package com.unitpricecalculator.locale

import android.os.LocaleList
import com.squareup.otto.Bus
import com.unitpricecalculator.events.AppLocaleChangedEvent
import com.unitpricecalculator.locale.AppLocale.MATCH_DEVICE
import com.unitpricecalculator.util.prefs.IntPrefsKey
import com.unitpricecalculator.util.prefs.Prefs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLocalAppLocaleManager @Inject constructor(
    private val prefs: Prefs,
    private val bus: Bus
) : AppLocaleManager {

    private val defaultLocaleList = LocaleList.getDefault()

    init {
        _instance = this
    }

    private var _current: AppLocale? = null

    override var current: AppLocale
        get() {
            _current?.let { return it }
            return AppLocale.forNumber(prefs[PREFS_KEY] ?: MATCH_DEVICE.number)
                .also {
                    if (it != MATCH_DEVICE) {
                        LocaleList.setDefault(LocaleList(it.toLocale()))
                    }
                    _current = it
                }
        }
        set(value) {
            if (value == _current) return
            _current = value
            prefs[PREFS_KEY] = value.number
            bus.post(AppLocaleChangedEvent)
            if (value == MATCH_DEVICE) {
                LocaleList.setDefault(defaultLocaleList)
            } else {
                LocaleList.setDefault(LocaleList(value.toLocale()))
            }
        }

    fun hasData(): Boolean {
        return prefs[PREFS_KEY] != null
    }

    fun clear() {
        prefs[PREFS_KEY] = null
    }

    companion object {

        private val PREFS_KEY = IntPrefsKey("app_locale")

        private var _instance: AppLocaleManager? = null

        @JvmStatic
        fun getInstance(): AppLocaleManager {
            return _instance!!
        }
    }
}