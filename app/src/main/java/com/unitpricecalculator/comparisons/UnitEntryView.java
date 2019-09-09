package com.unitpricecalculator.comparisons;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import com.google.auto.factory.AutoFactory;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BuildConfig;
import com.unitpricecalculator.R;
import com.unitpricecalculator.events.CompareUnitChangedEvent;
import com.unitpricecalculator.events.NoteChangedEvent;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.inject.ViewInjection;
import com.unitpricecalculator.unit.DefaultUnit;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitEntry;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.AlertDialogs;
import com.unitpricecalculator.util.Consumer;
import com.unitpricecalculator.util.Localization;
import com.unitpricecalculator.util.SavesState;
import com.unitpricecalculator.util.abstracts.AbstractOnItemSelectedListener;
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher;
import com.unitpricecalculator.util.logger.Logger;
import java.util.Locale;
import javax.inject.Inject;

@AutoFactory
public final class UnitEntryView extends LinearLayout implements SavesState<SavedUnitEntryRow> {

  private static final int COST = 0;
  private static final int SIZE = 1;
  private static final int QUANTITY = 2;

  @Inject Activity activity;
  @Inject Units units;
  @Inject UnitArrayAdapterFactory unitArrayAdapterFactory;
  @Inject Bus bus;

  private TextView mRowNumberTextView;
  private EditText mCostEditText;
  private EditText mQuantityEditText;
  private EditText mSizeEditText;
  private Spinner mUnitSpinner;
  private View mInputFieldsContainer;

  private TextView inlineSummaryTextView;
  private TextView belowSummaryTextView;
  private TextView noteTextView;
  private Button noteButton;
  private CompareUnitChangedEvent mLastCompareUnit;

  private OnUnitEntryChangedListener mListener;

  private DefaultUnit mUnit;
  private Evaluation mEvaluation = Evaluation.NEUTRAL;

