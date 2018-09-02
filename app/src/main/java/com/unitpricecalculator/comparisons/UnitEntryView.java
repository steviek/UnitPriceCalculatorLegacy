package com.unitpricecalculator.comparisons;

import android.content.Context;
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
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.R;
import com.unitpricecalculator.events.CompareUnitChangedEvent;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.inject.ViewInjection;
import com.unitpricecalculator.unit.DefaultUnit;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitEntry;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.SavesState;
import com.unitpricecalculator.util.abstracts.AbstractOnItemSelectedListener;
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher;
import com.unitpricecalculator.util.logger.Logger;
import java.text.NumberFormat;
import javax.inject.Inject;

public final class UnitEntryView extends LinearLayout implements SavesState<SavedUnitEntryRow> {

    @Inject Units units;
    @Inject UnitArrayAdapterFactory unitArrayAdapterFactory;
    @Inject Bus bus;

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
        ViewInjection.inject(this);
        LayoutInflater.from(context).inflate(R.layout.view_unit_entry, this);
        onFinishInflate();
    }

    public UnitEntryView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewInjection.inject(this);
        LayoutInflater.from(context).inflate(R.layout.view_unit_entry, this);
    }

    @Override
    public SavedUnitEntryRow saveState() {
        return new SavedUnitEntryRow(
                mCostEditText.getText().toString(),
                mQuantityEditText.getText().toString(),
                mSizeEditText.getText().toString(),
                mUnit);
    }

    @Override
    public void restoreState(SavedUnitEntryRow entryRow) {
        mCostEditText.setText(entryRow.getCost());
        mSizeEditText.setText(entryRow.getSize());
        mQuantityEditText.setText(entryRow.getQuantity());
        mUnit = entryRow.getUnit();
        refreshAdapter(unitArrayAdapterFactory.create(mUnit));
        syncViews();
    }

    public void setRowNumber(int rowNumber) {
        mRowNumberTextView.setText(String.valueOf(rowNumber));
    }

    @Subscribe
    public void onUnitTypeChanged(UnitTypeChangedEvent event) {
        refreshAdapter(unitArrayAdapterFactory.create(event.getUnitType()));
    }

    @Subscribe
    public void onSystemOrderChanged(SystemChangedEvent event) {
        refreshAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType()));
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

            if (Strings.isNullOrEmpty(mSizeEditText.getText().toString())) {
                unitEntry.setSize(1);
                unitEntry.setSizeString("1");
            } else {
                unitEntry.setSizeString(mSizeEditText.getText().toString());
                unitEntry.setSize(Double.parseDouble(mSizeEditText.getText().toString()));
            }

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
            bus.register(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (!this.isInEditMode()) {
            bus.unregister(this);
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
            refreshAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType()));
            mUnitSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    Unit unit = ((UnitArrayAdapter) parent.getAdapter()).getUnit(position);
                    if (unit != mUnit) {
                        mUnit = unit;
                        mUnitSpinner.setAdapter(unitArrayAdapterFactory.create(mUnit));
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
            return DefaultUnit.UNIT;
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
            if (unitEntry.get().getUnit().getUnitType() != baseUnit.getUnitType()) {
                return;
            }
            double pricePer = unitEntry.get().pricePer(baseSize, baseUnit);
            NumberFormat numberFormat = NumberFormat.getCurrencyInstance();
            numberFormat.setCurrency(units.getCurrency());
            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setMaximumFractionDigits(8);
            mSummaryTextView.setText(getResources().getString(R.string.text_summary,
                    numberFormat.format(pricePer),
                    mLastCompareUnit.getSize(),
                    baseUnit.getSymbol(getResources())));
            mSummaryTextView.setVisibility(View.VISIBLE);

            mRowNumberTextView.setTextColor(
                    ContextCompat.getColor(getContext(), mEvaluation.getPrimaryColor()));
            mSummaryTextView.setTextColor(
                    ContextCompat.getColor(getContext(), mEvaluation.getSecondaryColor()));
        } else {
            mSummaryTextView.setVisibility(View.INVISIBLE);
            mRowNumberTextView.setTextColor(
                    ContextCompat.getColor(getContext(), Evaluation.NEUTRAL.getPrimaryColor()));
            mSummaryTextView.setTextColor(
                    ContextCompat.getColor(getContext(), Evaluation.NEUTRAL.getSecondaryColor()));
        }
    }

    public void clear() {
        mCostEditText.setText("");
        mQuantityEditText.setText("");
        mSizeEditText.setText("");
        refreshAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType().getBase()));
        syncViews();
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
