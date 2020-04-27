@file:JvmName("InitialScreens")

package com.unitpricecalculator.initialscreen

import androidx.annotation.StringRes
import com.unitpricecalculator.R

enum class InitialScreen(val value: Int, @StringRes val labelResId: Int) {
  NEW_COMPARISON(0, R.string.initial_screen_new_comparison),
  SAVED_COMPARISONS(1, R.string.saved_comparisons)
}


data class InitialScreenChangedEvent(val newInitialScreen: InitialScreen)
