package com.unitpricecalculator.settings

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.annotation.StringRes
import com.unitpricecalculator.R
import com.unitpricecalculator.databinding.SettingsTileBinding

class SettingsTileView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val layoutInflater = LayoutInflater.from(context)
    private val binding =
        SettingsTileBinding.inflate(layoutInflater, this, false).also { addView(it.root) }

    init {
        context.obtainStyledAttributes(attrs, R.styleable.SettingsTileView).use { typedArray ->
            binding.title.text = typedArray.getString(R.styleable.SettingsTileView_android_title)
            binding.body.text = typedArray.getString(R.styleable.SettingsTileView_android_subtitle)
        }
    }

    var subtitle: CharSequence
        get() = binding.body.text
        set(value) {
            binding.body.text = value
        }

    fun setSubtitle(@StringRes resId: Int) {
        binding.body.setText(resId)
    }

    fun setTileClickListener(listener: OnClickListener) {
        binding.root.setOnClickListener(listener)
    }
}