package com.unitpricecalculator.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import com.google.android.material.textfield.MaterialAutoCompleteTextView

class ExposedDropdownMenu(
    context: Context,
    attrs: AttributeSet?
) : MaterialAutoCompleteTextView(context, attrs) {

    var lastSelectedItemPosition: Int? = null
        private set

    private var delegateOnItemSelectedListener: OnItemSelectedListener? = null

    private val onItemSelectedListener = object: OnItemSelectedListener {
        override fun onItemSelected(
            parent: AdapterView<*>?,
            view: View?,
            position: Int,
            id: Long
        ) {
            lastSelectedItemPosition = position
            delegateOnItemSelectedListener?.onItemSelected(parent, view, position, id)
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
            lastSelectedItemPosition = null
            delegateOnItemSelectedListener?.onNothingSelected(parent)
        }
    }

    init {
        super.setOnItemSelectedListener(onItemSelectedListener)
    }

    override fun getFreezesText() = false

    override fun setOnItemSelectedListener(l: OnItemSelectedListener?) {
        delegateOnItemSelectedListener = l
    }
}
