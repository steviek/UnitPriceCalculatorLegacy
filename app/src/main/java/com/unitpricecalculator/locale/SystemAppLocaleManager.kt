package com.unitpricecalculator.locale

import android.annotation.TargetApi
import android.app.LocaleManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.LocaleList
import com.unitpricecalculator.locale.AppLocale.MATCH_DEVICE
import com.unitpricecalculator.util.extensions.firstOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@TargetApi(33)
@Singleton
class SystemAppLocaleManager @Inject constructor(
    @ApplicationContext context: Context,
    appLocalAppLocaleManager: AppLocalAppLocaleManager
) : AppLocaleManager {

    private val localeManager = context.getSystemService(LocaleManager::class.java)

    private val localeChangeReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            _current = getAppLocaleFromSystem()
        }
    }

    private var _current: AppLocale

    init {
        instance = this

        if (appLocalAppLocaleManager.hasData()) {
            _current = appLocalAppLocaleManager.current
            if (_current != MATCH_DEVICE) {
                localeManager.applicationLocales = LocaleList(_current.toLocale())
            }
            appLocalAppLocaleManager.clear()
        } else {
            _current = getAppLocaleFromSystem()
        }

        context.registerReceiver(
            localeChangeReceiver,
            IntentFilter(Intent.ACTION_LOCALE_CHANGED)
        )
    }

    private fun getAppLocaleFromSystem(): AppLocale {
        val overrideLocale = localeManager.applicationLocales.firstOrNull() ?: return MATCH_DEVICE
        return AppLocale.values()
            .firstOrNull { it.languageCode == overrideLocale.language }
            ?: run {
                // This should not happen, but if it does, just clear whatever was
                // somehow set.
                localeManager.applicationLocales = LocaleList.getEmptyLocaleList()
                MATCH_DEVICE
            }
    }

    override var current: AppLocale
        get() = _current
        set(value) {
            _current = value
            localeManager.applicationLocales =
                if (value == MATCH_DEVICE) {
                    LocaleList.getEmptyLocaleList()
                } else {
                    LocaleList(value.toLocale())
                }
        }

    companion object {
        private lateinit var instance: SystemAppLocaleManager

        fun getInstance(): SystemAppLocaleManager {
            return instance
        }
    }
}