package com.unitpricecalculator.view

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.google.android.material.internal.TextWatcherAdapter
import com.unitpricecalculator.databinding.MaterialSearchViewBinding

class MaterialSearchView(context: Context, attrs: AttributeSet?) : FrameLayout(context, attrs) {

    private val binding =
        MaterialSearchViewBinding.inflate(LayoutInflater.from(context), this, false)

    var queryChangedListener: OnSearchQueryChangedListener? = null

    var query: CharSequence
        get() = binding.input.text
        set(value) {
            binding.input.setText(value)
        }

    init {
        addView(binding.root)
        binding.input.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                queryChangedListener?.onQueryChanged(s?.toString() ?: "")
            }
        })
    }

}

fun interface OnSearchQueryChangedListener {
    fun onQueryChanged(query: String)
}