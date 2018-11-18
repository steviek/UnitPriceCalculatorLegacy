package com.unitpricecalculator.comparisons;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.currency.Currencies;
import com.unitpricecalculator.events.CompareUnitChangedEvent;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.inject.FragmentScoped;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitEntry;
import com.unitpricecalculator.unit.UnitType;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.NumberUtils;
import com.unitpricecalculator.util.SavesState;
import com.unitpricecalculator.util.abstracts.AbstractOnItemSelectedListener;
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher;
import com.unitpricecalculator.util.logger.Logger;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;
import dagger.android.ContributesAndroidInjector;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;
import javax.inject.Provider;

public final class ComparisonFragment extends BaseFragment
    implements UnitEntryView.OnUnitEntryChangedListener, SavesState<SavedComparison> {

  @dagger.Module
  public interface Module {

    @ContributesAndroidInjector
    @FragmentScoped
    ComparisonFragment contributeComparisonFragmentInjector();
  }

  @Inject
  Prefs prefs;
  @Inject
  Units units;
  @Inject
  Provider<UnitTypeArrayAdapter> unitTypeArrayAdapterProvider;
  @Inject
  Currencies currencies;
  @Inject
  UnitArrayAdapterFactory unitArrayAdapterFactory;
  @Inject
  Bus bus;

  private UnitTypeArrayAdapter unitTypeArrayAdapter;
  private LinearLayout mRowContainer;
  private View mAddRowButton;
  private TextView mRemoveRowButton;
  private EditText mFinalEditText;
  private Spinner mFinalSpinner;
  private TextView mSummaryText;
  private AlertDialog mAlertDialog;
  private Spinner mUnitTypeSpinner;
  private TextView mPriceHeader;
  private ActionMode actionMode;

  private List<UnitEntryView> mEntryViews = new ArrayList<>();

  private SavedComparison mSavedState;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_main, container, false);

    mPriceHeader = view.findViewById(R.id.price_header);
    mPriceHeader.setText(units.getCurrency().getSymbol());
    mPriceHeader.setOnClickListener(
        v -> currencies.showChangeCurrencyDialog(
            getContext(), currency -> mPriceHeader.setText(currency.getSymbol())));

    mUnitTypeSpinner = view.findViewById(R.id.unit_type_spinner);
    unitTypeArrayAdapter = unitTypeArrayAdapterProvider.get();
    mUnitTypeSpinner.setAdapter(unitTypeArrayAdapter);
    mUnitTypeSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Logger.d("onItemSelected: %s", parent.getItemAtPosition(position));
        UnitType unitType = UnitType.fromName((String) parent.getItemAtPosition(position),
            getResources());
        if (units.getCurrentUnitType() != unitType) {
          setUnitType((Spinner) parent, unitType);
        }
      }
    });

    mRowContainer = view.findViewById(R.id.row_container);

    for (int i = 0; i < mRowContainer.getChildCount(); i++) {
      UnitEntryView entryView = (UnitEntryView) mRowContainer.getChildAt(i);
      entryView.setOnUnitEntryChangedListener(this);
      mEntryViews.add(entryView);
      entryView.setRowNumber(i);
    }

    mAddRowButton = view.findViewById(R.id.add_row_btn);
    mAddRowButton.setOnClickListener(v -> {
      if (mEntryViews.size() == 9) {
        mAddRowButton.setEnabled(false);
      }
      addRowView();
    });

    mRemoveRowButton = view.findViewById(R.id.remove_row_btn);
    mRemoveRowButton.setOnClickListener(v -> {
      removeRow(mEntryViews.size() - 1);
    });

    mFinalEditText = view.findViewById(R.id.final_size);
    mFinalEditText.addTextChangedListener(new AbstractTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        bus.post(getCompareUnit());
        evaluateEntries();
      }
    });

    mFinalSpinner = view.findViewById(R.id.final_spinner);
    if (mFinalSpinner.getAdapter() == null) {
      mFinalSpinner.setAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType()));
      mFinalSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          bus.post(getCompareUnit());
          evaluateEntries();
        }
      });
    }

    mSummaryText = view.findViewById(R.id.final_text_summary);

    addRowView();
    addRowView();

    return view;
  }

  private void setUnitType(Spinner parent, UnitType unitType) {
    units.setCurrentUnitType(unitType);
    unitTypeArrayAdapter = unitTypeArrayAdapterProvider.get();
    parent.setAdapter(unitTypeArrayAdapter);
    mFinalSpinner.setAdapter(unitArrayAdapterFactory.create(unitType));
    CompareUnitChangedEvent event = getCompareUnit();
    for (UnitEntryView entryView : mEntryViews) {
      entryView.onCompareUnitChanged(event);
    }
    evaluateEntries();
  }

  private UnitEntryView addRowView() {
    UnitEntryView entryView = new UnitEntryView(getContext());
    mEntryViews.add(entryView);
    mRowContainer.addView(entryView, new ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT));
    float dp16 = TypedValue
        .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
    entryView.setPadding((int) dp16, 0, (int) dp16, 0);
    entryView.setRowNumber(mEntryViews.size() - 1);
    mRemoveRowButton.setEnabled(true);
    entryView.setOnUnitEntryChangedListener(this);
    entryView.onCompareUnitChanged(getCompareUnit());
    entryView.setLongClickable(true);
    WeakReference<UnitEntryView> entryViewReference = new WeakReference<>(entryView);
    entryView.setOnLongClickListener(v -> {
      actionMode = v.startActionMode(new Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
          if (mEntryViews.size() < 2) {
            return false;
          }

          mode.getMenuInflater().inflate(R.menu.menu_row_action_mode, menu);
          UnitEntryView entryView = entryViewReference.get();
          if (entryView == null) {
            return false;
          }

          entryView.onEnterActionMode();
          return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
          UnitEntryView entryView = entryViewReference.get();
          if (entryView == null) {
            return false;
          }

          int rowNumber = entryView.getRowNumber();

          boolean upVisible = rowNumber > 0;
          menu.findItem(R.id.action_up).setVisible(upVisible);

          boolean downVisible = rowNumber < mEntryViews.size() - 1;
          menu.findItem(R.id.action_down).setVisible(downVisible);

          boolean deleteVisible = mEntryViews.size() >= 2;
          menu.findItem(R.id.action_delete).setVisible(deleteVisible);

          return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
          UnitEntryView entryView = entryViewReference.get();
          if (entryView == null) {
            return false;
          }

          int rowNumber = entryView.getRowNumber();
          if (item.getItemId() == R.id.action_delete) {
            removeRow(rowNumber);
            mode.finish();
            return true;
          } else if (item.getItemId() == R.id.action_up) {
            swapRows(rowNumber, rowNumber - 1);
            mode.invalidate();
            return true;
          } else if (item.getItemId() == R.id.action_down) {
            swapRows(rowNumber, rowNumber + 1);
            mode.invalidate();
            return true;
          }

          return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
          actionMode = null;

          UnitEntryView entryView = entryViewReference.get();
          if (entryView == null) {
            return;
          }

          entryView.onExitActionMode();
        }
      });
      return true;
    });
    evaluateEntries();
    return entryView;
  }

  @Override
  public SavedComparison saveState() {
    return saveState(null);
  }

  private SavedComparison saveState(String name) {
    ImmutableList.Builder<SavedUnitEntryRow> list = ImmutableList.builder();
    for (UnitEntryView entryView : mEntryViews) {
      list.add(entryView.saveState());
    }
    UnitType unitType = UnitType.fromName(
        unitTypeArrayAdapter.getItem(mUnitTypeSpinner.getSelectedItemPosition()),
        getResources());
    String finalSize = mFinalEditText.getText().toString();
    Unit finalUnit = ((UnitArrayAdapter) mFinalSpinner.getAdapter())
        .getUnit(mFinalSpinner.getSelectedItemPosition());
    String savedName = name;
    if (Strings.isNullOrEmpty(savedName)) {
      DateFormat df = DateFormat.getDateTimeInstance();
      savedName =
          getString(R.string.saved_from_date, df.format(new Date(System.currentTimeMillis())));
    }
    return new SavedComparison(savedName, unitType, list.build(), finalSize, finalUnit,
        units.getCurrency().getCurrencyCode());
  }

  @Override
  public void restoreState(SavedComparison comparison) {
    if (mRowContainer == null || getContext() == null) {
      mSavedState = comparison;
      return;
    }

    mRowContainer.removeAllViewsInLayout();
    setUnitType(mUnitTypeSpinner, comparison.getUnitType());
    mEntryViews.clear();
    for (SavedUnitEntryRow entryRow : comparison.getSavedUnitEntryRows()) {
      UnitEntryView entryView = addRowView();
      entryView.restoreState(entryRow);
    }
    mFinalEditText.setText(comparison.getFinalQuantity());
    Unit unit = comparison.getFinalUnit();
    UnitArrayAdapter adapter = unitArrayAdapterFactory.create(unit);
    mFinalSpinner.setAdapter(adapter);
    mFinalSpinner.setSelection(0);

    String currencyCode = comparison.getCurrencyCode();
    if (currencyCode != null) {
      try {
        Currency currency = Currency.getInstance(currencyCode);
        if (currency != null) {
          units.setCurrency(currency);
        }
      } catch (IllegalArgumentException e) {
        // This currency used to be supported and no longer is, oh well.
      }
    }

    adapter.notifyDataSetChanged();

    mSavedState = null;
  }

  public void clear() {
    for (UnitEntryView entryView : mEntryViews) {
      entryView.clear();
    }
    mFinalEditText.setText("");
    mFinalSpinner.setAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType()));
    mSummaryText.setText("");
  }

  public void save() {
    if (mAlertDialog == null) {
      View view = LayoutInflater.from(getContext()).inflate(R.layout.view_enter_name, null);
      final EditText name = view.findViewById(R.id.comparison_label);
      name.setInputType(InputType.TYPE_CLASS_TEXT);
      name.setHint(R.string.enter_name);
      name.requestFocus();

      AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
      alert.setMessage(R.string.give_name);
      alert.setView(view);
      alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
        String savedName = name.getText().toString();
        SavedComparison comparison = saveState(savedName);
        prefs.addToList(SavedComparison.class, Keys.SAVED_STATES, comparison);
      });
      alert.setNegativeButton(android.R.string.cancel, null);
      mAlertDialog = alert.create();
      mAlertDialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    }

    if (mAlertDialog.isShowing()) {
      mAlertDialog.dismiss();
    } else {
      mAlertDialog.show();
    }
  }

  @Override
  public void onResume() {
    super.onResume();
    bus.register(this);
    if (mSavedState != null) {
      restoreState(mSavedState);
      evaluateEntries();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    bus.unregister(this);
  }

  @Subscribe
  public void onUnitTypeChanged(UnitTypeChangedEvent event) {
    mFinalSpinner.setAdapter(unitArrayAdapterFactory.create(event.getUnitType()));
    finishActionMode();
  }

  @Subscribe
  public void onSystemChanged(SystemChangedEvent event) {
    mFinalSpinner.setAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType()));
    finishActionMode();
  }

  @Override
  public void onUnitEntryChanged(Optional<UnitEntry> unitEntry) {
    evaluateEntries();
    finishActionMode();
  }

  private CompareUnitChangedEvent getCompareUnit() {
    Unit unit = ((UnitArrayAdapter) mFinalSpinner.getAdapter())
        .getUnit(mFinalSpinner.getSelectedItemPosition());
    String size = NumberUtils.firstParsableDouble(
        mFinalEditText.getText().toString(), String.valueOf(unit.getDefaultQuantity()));
    return new CompareUnitChangedEvent(size, unit);
  }

  private void evaluateEntries() {
    CompareUnitChangedEvent compareUnit = getCompareUnit();
    double size = Double.parseDouble(compareUnit.getSize());
    Unit unit = compareUnit.getUnit();

    if (Strings.isNullOrEmpty(mFinalEditText.getText().toString())) {
      mFinalEditText.setHint(String.valueOf(unit.getDefaultQuantity()));
    }

    List<UnitEntryWithIndex> unitEntries = new ArrayList<>();
    for (int i = 0; i < mEntryViews.size(); i++) {
      UnitEntryView entryView = mEntryViews.get(i);
      Optional<UnitEntry> entry = entryView.getEntry();
      if (entry.isPresent()) {
        unitEntries.add(new UnitEntryWithIndex(i, entry.get()));
      }
    }

    Collections.sort(
        unitEntries,
        (entry1, entry2) ->
            Double.compare(
                entry1.getUnitEntry().pricePer(size, unit),
                entry2.getUnitEntry().pricePer(size, unit)));

    if (unitEntries.size() < 2) {
      mSummaryText.setText("");
      for (UnitEntryView entryView : mEntryViews) {
        entryView.setEvaluation(UnitEntryView.Evaluation.NEUTRAL);
      }
      return;
    }

    NumberFormat format = NumberFormat.getCurrencyInstance();
    format.setCurrency(units.getCurrency());
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(8);

    StringBuilder finalSummary = new StringBuilder();
    UnitEntryWithIndex best = unitEntries.get(0);
    finalSummary.append(getString(R.string.main_final_summary, best.getIndex() + 1)).append('\n');
    appendSingleRowSummary(finalSummary, best.getUnitEntry(), compareUnit, format);

    finalSummary.append("\n\n");

    for (UnitEntryWithIndex entryWithIndex : unitEntries) {
      finalSummary.append(
          String.format(Locale.getDefault(), "%d: ", entryWithIndex.getIndex() + 1));

      appendSingleRowSummary(finalSummary, entryWithIndex.getUnitEntry(), compareUnit, format);
    }

    mSummaryText.setText(finalSummary);

    for (UnitEntryView entryView : mEntryViews) {
      Optional<UnitEntry> entry = entryView.getEntry();
      if (entry.isPresent()) {
        if (entry.get().pricePer(size, unit) <= best.getUnitEntry().pricePer(size, unit)) {
          entryView.setEvaluation(UnitEntryView.Evaluation.GOOD);
        } else {
          entryView.setEvaluation(UnitEntryView.Evaluation.BAD);
        }
      } else {
        entryView.setEvaluation(UnitEntryView.Evaluation.NEUTRAL);
      }
    }
  }

  private void appendSingleRowSummary(StringBuilder message, UnitEntry unitEntry,
      CompareUnitChangedEvent compareUnitChangedEvent, NumberFormat format) {
    Unit compareUnit = compareUnitChangedEvent.getUnit();
    String compareSize = compareUnitChangedEvent.getSize();

    String formattedEntryCostString = format.format(unitEntry.getCost());
    String unitEntrySymbol = unitEntry.getUnit().getSymbol(getResources());

    if (unitEntry.getQuantity() == 1 && unitEntry.getSizeString().equals("1")) {
      message.append(getString(R.string.m_per_u, formattedEntryCostString, unitEntrySymbol));
    } else if (unitEntry.getQuantity() == 1) {
      message.append(
          getString(R.string.m_per_s_u, formattedEntryCostString, unitEntry.getSizeString(),
              unitEntrySymbol));
    } else {
      message.append(
          getString(R.string.m_per_qxs_u, formattedEntryCostString, unitEntry.getQuantityString(),
              unitEntry.getSizeString(), unitEntrySymbol));
    }

    message.append(" = ");

    String formattedCompareUnitCost =
        format.format(unitEntry.pricePer(Double.parseDouble(compareSize), compareUnit));
    String compareUnitSymbol = compareUnit.getSymbol(getResources());

    if (compareSize.equals("1")) {
      message
          .append(getString(R.string.m_per_u, formattedCompareUnitCost, compareUnitSymbol));
    } else {
      message.append(
          getString(R.string.m_per_s_u, formattedCompareUnitCost, compareSize,
              compareUnitSymbol));
    }

    message.append("\n");
  }

  private void finishActionMode() {
    if (actionMode != null) {
      actionMode.finish();
    }
  }

  private void swapRows(int index1, int index2) {
    Preconditions.checkArgument(index1 >= 0);
    Preconditions.checkArgument(index2 >= 0);
    Preconditions.checkArgument(index1 < mEntryViews.size());
    Preconditions.checkArgument(index2 < mEntryViews.size());

    if (index1 == index2) {
      return;
    }

    int max = Math.max(index1, index2);
    int min = Math.min(index1, index2);

    UnitEntryView maxEntryView = mEntryViews.get(max);
    UnitEntryView minEntryView = mEntryViews.get(min);

    Optional<Integer> maxFocusedViewId = maxEntryView.getFocusedViewId();
    Optional<Integer> minFocusedViewId = minEntryView.getFocusedViewId();

    mEntryViews.remove(max);
    mEntryViews.remove(min);

    mRowContainer.removeView(maxEntryView);
    mRowContainer.removeView(minEntryView);

    mRowContainer.addView(maxEntryView, min);
    mRowContainer.addView(minEntryView, max);

    mEntryViews.add(min, maxEntryView);
    mEntryViews.add(max, minEntryView);

    minEntryView.setRowNumber(max);
    maxEntryView.setRowNumber(min);

    if (maxFocusedViewId.isPresent()) {
      maxEntryView.setFocusedViewId(maxFocusedViewId.get());
    } else if (minFocusedViewId.isPresent()) {
      minEntryView.setFocusedViewId(minFocusedViewId.get());
    }

    evaluateEntries();
  }

  private void removeRow(int index) {
    Preconditions.checkState(mEntryViews.size() > 1);

    if (mEntryViews.size() == 2) {
      mRemoveRowButton.setEnabled(false);
    }
    mAddRowButton.setEnabled(true);

    UnitEntryView entryView = mEntryViews.remove(index);

    if (entryView.getFocusedViewId().isPresent()) {
      entryView.clearFocus();
    }

    entryView.setOnUnitEntryChangedListener(null);
    mRowContainer.removeView(entryView);

    for (int i = index; i < mEntryViews.size(); i++) {
      mEntryViews.get(i).setRowNumber(i);
    }

    evaluateEntries();
  }

  private static final class UnitEntryWithIndex {

    final int index;
    final UnitEntry unitEntry;

    UnitEntryWithIndex(int index, UnitEntry unitEntry) {
      this.index = index;
      this.unitEntry = unitEntry;
    }

    public int getIndex() {
      return index;
    }

    public UnitEntry getUnitEntry() {
      return unitEntry;
    }
  }
}
