package com.unitpricecalculator.util.extensions

import android.os.LocaleList
import java.util.Locale

fun LocaleList.firstOrNull(): Locale? {
    return if (isEmpty) null else get(0)
}
