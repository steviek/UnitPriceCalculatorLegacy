package com.unitpricecalculator.comparisons;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.InputType;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.MyApplication;
import com.unitpricecalculator.R;
import com.unitpricecalculator.currency.Currencies;
import com.unitpricecalculator.events.CompareUnitChangedEvent;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
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

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class ComparisonFragment extends BaseFragment
        implements UnitEntryView.OnUnitEntryChangedListener, SavesState<SavedComparison> {

  private LinearLayout mRowContainer;
  private View mAddRowButton;
  private TextView mRemoveRowButton;
  private EditText mFinalEditText;
  private Spinner mFinalSpinner;
  private TextView mSummaryText;
  private AlertDialog mAlertDialog;
  private Spinner mUnitTypeSpinner;
  private UnitTypeArrayAdapter mUnitTypeArrayAdapter;
  private TextView mPriceHeader;

  private List<UnitEntryView> mEntryViews = new ArrayList<>();

  private SavedComparison mSavedState;

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_main, container, false);

    mPriceHeader = view.findViewById(R.id.price_header);
    mPriceHeader.setText(Units.getCurrency().getSymbol());
    mPriceHeader.setOnClickListener(
            v -> Currencies.showChangeCurrencyDialog(
                    getContext(), currency -> mPriceHeader.setText(currency.getSymbol())));

    mUnitTypeSpinner = view.findViewById(R.id.unit_type_spinner);
    mUnitTypeArrayAdapter = new UnitTypeArrayAdapter(getContext());
    mUnitTypeSpinner.setAdapter(mUnitTypeArrayAdapter);
    mUnitTypeSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Logger.d("onItemSelected: %s", parent.getItemAtPosition(position));
        UnitType unitType = UnitType.fromName((String) parent.getItemAtPosition(position),
            getResources());
        if (Units.getCurrentUnitType() != unitType) {
          setUnitType((Spinner) parent, unitType);
        }
      }
    });

    mRowContainer = view.findViewById(R.id.row_container);

    for (int i = 0; i < mRowContainer.getChildCount(); i++) {
      UnitEntryView entryView = (UnitEntryView) mRowContainer.getChildAt(i);
      entryView.setOnUnitEntryChangedListener(this);
      mEntryViews.add(entryView);
      entryView.setRowNumber(i + 1);
    }

    mAddRowButton = view.findViewById(R.id.add_row_btn);
    mAddRowButton.setOnClickListener(v -> {
      if (mEntryViews.size() == 9) {
        mAddRowButton.setEnabled(false);
      }
      addRowView();
    });

    mRemoveRowButton = view.findViewById(R.id.remove_row_btn);
    mRemoveRowButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mEntryViews.size() == 1) {
          throw new IllegalStateException();
        } else if (mEntryViews.size() == 2) {
          mRemoveRowButton.setEnabled(false);
        }
        mAddRowButton.setEnabled(true);
        UnitEntryView entryView = mEntryViews.remove(mEntryViews.size() - 1);
        entryView.setOnUnitEntryChangedListener(null);
        mRowContainer.removeView(entryView);
        evaluateEntries();
      }
    });

    mFinalEditText = view.findViewById(R.id.final_size);
    mFinalEditText.addTextChangedListener(new AbstractTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        MyApplication.getInstance().getBus().post(getCompareUnit());
        evaluateEntries();
      }
    });

    mFinalSpinner = view.findViewById(R.id.final_spinner);
    if (mFinalSpinner.getAdapter() == null) {
      mFinalSpinner.setAdapter(UnitArrayAdapter.of(getContext(), Units.getCurrentUnitType()));
      mFinalSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          MyApplication.getInstance().getBus().post(getCompareUnit());
          evaluateEntries();
        }
      });
    }

    mSummaryText = view.findViewById(R.id.final_text_summary);

    return view;
  }

  private void setUnitType(Spinner parent, UnitType unitType) {
    Units.setCurrentUnitType(unitType);
    parent.setAdapter(new UnitTypeArrayAdapter(getContext()));
    mFinalSpinner.setAdapter(UnitArrayAdapter.of(getContext(), unitType));
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
    float dp16 = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16, getResources().getDisplayMetrics());
    entryView.setPadding((int) dp16, 0, (int) dp16, 0);
    entryView.setRowNumber(mEntryViews.size());
    mRemoveRowButton.setEnabled(true);
    entryView.setOnUnitEntryChangedListener(this);
    entryView.onCompareUnitChanged(getCompareUnit());
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
    UnitType unitType = UnitType.fromName(mUnitTypeArrayAdapter.getItem(mUnitTypeSpinner.getSelectedItemPosition()),
        MyApplication.getInstance().getResources());
    String finalSize = mFinalEditText.getText().toString();
    Unit finalUnit = ((UnitArrayAdapter) mFinalSpinner.getAdapter())
        .getUnit(mFinalSpinner.getSelectedItemPosition());
    String savedName = name;
    if (Strings.isNullOrEmpty(savedName)) {
      DateFormat df = DateFormat.getDateTimeInstance();
      savedName = getString(R.string.saved_from_date, df.format(new Date(System.currentTimeMillis())));
    }
    return new SavedComparison(savedName, unitType, list.build(), finalSize, finalUnit);
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
    UnitArrayAdapter adapter = UnitArrayAdapter.of(getContext(), unit);
    mFinalSpinner.setAdapter(adapter);
    mFinalSpinner.setSelection(0);
    adapter.notifyDataSetChanged();

    mSavedState = null;
  }

  public void clear() {
    for (UnitEntryView entryView : mEntryViews) {
      entryView.clear();
    }
    mFinalEditText.setText("");
    mFinalSpinner.setAdapter(UnitArrayAdapter.of(getContext(), Units.getCurrentUnitType()));
    mSummaryText.setText("");
  }

  public void save() {
    if (mAlertDialog == null) {
      View view = LayoutInflater.from(getContext()).inflate(R.layout.view_enter_name, null);
      final EditText name = view.findViewById(R.id.comparison_label);
      name.setInputType(InputType.TYPE_CLASS_TEXT);
      name.setHint(R.string.enter_name);

      AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
      alert.setMessage(R.string.give_name);
      alert.setView(view);
      alert.setPositiveButton(android.R.string.ok, (dialog, which) -> {
        String savedName = name.getText().toString();
        SavedComparison comparison = saveState(savedName);
        Prefs.addToList(SavedComparison.class, Keys.SAVED_STATES, comparison);
      });
      alert.setNegativeButton(android.R.string.cancel, null);
      mAlertDialog = alert.create();
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
    MyApplication.getInstance().getBus().register(this);
    if (mSavedState != null) {
      restoreState(mSavedState);
      evaluateEntries();
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    MyApplication.getInstance().getBus().unregister(this);
  }

  @Subscribe
  public void onUnitTypeChanged(UnitTypeChangedEvent event) {
    mFinalSpinner.setAdapter(UnitArrayAdapter.of(getContext(), event.getUnitType()));
  }

  @Subscribe
  public void onSystemChanged(SystemChangedEvent event) {
    mFinalSpinner.setAdapter(UnitArrayAdapter.of(getContext(), Units.getCurrentUnitType()));
  }

  @Override
  public void onUnitEntryChanged(Optional<UnitEntry> unitEntry) {
    evaluateEntries();
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
    format.setCurrency(Units.getCurrency());
    format.setMinimumFractionDigits(2);
    format.setMaximumFractionDigits(8);

    StringBuilder finalSummary = new StringBuilder();
    UnitEntryWithIndex best = unitEntries.get(0);
    if (best.getUnitEntry().getQuantity() == 1) {
      finalSummary.append(getResources().getString(R.string.main_final_summary_no_number,
              best.getIndex() + 1,
              format.format(best.getUnitEntry().getCost()),
              best.getUnitEntry().getSizeString(),
              best.getUnitEntry().getUnit().getSymbol(),
              format.format(best.getUnitEntry().pricePer(size, unit)),
              compareUnit.getSize(),
              unit.getSymbol()));
    } else {
      finalSummary.append(getResources().getString(R.string.main_final_summary,
              best.getIndex() + 1,
              format.format(best.getUnitEntry().getCost()),
              best.getUnitEntry().getQuantityString(),
              best.getUnitEntry().getSizeString(),
              best.getUnitEntry().getUnit().getSymbol(),
              format.format(best.getUnitEntry().pricePer(size, unit)),
              compareUnit.getSize(),
              unit.getSymbol()));
    }

    finalSummary.append("\n\n");

    for (UnitEntryWithIndex entryWithIndex : unitEntries) {
      if (entryWithIndex.getUnitEntry().getQuantity() == 1) {
        finalSummary.append(getResources().getString(R.string.single_row_summary_no_number,
                entryWithIndex.getIndex() + 1,
                format.format(entryWithIndex.getUnitEntry().getCost()),
                entryWithIndex.getUnitEntry().getSizeString(),
                entryWithIndex.getUnitEntry().getUnit().getSymbol(),
                format.format(entryWithIndex.getUnitEntry().pricePer(size, unit)),
                compareUnit.getSize(),
                unit.getSymbol()));
      } else {
        finalSummary.append(getResources().getString(R.string.single_row_summary,
                entryWithIndex.getIndex() + 1,
                format.format(entryWithIndex.getUnitEntry().getCost()),
                entryWithIndex.getUnitEntry().getQuantityString(),
                entryWithIndex.getUnitEntry().getSizeString(),
                entryWithIndex.getUnitEntry().getUnit().getSymbol(),
                format.format(entryWithIndex.getUnitEntry().pricePer(size, unit)),
                compareUnit.getSize(),
                unit.getSymbol()));
      }

      finalSummary.append("\n");
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
