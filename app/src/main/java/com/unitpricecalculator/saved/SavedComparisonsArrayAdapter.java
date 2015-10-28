package com.unitpricecalculator.saved;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.unitpricecalculator.R;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;

import java.util.List;

public class SavedComparisonsArrayAdapter extends ArrayAdapter<SavedComparison> {

    private SavedComparisonsArrayAdapter(Context context, List<SavedComparison> savedComparisons) {
        super(context, R.layout.row_saved, savedComparisons);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_saved, null);
            view.setTag(new ViewHolder(view));
        }
        SavedComparison comparison = getItem(position);
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.getTitle().setText(comparison.getName());
        return view;
    }

    public static SavedComparisonsArrayAdapter create(Context context) {
        List<SavedComparison> savedComparisons = Prefs.getList(SavedComparison.class, Keys.SAVED_STATES);
        return new SavedComparisonsArrayAdapter(context, savedComparisons);
    }

    private static final class ViewHolder {
        private final TextView title;

        public ViewHolder(View view) {
            this.title = (TextView) view.findViewById(R.id.text_title);
        }

        public TextView getTitle() {
            return title;
        }
    }
}
