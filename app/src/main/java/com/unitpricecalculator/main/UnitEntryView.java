package com.unitpricecalculator.main;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import com.squareup.otto.Subscribe;
import com.unitpricecalculator.MyApplication;
import com.unitpricecalculator.R;
import com.unitpricecalculator.events.CompareUnitChangedEvent;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitEntry;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.abstracts.AbstractOnItemSelectedListener;
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher;
import com.unitpricecalculator.util.SavesStateInBundle;
import com.unitpricecalculator.util.logger.Logger;

import java.text.NumberFormat;
import java.util.Currency;
import java.util.Locale;

final class UnitEntryView extends LinearLayout implements SavesStateInBundle {

    private TextView mRowNumberTextView;
    private EditText mCostEditText;
    private EditText mQuantityEditText;
    private EditText mSizeEditText;
    private Spinner mUnitSpinner;

    private TextView mSummaryTextView;
    private CompareUnitChangedEvent mLastCompareUnit;

    private OnUnitEntryChangedListener mListener;

    private Unit mUnit;
    private Evaluation mEvaluation = Evaluation.NEUTRAL;

    private TextWatcher mTextWatcher = new AbstractTextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            onUnitChanged();
        }
    };

    private boolean mInflated = false;

    public UnitEntryView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.view_unit_entry, this);
        onFinishInflate();
    }

    public UnitEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.view_unit_entry, this);
    }

    @Override
    public Bundle saveState() {
        Bundle bundle = new Bundle();

        bundle.putString("cost", mCostEditText.getText().toString());
        bundle.putString("size", mSizeEditText.getText().toString());
        bundle.putString("quantity", mQuantityEditText.getText().toString());

        if (mUnit != null) {
            bundle.putString("unit", mUnit.name());
        }

        return bundle;
    }

    @Override
    public void restoreState(Bundle bundle) {
        mCostEditText.setText(bundle.getString("cost"));
        mSizeEditText.setText(bundle.getString("size"));
        mQuantityEditText.setText(bundle.getString("quantity"));

        if (bundle.containsKey("unit")) {
            mUnit = Unit.valueOf(bundle.getString("unit"));
        }

        refreshAdapter(UnitArrayAdapter.of(getContext(), mUnit));
        syncViews();
    }

    public void setRowNumber(int rowNumber) {
        mRowNumberTextView.setText(String.valueOf(rowNumber));
    }

    @Subscribe
    public void onUnitTypeChanged(UnitTypeChangedEvent event) {
        refreshAdapter(UnitArrayAdapter.of(getContext(), event.getUnitType()));
    }

    @Subscribe
    public void onSystemOrderChanged(SystemChangedEvent event) {
        refreshAdapter(UnitArrayAdapter.of(getContext(), Units.getCurrentUnitType()));
    }

    @Subscribe
    public void onCompareUnitChanged(CompareUnitChangedEvent event) {
        mLastCompareUnit = event;
        syncViews();
    }

    public Optional<UnitEntry> getEntry() {
        try {
            UnitEntry.Builder unitEntry = UnitEntry.builder();

            unitEntry.setCostString(mCostEditText.getText().toString());
            unitEntry.setCost(Double.parseDouble(mCostEditText.getText().toString()));


            if (Strings.isNullOrEmpty(mQuantityEditText.getText().toString())) {
                unitEntry.setQuantity(1);
                unitEntry.setQuantityString("1");
            } else {
                unitEntry.setQuantity(Integer.parseInt(mQuantityEditText.getText().toString()));
                unitEntry.setQuantityString(mQuantityEditText.getText().toString());
            }

            unitEntry.setSizeString(mSizeEditText.getText().toString());
            unitEntry.setSize(Double.parseDouble(mSizeEditText.getText().toString()));

            unitEntry.setUnit(getSelectedUnit());

            return Optional.of(unitEntry.build());
        } catch (NullPointerException | IllegalArgumentException e) {
            Logger.e(e);
        }
        return Optional.absent();
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

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        boolean oneLine = getResources().getDisplayMetrics().widthPixels >=
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600, getResources().getDisplayMetrics());

        if (oneLine) {
            setOrientation(HORIZONTAL);
        } else {
            setOrientation(VERTICAL);
        }

        mRowNumberTextView = (TextView) findViewById(R.id.text_ordinal);

        mCostEditText = (EditText) findViewById(R.id.price_edit_text);
        mCostEditText.addTextChangedListener(mTextWatcher);

        mQuantityEditText = (EditText) findViewById(R.id.number_edit_text);
        mQuantityEditText.addTextChangedListener(mTextWatcher);

        mSizeEditText = (EditText) findViewById(R.id.size_edit_text);
        mSizeEditText.addTextChangedListener(mTextWatcher);

        mUnitSpinner = (Spinner) findViewById(R.id.unit_spinner);

        mSummaryTextView = (TextView) findViewById(R.id.text_summary);
        mSummaryTextView.setGravity(oneLine
                ? Gravity.LEFT | Gravity.CENTER_VERTICAL
                : Gravity.CENTER);

        mInflated = true;

        if (!this.isInEditMode()) {
            refreshAdapter(UnitArrayAdapter.of(getContext(), Units.getCurrentUnitType()));
            mUnitSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Unit unit = ((UnitArrayAdapter) parent.getAdapter()).getUnit(position);
                    if (unit != mUnit) {
                        mUnit = unit;
                        mUnitSpinner.setAdapter(UnitArrayAdapter.of(parent.getContext(), mUnit));
                        onUnitChanged();
                        syncViews();
                    }

                }
            });
        }
        syncViews();
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

    private void onUnitChanged() {
        Optional<UnitEntry> unitEntry = getEntry();
        syncViews();
        if (mListener != null) {
            mListener.onUnitEntryChanged(unitEntry);
        }
    }

    private void syncViews() {
        Optional<UnitEntry> unitEntry = getEntry();
        if (unitEntry.isPresent() && mLastCompareUnit != null) {
            double baseSize = Double.parseDouble(mLastCompareUnit.getSize());
            Unit baseUnit = mLastCompareUnit.getUnit();
            double pricePer = unitEntry.get().pricePer(baseSize, baseUnit);
            mSummaryTextView.setText(getResources().getString(R.string.text_summary,
                    NumberFormat.getCurrencyInstance().format(pricePer),
                    mLastCompareUnit.getSize(),
                    getResources().getString(baseUnit.getSymbol())));
            mSummaryTextView.setVisibility(View.VISIBLE);

            mRowNumberTextView.setTextColor(ContextCompat.getColor(getContext(), mEvaluation.getPrimaryColor()));
            mSummaryTextView.setTextColor(ContextCompat.getColor(getContext(), mEvaluation.getSecondaryColor()));
        } else {
            mSummaryTextView.setVisibility(View.INVISIBLE);
            mRowNumberTextView.setTextColor(ContextCompat.getColor(getContext(), Evaluation.NEUTRAL.getPrimaryColor()));
            mSummaryTextView.setTextColor(ContextCompat.getColor(getContext(), Evaluation.NEUTRAL.getSecondaryColor()));
        }

    }

    public void setEvaluation(Evaluation evaluation) {
        mEvaluation = evaluation;
        syncViews();
    }

    public void setOnUnitEntryChangedListener(OnUnitEntryChangedListener listener) {
        mListener = listener;
    }

    public interface OnUnitEntryChangedListener {
        void onUnitEntryChanged(Optional<UnitEntry> unitEntry);
    }

    public enum Evaluation {
        GOOD(R.color.good_green, R.color.good_green),
        BAD(R.color.bad_red, R.color.bad_red),
        NEUTRAL(R.color.primaryText, R.color.secondaryText);

        private int primaryColor;
        private int secondaryColor;

        Evaluation(int primaryColor, int secondaryColor) {
            this.primaryColor = primaryColor;
            this.secondaryColor = secondaryColor;
        }

        public int getPrimaryColor() {
            return primaryColor;
        }

        public int getSecondaryColor() {
            return secondaryColor;
        }
    }
}
