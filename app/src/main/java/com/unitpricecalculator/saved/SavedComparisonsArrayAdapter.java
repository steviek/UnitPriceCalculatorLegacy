package com.unitpricecalculator.saved;

import android.content.Context;
import android.widget.ArrayAdapter;

import com.unitpricecalculator.comparisons.SavedComparison;

/**
 * Created by stevenkideckel on 2015-10-25.
 */
public class SavedComparisonsArrayAdapter extends ArrayAdapter<SavedComparison> {

    public SavedComparisonsArrayAdapter(Context context, int resource) {
        super(context, resource);
    }
}
