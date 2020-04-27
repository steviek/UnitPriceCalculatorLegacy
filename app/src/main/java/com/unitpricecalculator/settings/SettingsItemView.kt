package com.unitpricecalculator.settings

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import com.unitpricecalculator.R

class SettingsItemView(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {

  private val titleText: TextView
  private val subtitleText: TextView

  init {
    orientation = VERTICAL
    View.inflate(context, R.layout.view_settings_item, this)
    titleText = findViewById(R.id.title)
    subtitleText = findViewById(R.id.subtitle)

    val resolvedAttributes = context.obtainStyledAttributes(attrs, R.styleable.SettingsItemView)

    resolvedAttributes.getString(R.styleable.SettingsItemView_android_title)?.let {
      titleText.text = it
    }

    resolvedAttributes.getString(R.styleable.SettingsItemView_android_subtitle)?.let {
      subtitleText.text = it
    }

    resolvedAttributes.recycle()
  }

  var title: CharSequence
    get() = titleText.text
    set(value) = titleText.setText(value)

  var subtitle: CharSequence
    get() = subtitleText.text
    set(value) = subtitleText.setText(value)

  fun setSubtitle(@StringRes labelResId: Int) {
    subtitleText.setText(labelResId)
  }

}