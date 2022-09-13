package com.unitpricecalculator.locale

import android.os.Build.VERSION
import androidx.fragment.app.FragmentManager
import com.unitpricecalculator.dialog.DelegatingDialogFragment
import com.unitpricecalculator.dialog.DialogId.LOCALE_DIALOG
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import javax.inject.Provider

interface AppLocaleManager {

    var current: AppLocale

    companion object {
        @JvmStatic
        fun getInstance(): AppLocaleManager {
            return if (VERSION.SDK_INT >= 33) {
                SystemAppLocaleManager.getInstance()
            } else {
                AppLocalAppLocaleManager.getInstance()
            }
        }

        fun showSelectionDialog(fragmentManager: FragmentManager) {
            DelegatingDialogFragment.show(fragmentManager, LOCALE_DIALOG)
        }
    }
}

val AppLocaleManager.currentLocale: Locale
    get() = current.toLocale()

@InstallIn(SingletonComponent::class)
@Module
object AppLocaleManagerModule {
    @Provides
    fun provideAppLocaleManager(
        appLocalManager: Provider<AppLocalAppLocaleManager>,
        systemAppLocaleManager: Provider<SystemAppLocaleManager>
    ): AppLocaleManager {
        return if (VERSION.SDK_INT >= 33) {
            systemAppLocaleManager.get()
        } else {
            appLocalManager.get()
        }
    }
}
