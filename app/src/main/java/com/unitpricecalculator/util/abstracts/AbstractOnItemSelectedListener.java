package com.unitpricecalculator.util.abstracts;

import android.view.View;
import android.widget.AdapterView;
import androidx.annotation.Nullable;

public abstract class AbstractOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

    @Override
    public void onItemSelected(AdapterView<?> parent, @Nullable View view, int position, long id) {}

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
