package com.unitpricecalculator.mode

import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.NightMode
import com.unitpricecalculator.R

enum class DarkModeState(
    val index: Int,
    @StringRes val labelResId: Int,
    @NightMode val nightMode: Int
) {
  MATCH_DEVICE(0, R.string.match_device, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM),
  ALWAYS_DARK(1, R.string.dark_mode_always_dark, AppCompatDelegate.MODE_NIGHT_YES),
  ALWAYS_LIGHT(2, R.string.dark_mode_always_light, AppCompatDelegate.MODE_NIGHT_NO);
}