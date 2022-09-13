package com.unitpricecalculator

import android.content.Context
import android.content.res.Configuration
import android.os.Build.VERSION
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.unitpricecalculator.locale.AppLocale.MATCH_DEVICE
import com.unitpricecalculator.locale.AppLocaleManager
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

abstract class BaseActivity : AppCompatActivity() {
    protected fun hideSoftKeyboard() {
        val view = this.currentFocus
        if (view != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        if (VERSION.SDK_INT >= 33) {
            super.attachBaseContext(newBase)
            return
        }

        val manager =
            EntryPoints.get(newBase.applicationContext, AttachBaseContextEntryPoint::class.java)
                .getAppLocaleManager()
        val locale = manager.current
        if (locale === MATCH_DEVICE) {
            super.attachBaseContext(newBase)
        } else {
            val configuration = Configuration()
            configuration.setLocale(locale.toLocale())
            super.attachBaseContext(newBase.createConfigurationContext(configuration))
        }
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface AttachBaseContextEntryPoint {
        fun getAppLocaleManager(): AppLocaleManager
    }
}