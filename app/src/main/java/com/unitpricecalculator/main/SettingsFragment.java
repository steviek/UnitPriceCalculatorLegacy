package com.unitpricecalculator.main;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.currency.Currencies;
import com.unitpricecalculator.events.CurrencyChangedEvent;
import com.unitpricecalculator.inject.FragmentScoped;
import com.unitpricecalculator.unit.System;
import com.unitpricecalculator.unit.Systems;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.sometimes.MutableSometimes;
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

  @Inject
  Units units;
  @Inject
  Systems systems;
  @Inject
  Currencies currencies;
  @Inject
  Bus bus;

  private final MutableSometimes<TextView> changeCurrencySubtitle = MutableSometimes.create();

  @Override
  public void onStart() {
    super.onStart();
    bus.register(this);
  }

  @Override
  public void onStop() {
    super.onStop();
    bus.unregister(this);
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_settings, container, false);

    changeCurrencySubtitle.set(view.findViewById(R.id.change_currency_subtitle));
    changeCurrencySubtitle.toOptional().get().setText(units.getCurrency().getSymbol());
    view.findViewById(R.id.change_currency_parent).setOnClickListener(
        v -> currencies.showChangeCurrencyDialog());

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

  @Subscribe
  public void onCurrencyChangedEvent(CurrencyChangedEvent event) {
    changeCurrencySubtitle
        .whenPresent(textView -> textView.setText(event.getCurrency().getSymbol()));
  }
}