  private TextWatcher mTextWatcher = new AbstractTextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
      onUnitChanged();
    }
  };

  private boolean mInflated = false;
  private boolean mInActionMode = false;
  private int mRowNumber;
  private String note;

  private final OnClickListener noteOnClickListener = v -> {
    EditText editText =
        (EditText) LayoutInflater.from(getContext()).inflate(R.layout.enter_note_edit_text, null);
    int margin = getResources().getDimensionPixelOffset(R.dimen.horizontal_margin);
    editText.setText(note);
    editText.requestFocus();
    editText.setSelectAllOnFocus(true);

    DialogInterface.OnClickListener onClickListener = (dialog, which) -> {
      switch (which) {
        case DialogInterface.BUTTON_POSITIVE:
          if (!Strings.nullToEmpty(note).equals(editText.getText().toString())) {
            note = editText.getText().toString();
            bus.post(new NoteChangedEvent());
          }
          break;
        case DialogInterface.BUTTON_NEUTRAL:
          note = "";
          bus.post(new NoteChangedEvent());
          break;
      }
      InputMethodManager imm =
          (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(editText.getWindowToken(),
          InputMethodManager.HIDE_NOT_ALWAYS);
    };

    AlertDialog alertDialog = new AlertDialog.Builder(getContext())
        .setPositiveButton(android.R.string.ok, onClickListener)
        .setNegativeButton(android.R.string.cancel, onClickListener)
        .setNeutralButton(R.string.delete, onClickListener)
        .create();
    alertDialog.setView(editText, margin, margin, margin, 0);
    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    alertDialog.show();
    AlertDialogs.materialize(alertDialog);
    if (Strings.isNullOrEmpty(note)) {
      alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.GONE);
    }
    editText.addTextChangedListener(new AbstractTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setEnabled(!s.toString().isEmpty());
      }
    });
  };

  public UnitEntryView(Context context) {
    super(context);
    ViewInjection.inject(this);
    LayoutInflater.from(context).inflate(R.layout.view_unit_entry, this);
    onFinishInflate();
  }

  @Override
  public SavedUnitEntryRow saveState(Context context) {
    return new SavedUnitEntryRow(
        mCostEditText.getText().toString(),
        mQuantityEditText.getText().toString(),
        mSizeEditText.getText().toString(),
        mUnit,
        note);
  }

  @Override
  public void restoreState(SavedUnitEntryRow entryRow) {
    withoutTextWatcher(mCostEditText, editText -> editText.setText(entryRow.getCost()));
    withoutTextWatcher(mSizeEditText, editText -> editText.setText(entryRow.getSize()));
    withoutTextWatcher(mQuantityEditText, editText -> editText.setText(entryRow.getQuantity()));
    mUnit = entryRow.getUnit();
    note = entryRow.getNote();
    refreshAdapter(unitArrayAdapterFactory.create(mUnit));
    syncViews();
  }

  public void setRowNumber(int rowNumber) {
    mRowNumber = rowNumber;
    mRowNumberTextView.setText(String.format(Locale.getDefault(), "%d", rowNumber + 1));
  }

  public int getRowNumber() {
    return mRowNumber;
  }

  public void onEnterActionMode() {
    mInActionMode = true;
    syncViews();
  }

  public void onExitActionMode() {
    mInActionMode = false;
    syncViews();
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

  public Optional<Integer> getFocusedViewId() {
    if (mCostEditText.isFocused()) {
      return Optional.of(COST);
    }

    if (mQuantityEditText.isFocused()) {
      return Optional.of(QUANTITY);
    }

    if (mSizeEditText.isFocused()) {
      return Optional.of(SIZE);
    }

    return Optional.absent();
  }

  public void setFocusedViewId(int id) {
    if (id == COST) {
      mCostEditText.requestFocus();
      return;
    }

    if (id == QUANTITY) {
      mQuantityEditText.requestFocus();
      return;
    }

    if (id == SIZE) {
      mSizeEditText.requestFocus();
      return;
    }

    if (BuildConfig.DEBUG) {
      throw new IllegalArgumentException();
    }
  }

  public Optional<UnitEntry> getEntry() {
    try {
      UnitEntry.Builder unitEntry = UnitEntry.builder();

      unitEntry.setCostString(mCostEditText.getText().toString());
      unitEntry.setCost(Localization.parseDoubleOrThrow(mCostEditText.getText().toString()));

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
        unitEntry.setSize(Localization.parseDoubleOrThrow(mSizeEditText.getText().toString()));
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
        TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600, getResources().getDisplayMetrics());

    if (oneLine) {
      setOrientation(HORIZONTAL);
    } else {
      setOrientation(VERTICAL);
    }

    mRowNumberTextView = findViewById(R.id.text_ordinal);

    mCostEditText = findViewById(R.id.price_edit_text);
    mCostEditText.addTextChangedListener(mTextWatcher);
    Localization.addLocalizedKeyListener(mCostEditText);

    mQuantityEditText = findViewById(R.id.number_edit_text);
    mQuantityEditText.addTextChangedListener(mTextWatcher);
    Localization.addLocalizedKeyListener(mQuantityEditText);

    mSizeEditText = findViewById(R.id.size_edit_text);
    mSizeEditText.addTextChangedListener(mTextWatcher);
    Localization.addLocalizedKeyListener(mSizeEditText);

    mUnitSpinner = findViewById(R.id.unit_spinner);

    inlineSummaryTextView = findViewById(R.id.text_summary_inline);
    belowSummaryTextView = findViewById(R.id.text_summary_below);
    noteTextView = findViewById(R.id.text_note);
    noteTextView.setOnClickListener(noteOnClickListener);

    noteButton = findViewById(R.id.note_button);
    noteButton.setOnClickListener(noteOnClickListener);

    mInputFieldsContainer = findViewById(R.id.input_fields_container);

    mInflated = true;

    if (!this.isInEditMode()) {
      refreshAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType()));
      mUnitSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          DefaultUnit unit = ((UnitArrayAdapter) parent.getAdapter()).getUnit(position);
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
    return ((UnitArrayAdapter) mUnitSpinner.getAdapter())
        .getUnit(mUnitSpinner.getSelectedItemPosition());
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
    if (!mInflated) {
      return;
    }

    int focusedColor = 0xffDDDDDD;
    int unfocusedColor = Color.TRANSPARENT;

    TextView summaryTextView;
    if (Strings.isNullOrEmpty(note)) {
      noteTextView.setVisibility(View.GONE);
      summaryTextView = inlineSummaryTextView;
      belowSummaryTextView.setVisibility(View.GONE);
      noteButton.setText(R.string.add_note);
    } else {
      noteTextView.setVisibility(View.VISIBLE);
      noteTextView.setText(note);
      summaryTextView = belowSummaryTextView;
      inlineSummaryTextView.setVisibility(View.GONE);
      noteButton.setText(R.string.edit_note);
    }

    Optional<UnitEntry> unitEntry = getEntry();
    if (unitEntry.isPresent() && mLastCompareUnit != null) {
      Unit baseUnit = mLastCompareUnit.getUnit();
      if (unitEntry.get().getUnit().getUnitType() != baseUnit.getUnitType()) {
        return;
      }
      summaryTextView.setText(getSummaryText(unitEntry.get(), baseUnit));
      summaryTextView.setVisibility(View.VISIBLE);

      mRowNumberTextView.setTextColor(
          ContextCompat.getColor(getContext(), mEvaluation.getPrimaryColor()));
      summaryTextView.setTextColor(
          ContextCompat.getColor(getContext(), mEvaluation.getSecondaryColor()));

    } else {
      summaryTextView.setVisibility(View.INVISIBLE);
      mRowNumberTextView.setTextColor(
          ContextCompat.getColor(getContext(), Evaluation.NEUTRAL.getPrimaryColor()));
      summaryTextView.setTextColor(
          ContextCompat.getColor(getContext(), Evaluation.NEUTRAL.getSecondaryColor()));
    }
    setBackgroundColor(mInActionMode ? focusedColor : unfocusedColor);

  }

  public void clear() {
    withoutTextWatcher(mCostEditText, editText -> editText.setText(""));
    withoutTextWatcher(mQuantityEditText, editText -> editText.setText(""));
    withoutTextWatcher(mSizeEditText, editText -> editText.setText(""));
    note = "";
    refreshAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType().getBase()));
    syncViews();
  }

  private void withoutTextWatcher(EditText editText, Consumer<EditText> consumer) {
    editText.removeTextChangedListener(mTextWatcher);
    consumer.consume(editText);
    editText.addTextChangedListener(mTextWatcher);
  }

  public void setEvaluation(Evaluation evaluation) {
    mEvaluation = evaluation;
    syncViews();
  }

  public void setOnUnitEntryChangedListener(OnUnitEntryChangedListener listener) {
    mListener = listener;
  }

  private String getSummaryText(UnitEntry unitEntry, Unit baseUnit) {
    double baseSize = Localization.parseDoubleOrThrow(mLastCompareUnit.getSize());
    double pricePer = unitEntry.pricePer(baseSize, baseUnit);

    String formattedPricePer = units.getFormatter().apply(pricePer);

    if (baseSize == 1) {
      return getResources().getString(R.string.m_per_u,
          formattedPricePer,
          baseUnit.getSymbol(getResources()));
    } else {
      return getResources().getString(R.string.m_per_s_u,
          formattedPricePer,
          mLastCompareUnit.getSize(),
          baseUnit.getSymbol(getResources()));
    }
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
