package com.unitpricecalculator.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;

public class SettingsFragment extends BaseFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        /*mDragLinearLayout = (DragLinearLayout) view.findViewById(R.id.drag_linear_layout);

        for (System system : System.getPreferredOrder()) {
            View rowView = inflater.inflate(R.layout.list_draggable, null);
            ((TextView) rowView.findViewById(R.id.text))
                    .setText(getResources().getString(system.getName()));
            rowView.setTag(system);
            mDragLinearLayout.addDragView(rowView, rowView.findViewById(R.id.handler));
        }

        mDragLinearLayout.setOnViewSwapListener(new DragLinearLayout.OnViewSwapListener() {
            @Override
            public void onSwap(View firstView, int firstPosition, View secondView, int secondPosition) {
                System[] order = System.getPreferredOrder();
                System temp = order[firstPosition];
                order[firstPosition] = order[secondPosition];
                order[secondPosition] = temp;
                System.setPreferredOrder(order);
            }
        });*/

        return view;
    }
}
