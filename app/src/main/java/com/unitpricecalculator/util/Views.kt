package com.unitpricecalculator.util

import android.graphics.drawable.Drawable
import android.text.Editable
import android.util.Size
import android.view.View
import android.view.ViewGroup
import android.widget.Adapter
import android.widget.AdapterView
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.unitpricecalculator.util.abstracts.AbstractOnItemSelectedListener
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher

val ViewGroup.children: List<View>
    get() = (0 until childCount).map { getChildAt(it) }

var TextView.drawableStart: Drawable?
    get() = compoundDrawablesRelative[0]
    set(value) {
        val currentDrawables = compoundDrawablesRelative
        setCompoundDrawablesRelative(
            value,
            currentDrawables[1],
            currentDrawables[2],
            currentDrawables[3]
        )
    }

var TextView.drawableEnd: Drawable?
    get() = compoundDrawablesRelative[2]
    set(value) {
        val currentDrawables = compoundDrawablesRelative
        setCompoundDrawablesRelative(
            currentDrawables[0],
            currentDrawables[1],
            value,
            currentDrawables[3]
        )
    }

fun TextView.setDrawableStart(@DrawableRes resId: Int, size: Size) {
    drawableStart = ContextCompat.getDrawable(context, resId).also {
        it?.setBounds(0, 0, size.width, size.height)
    }
}

fun TextView.setDrawableEnd(@DrawableRes resId: Int, size: Size) {
    drawableEnd = ContextCompat.getDrawable(context, resId).also {
        it?.setBounds(0, 0, size.width, size.height)
    }
}

inline fun TextView.afterTextChanged(crossinline consumer: (String) -> Unit) {
    addTextChangedListener(
        object : AbstractTextWatcher() {
            override fun afterTextChanged(s: Editable?) {
                consumer(s?.toString() ?: "")
            }
        }
    )
}

inline fun AdapterView<*>.onItemSelected(crossinline block: (position: Int) -> Unit) {
    onItemSelectedListener = object : AbstractOnItemSelectedListener() {
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            block(position)
        }
    }
}

inline fun View.getString(@StringRes resId: Int) = resources.getString(resId)

inline fun View.getString(@StringRes resId: Int, vararg formatArgs: Any?) : String {
    return resources.getString(resId, *formatArgs)
}
