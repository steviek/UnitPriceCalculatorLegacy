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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BuildConfig;
import com.unitpricecalculator.R;
import com.unitpricecalculator.databinding.EnterNoteEditTextBinding;
import com.unitpricecalculator.databinding.UnitEntryViewBinding;
import com.unitpricecalculator.events.CompareUnitChangedEvent;
import com.unitpricecalculator.events.NoteChangedEvent;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.locale.AppLocaleManager;
import com.unitpricecalculator.unit.DefaultUnit;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitEntry;
import com.unitpricecalculator.unit.UnitFormatter;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.Consumer;
import com.unitpricecalculator.util.Localization;
import com.unitpricecalculator.util.SavesState;
import com.unitpricecalculator.util.abstracts.AbstractOnItemSelectedListener;
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher;
import com.unitpricecalculator.util.logger.Logger;

import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class UnitEntryView extends LinearLayout implements SavesState<SavedUnitEntryRow> {

  private static final int COST = 0;
  private static final int SIZE = 1;
  private static final int QUANTITY = 2;

  @Inject
  Activity activity;
  @Inject
  Units units;
  @Inject
  UnitArrayAdapterFactory unitArrayAdapterFactory;
  @Inject
  Bus bus;
  @Inject
  UnitFormatter unitFormatter;

  private TextView mRowNumberTextView;
  private EditText mCostEditText;
  private EditText mQuantityEditText;
  private EditText mSizeEditText;
  private Spinner mUnitSpinner;
  private View mInputFieldsContainer;

  private TextView summaryTextView;
  private TextView belowNoteTextView;
  private TextView inlineNoteTextView;
  private Button addNoteButton;
  private ImageButton editNoteButton;
  private CompareUnitChangedEvent mLastCompareUnit;

  private OnUnitEntryChangedListener mListener;

  private DefaultUnit mUnit;
  private Evaluation mEvaluation = Evaluation.NEUTRAL;

  private final TextWatcher mTextWatcher = new AbstractTextWatcher() {
    @Override
    public void afterTextChanged(Editable s) {
      onUnitChanged();
    }
  };

  private boolean mInflated = false;
  private boolean mInActionMode = false;
  private boolean mExpanded = false;
  private int mRowNumber;
  @Nullable
  private String note;

  private final OnClickListener noteOnClickListener = v -> {
    EnterNoteEditTextBinding binding =
        EnterNoteEditTextBinding.inflate(LayoutInflater.from(getContext()));
    EditText editText = binding.input;
    int margin = (int) (getResources().getDisplayMetrics().density * 32);
    editText.setText(note);
    editText.setSelection(editText.length());
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

    AlertDialog alertDialog = new MaterialAlertDialogBuilder(getContext())
        .setPositiveButton(android.R.string.ok, onClickListener)
        .setNegativeButton(android.R.string.cancel, onClickListener)
        .setNeutralButton(R.string.delete, onClickListener)
        .create();
    alertDialog.setView(binding.getRoot(), margin, margin, margin, 0);
    alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
    alertDialog.setOnShowListener(
        dialog -> alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setEnabled(!Strings.isNullOrEmpty(note)));
    alertDialog.show();
    if (Strings.isNullOrEmpty(note)) {
      alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL).setVisibility(View.GONE);
    }
    editText.addTextChangedListener(new AbstractTextWatcher() {
      @Override
      public void afterTextChanged(Editable s) {
        alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
            .setEnabled(!s.toString().isEmpty() && !s.toString().equals(note));
      }
    });
  };

  public UnitEntryView(Context context) {
    super(context);
    setLayoutDirection(View.LAYOUT_DIRECTION_LOCALE);
    setTextDirection(View.TEXT_DIRECTION_LOCALE);
    LayoutInflater.from(context).inflate(R.layout.unit_entry_view, this);
    onFinishInflate();
  }

  @NonNull
  @Override
  public SavedUnitEntryRow saveState(@NonNull Context context) {
    return new SavedUnitEntryRow(
        mCostEditText.getText().toString(),
        mQuantityEditText.getText().toString(),
        mSizeEditText.getText().toString(),
        mUnit,
        note);
  }

  @Override
  public void restoreState(SavedUnitEntryRow entryRow) {
    Logger.d("Start restoring " + entryRow + " in " + this);
    withoutTextWatcher(mCostEditText, editText -> editText.setText(entryRow.getCost()));
    withoutTextWatcher(mSizeEditText, editText -> editText.setText(entryRow.getSize()));
    withoutTextWatcher(mQuantityEditText, editText -> editText.setText(entryRow.getQuantity()));
    mUnit = entryRow.getUnit();
    note = entryRow.getNote();
    refreshAdapter(unitArrayAdapterFactory.create(mUnit));
    syncViews();
    Logger.d("Cost after restore: " + mCostEditText.getText());
    Logger.d("Done restoring " + entryRow + ", entry is " + getEntry() + " in " + this);
  }

  public void setRowNumber(int rowNumber) {
    mRowNumber = rowNumber;
    mRowNumberTextView.setText(
        String.format(AppLocaleManager.getInstance().getCurrentLocale(), "%d", rowNumber + 1)
    );
  }

  public int getRowNumber() {
    return mRowNumber;
  }

  @Nullable
  public String getNote() {
    return note;
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
    refreshAdapter(
        unitArrayAdapterFactory.create(units.getDefaultQuantity(event.getUnitType()).getUnit()));
  }

  @Subscribe
  public void onSystemOrderChanged(SystemChangedEvent event) {
    refreshAdapter(unitArrayAdapterFactory.create(units.getDefaultQuantity().getUnit()));
  }

  @Subscribe
  public void onCompareUnitChanged(CompareUnitChangedEvent event) {
    mLastCompareUnit = event;
    syncViews();
  }

  @Nullable
  public Integer getFocusedViewId() {
    if (mCostEditText.isFocused()) {
      return COST;
    }

    if (mQuantityEditText.isFocused()) {
      return QUANTITY;
    }

    if (mSizeEditText.isFocused()) {
      return SIZE;
    }

    return null;
  }

  public void setFocusedViewId(@Nullable Integer id) {
    if (id == null) {
      return;
    }

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
      UnitEntry.Builder unitEntry = new UnitEntry.Builder();

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
      // This is lazy, but we'll just return absent if anything fails.
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

    Logger.d("onFinishInlate: " + this);

    boolean oneLine = getResources().getDisplayMetrics().widthPixels >=
        TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 600, getResources().getDisplayMetrics());

    if (oneLine) {
      setOrientation(HORIZONTAL);
    } else {
      setOrientation(VERTICAL);
    }

    UnitEntryViewBinding viewBinding = UnitEntryViewBinding.bind(this);

    mRowNumberTextView = viewBinding.textOrdinal;

    mCostEditText = viewBinding.priceEditText;
    mCostEditText.addTextChangedListener(mTextWatcher);
    Localization.addLocalizedKeyListener(mCostEditText);

    Locale locale = AppLocaleManager.getInstance().getCurrentLocale();

    mQuantityEditText = viewBinding.numberEditText;
    mQuantityEditText.setHint(String.format(locale, "%d", 1));
    mQuantityEditText.addTextChangedListener(mTextWatcher);
    Localization.addLocalizedKeyListener(mQuantityEditText);

    mSizeEditText = viewBinding.sizeEditText;
    mSizeEditText.setHint(String.format(locale, "%d", 1));
    mSizeEditText.addTextChangedListener(mTextWatcher);
    Localization.addLocalizedKeyListener(mSizeEditText);

    mUnitSpinner = viewBinding.unitSpinner;

    summaryTextView = viewBinding.textSummaryInline;
    belowNoteTextView = viewBinding.textNoteBelow;
    belowNoteTextView.setOnClickListener(v -> {
      mExpanded = !mExpanded;
      syncViews();
    });
    inlineNoteTextView = viewBinding.textNoteInline;
    inlineNoteTextView.setOnClickListener(v -> {
      mExpanded = !mExpanded;
      syncViews();
    });

    addNoteButton = viewBinding.noteButton;
    addNoteButton.setOnClickListener(noteOnClickListener);
    editNoteButton = viewBinding.editNoteButton;
    editNoteButton.setOnClickListener(noteOnClickListener);

    mInputFieldsContainer = viewBinding.inputFieldsContainer;

    mInflated = true;

    if (!this.isInEditMode()) {
      refreshAdapter(unitArrayAdapterFactory.create(units.getDefaultQuantity().getUnit()));
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
      mListener.onUnitEntryChanged(this, unitEntry);
    }
  }

  private void syncViews() {
    if (!mInflated) {
      return;
    }

    int unfocusedColor = Color.TRANSPARENT;

    if (Strings.isNullOrEmpty(note)) {
      belowNoteTextView.setVisibility(View.GONE);
      inlineNoteTextView.setVisibility(View.INVISIBLE);

      belowNoteTextView.setVisibility(View.GONE);
      addNoteButton.setVisibility(View.VISIBLE);
      editNoteButton.setVisibility(View.GONE);
    } else {
      belowNoteTextView.setText(note);
      inlineNoteTextView.setText(note);
      addNoteButton.setVisibility(View.GONE);
      editNoteButton.setVisibility(View.VISIBLE);

      if (mExpanded) {
        belowNoteTextView.setVisibility(View.VISIBLE);
        inlineNoteTextView.setVisibility(View.INVISIBLE);
      } else {
        belowNoteTextView.setVisibility(View.GONE);
        inlineNoteTextView.setVisibility(View.VISIBLE);
      }
    }

    Optional<UnitEntry> unitEntry = getEntry();
    if (unitEntry.isPresent() && mLastCompareUnit != null) {
      Unit baseUnit = mLastCompareUnit.getUnit();
      if (unitEntry.get().getUnit().getUnitType() != baseUnit.getUnitType()) {
        return;
      }
      this.summaryTextView.setText(getSummaryText(unitEntry.get(), baseUnit));
      this.summaryTextView.setVisibility(View.VISIBLE);

      mRowNumberTextView.setTextColor(
          ContextCompat.getColor(getContext(), mEvaluation.getPrimaryColor()));
      this.summaryTextView.setTextColor(
          ContextCompat.getColor(getContext(), mEvaluation.getSecondaryColor()));

    } else {
      this.summaryTextView.setVisibility(View.INVISIBLE);
      mRowNumberTextView.setTextColor(
          ContextCompat.getColor(getContext(), Evaluation.NEUTRAL.getPrimaryColor()));
      this.summaryTextView.setTextColor(
          ContextCompat.getColor(getContext(), Evaluation.NEUTRAL.getSecondaryColor()));
    }
    setBackgroundColor(
        mInActionMode
            ? ContextCompat.getColor(getContext(), R.color.row_focused_color)
            : unfocusedColor);

  }

  public void clear() {
    withoutTextWatcher(mCostEditText, editText -> editText.setText(""));
    withoutTextWatcher(mQuantityEditText, editText -> editText.setText(""));
    withoutTextWatcher(mSizeEditText, editText -> editText.setText(""));
    note = "";
    refreshAdapter(unitArrayAdapterFactory.create(units.getDefaultQuantity().getUnit()));
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

    String formattedPricePer = units.getFormatter().format(pricePer);

    return getResources().getString(R.string.m_per_u,
        formattedPricePer,
        unitFormatter.format((DefaultUnit) baseUnit, baseSize, mLastCompareUnit.getSize()));
  }

  public interface OnUnitEntryChangedListener {

    void onUnitEntryChanged(UnitEntryView view, Optional<UnitEntry> unitEntry);
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

    @ColorRes
    public int getPrimaryColor() {
      return primaryColor;
    }

    @ColorRes
    public int getSecondaryColor() {
      return secondaryColor;
    }
  }
}
