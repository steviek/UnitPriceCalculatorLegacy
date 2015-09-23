package com.unitpricecalculator.main;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BuildConfig;
import com.unitpricecalculator.MyApplication;
import com.unitpricecalculator.R;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.SavesStateInBundle;

final class UnitEntryView extends LinearLayout implements SavesStateInBundle {

  private TextView mRowNumberTextView;
  private EditText mCostEditText;
  private EditText mQuantityEditText;
  private EditText mSizeEditText;
  private Spinner mUnitSpinner;

  private double mCost;
  private int mQuantity;
  private double mSize;
  private Unit mUnit;

  private boolean mInflated = false;

  public UnitEntryView(Context context) {
    super(context);
    init(context, null);
    onFinishInflate();
  }

  public UnitEntryView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs);
  }

  private void init(Context context, AttributeSet attrs) {
    setOrientation(HORIZONTAL);
    LayoutInflater.from(context).inflate(R.layout.view_unit_entry, this);
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();


    mRowNumberTextView = (TextView) findViewById(R.id.text_ordinal);
    mCostEditText = (EditText) findViewById(R.id.price_edit_text);
    mQuantityEditText = (EditText) findViewById(R.id.number_edit_text);
    mSizeEditText = (EditText) findViewById(R.id.size_edit_text);

    mUnitSpinner = (Spinner) findViewById(R.id.unit_spinner);

    mInflated = true;

    if (!BuildConfig.DEBUG || !this.isInEditMode()) {
      refreshAdapter(UnitArrayAdapter.of(getContext(), Units.getCurrentUnitType()));
      mUnitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          Unit unit = ((UnitArrayAdapter) parent.getAdapter()).getUnit(position);
          if (unit != mUnit) {
            mUnit = unit;
            mUnitSpinner.setAdapter(UnitArrayAdapter.of(parent.getContext(), mUnit));
          }
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
      });
    }


  }

  @Override
  public Bundle saveState() {
    Bundle bundle = new Bundle();
    bundle.putDouble("cost", mCost);
    bundle.putDouble("size", mSize);
    bundle.putInt("quantity", mQuantity);

    if (mUnit != null) {
      bundle.putString("unit", mUnit.name());
    }

    return bundle;
  }

  @Override
  public void restoreState(Bundle bundle) {
    mCost = bundle.getDouble("cost");
    mSize = bundle.getDouble("size");
    mQuantity = bundle.getInt("quantity");

    if (bundle.containsKey("unit")) {
      mUnit = Unit.valueOf(bundle.getString("unit"));
    }

    refreshAdapter(UnitArrayAdapter.of(getContext(), mUnit));
  }

  private Unit getSelectedUnit() {
    if (this.isInEditMode()) {
      return Unit.UNIT;
    }
    return ((UnitArrayAdapter) mUnitSpinner.getAdapter()).getUnit(mUnitSpinner.getSelectedItemPosition());
  }

  private void refreshAdapter(UnitArrayAdapter adapter) {
    if (mInflated) {
      mUnitSpinner.setAdapter(adapter);
      mUnit = adapter.getUnit(mUnitSpinner.getSelectedItemPosition());
    }
  }

  public void setRowNumber(int rowNumber) {
    mRowNumberTextView.setText(String.valueOf(rowNumber));
  }

  public boolean isComplete() {
    return mCost >= 0 && mSize > 0 && mQuantity > 0 && mUnit != null;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    if (!this.isInEditMode()) {
      MyApplication.getInstance().getBus().register(this);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (!this.isInEditMode()) {
      MyApplication.getInstance().getBus().unregister(this);
    }
  }

  @Subscribe
  public void onUnitTypeChanged(UnitTypeChangedEvent event) {
      refreshAdapter(UnitArrayAdapter.of(getContext(), event.getUnitType()));
  }

  @Subscribe
  public void onSystemOrderChanged(SystemChangedEvent event) {
      refreshAdapter(UnitArrayAdapter.of(getContext(), Units.getCurrentUnitType()));
  }
}
