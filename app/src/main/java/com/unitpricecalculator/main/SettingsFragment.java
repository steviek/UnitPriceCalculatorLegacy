package com.unitpricecalculator.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.currency.Currencies;
import com.unitpricecalculator.events.CurrencyChangedEvent;
import com.unitpricecalculator.inject.FragmentScoped;
import com.unitpricecalculator.mode.DarkModeDialogFragment;
import com.unitpricecalculator.mode.DarkModeManager;
import com.unitpricecalculator.mode.DarkModeStateChangedEvent;
import com.unitpricecalculator.unit.System;
import com.unitpricecalculator.unit.Systems;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.sometimes.MutableSometimes;
import com.unitpricecalculator.view.DragLinearLayout;
import dagger.android.ContributesAndroidInjector;
import java.util.HashSet;
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
  @Inject Bus bus;
  @Inject DarkModeManager darkModeManager;

  private final MutableSometimes<TextView> changeCurrencySubtitle = MutableSometimes.create();
  private final MutableSometimes<TextView> darkModeSubtitle = MutableSometimes.create();

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

    TextView changeCurrencySubtitleTextView = view.findViewById(R.id.change_currency_subtitle);
    changeCurrencySubtitle.set(changeCurrencySubtitleTextView);
    changeCurrencySubtitleTextView.setText(units.getCurrency().getSymbol());
    view.findViewById(R.id.change_currency_parent).setOnClickListener(
        v -> currencies.showChangeCurrencyDialog());

    DragLinearLayout dragLinearLayout = view.findViewById(R.id.drag_linear_layout);

    HashSet<System> includedSystems = new HashSet<>(systems.getIncludedSystems());
    for (System system : systems.getPreferredOrder()) {
      View rowView = inflater.inflate(R.layout.list_draggable, dragLinearLayout, false);
      TextView text = rowView.findViewById(R.id.text);
      text.setText(getResources().getString(system.getName()));
      rowView.setTag(system);
      dragLinearLayout.addDragView(rowView, rowView.findViewById(R.id.handler));
      CheckBox checkBox = rowView.findViewById(R.id.checkbox);
      checkBox.setChecked(includedSystems.contains(system));
      text.setOnClickListener(v -> checkBox.toggle());
      checkBox.setOnCheckedChangeListener((compoundButton, isChecked) -> {
        boolean modificationRequired = isChecked != includedSystems.contains(system);
        if (!modificationRequired) {
          return;
        }

        if (includedSystems.size() == 1 && !isChecked) {
          // If this is the last unit, don't allow it to be unchecked.
          compoundButton.toggle();
          return;
        }

        if (isChecked) {
          includedSystems.add(system);
        } else {
          includedSystems.remove(system);
        }
        systems.setIncludedSystems(includedSystems);
      });
    }

    dragLinearLayout.setOnViewSwapListener(
        (firstView, firstPosition, secondView, secondPosition) -> {
          System[] order = systems.getPreferredOrder();
          System temp = order[firstPosition];
          order[firstPosition] = order[secondPosition];
          order[secondPosition] = temp;
          systems.setPreferredOrder(order);
        });

    TextView darkModeSubtitleTextView = view.findViewById(R.id.dark_mode_subtitle);
    darkModeSubtitle.set(darkModeSubtitleTextView);
    darkModeSubtitleTextView.setText(darkModeManager.getCurrentDarkModeState().getLabelResId());
    view.findViewById(R.id.dark_mode_parent)
        .setOnClickListener(v -> DarkModeDialogFragment.show(getChildFragmentManager()));

    return view;
  }

  @Subscribe
  public void onCurrencyChangedEvent(CurrencyChangedEvent event) {
    changeCurrencySubtitle
        .whenPresent(textView -> textView.setText(event.getCurrency().getSymbol()));
  }

  @Subscribe
  public void onDarkModeStateChanged(DarkModeStateChangedEvent event) {
    darkModeSubtitle.whenPresent(textView -> textView.setText(event.getNewState().getLabelResId()));
  }
}
