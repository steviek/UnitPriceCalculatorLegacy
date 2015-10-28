package com.unitpricecalculator.saved;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.comparisons.SavedComparison;

public class SavedFragment extends BaseFragment {

    private Callback mCallback;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = castOrThrow(Callback.class, context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView listView = (ListView) view.findViewById(R.id.list_view);
        final SavedComparisonsArrayAdapter adapter = SavedComparisonsArrayAdapter.create(getContext());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onLoadSavedComparison(adapter.getItem(position));
            }
        });
    }

    public interface Callback {
        void onLoadSavedComparison(SavedComparison comparison);
    }
}
