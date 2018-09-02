package com.unitpricecalculator.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.currency.Currencies;
import com.unitpricecalculator.inject.FragmentScoped;
import com.unitpricecalculator.unit.System;
import com.unitpricecalculator.unit.Systems;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.view.DragLinearLayout;
import dagger.android.ContributesAndroidInjector;
import javax.inject.Inject;

public class SettingsFragment extends BaseFragment {

  @dagger.Module
  public interface Module {
    @ContributesAndroidInjector
    @FragmentScoped
    SettingsFragment contributeSettingsFragmentInjector();
  }

  @Inject Units units;
  @Inject Systems systems;
  @Inject Currencies currencies;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        final TextView changeCurrencySubtitle = view.findViewById(R.id.change_currency_subtitle);
        view.findViewById(R.id.change_currency_parent).setOnClickListener(
                v -> currencies.showChangeCurrencyDialog(
                        v.getContext(),
                        currency -> changeCurrencySubtitle.setText(currency.getSymbol())));
        changeCurrencySubtitle.setText(units.getCurrency().getSymbol());

        DragLinearLayout dragLinearLayout = view.findViewById(R.id.drag_linear_layout);

        for (System system : systems.getPreferredOrder()) {
            View rowView = inflater.inflate(R.layout.list_draggable, null);
            ((TextView) rowView.findViewById(R.id.text))
                    .setText(getResources().getString(system.getName()));
            rowView.setTag(system);
            dragLinearLayout.addDragView(rowView, rowView.findViewById(R.id.handler));
        }

        dragLinearLayout.setOnViewSwapListener(
                (firstView, firstPosition, secondView, secondPosition) -> {
                    System[] order = systems.getPreferredOrder();
                    System temp = order[firstPosition];
                    order[firstPosition] = order[secondPosition];
                    order[secondPosition] = temp;
                    systems.setPreferredOrder(order);
                });

        return view;
    }
}
