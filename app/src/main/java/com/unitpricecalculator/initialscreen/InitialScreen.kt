@file:JvmName("InitialScreens")

package com.unitpricecalculator.initialscreen

import androidx.annotation.StringRes
import com.unitpricecalculator.R

enum class InitialScreen(val value: Int, @StringRes val labelResId: Int) {
  NEW_COMPARISON(0, R.string.current),
  SAVED_COMPARISONS(1, R.string.saved)
}


data class InitialScreenChangedEvent(val newInitialScreen: InitialScreen)
