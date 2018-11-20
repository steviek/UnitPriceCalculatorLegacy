package com.unitpricecalculator.comparisons;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.common.base.Function;
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
import com.unitpricecalculator.events.CurrencyChangedEvent;
import com.unitpricecalculator.events.NoteChangedEvent;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.inject.FragmentScoped;
import com.unitpricecalculator.saved.SavedComparisonManager;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitEntry;
import com.unitpricecalculator.unit.UnitType;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.AlertDialogs;
import com.unitpricecalculator.util.MenuItems;
import com.unitpricecalculator.util.NumberUtils;
import com.unitpricecalculator.util.SavesState;
import com.unitpricecalculator.util.abstracts.AbstractOnItemSelectedListener;
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher;
import com.unitpricecalculator.util.logger.Logger;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;
import com.unitpricecalculator.util.sometimes.MutableSometimes;
import dagger.android.ContributesAndroidInjector;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
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
  @Inject
  AppCompatActivity activity;
  @Inject
  SavedComparisonManager savedComparisonManager;

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
  private TextView savedChangesStatus;
  private View savedChangesDivider;
  private ActionMode actionMode;
  private final MutableSometimes<EditText> fileNameEditText = MutableSometimes.create();
  private final MutableSometimes<MenuItem> saveMenuItem = MutableSometimes.create();
  private final MutableSometimes<Object> resumed = MutableSometimes.create();

  private List<UnitEntryView> mEntryViews = new ArrayList<>();
  private final Handler handler = new Handler();

  private Optional<Integer> savedChangesCountdown = Optional.absent();

  private final Runnable savedChangesCountdownTick = new Runnable() {
    @Override
    public void run() {
      if (savedChangesStatus == null || !savedChangesCountdown.isPresent()
          || getContext() == null) {
        return;
      }

      int tick = savedChangesCountdown.get();
      savedChangesStatus.setText(getString(R.string.all_changes_saved, tick));
      if (tick >= 1) {
        savedChangesCountdown = Optional.of(tick - 1);
        handler.postDelayed(this, TimeUnit.SECONDS.toMillis(1));
      } else {
        savedChangesCountdown = Optional.absent();
        savedChangesStatus.setVisibility(View.GONE);
        savedChangesDivider.setVisibility(View.GONE);
      }
    }
  };

  @Nullable
  private SavedComparison pendingSavedStateToRestore;
  private String draftKey = String.valueOf(System.currentTimeMillis());
  private Optional<SavedComparison> lastKnownSavedState = Optional.absent();

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
      Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    View view = inflater.inflate(R.layout.fragment_main, container, false);

    savedChangesStatus = view.findViewById(R.id.saved_changes_status);
    savedChangesDivider = view.findViewById(R.id.saved_changes_divider);

    mPriceHeader = view.findViewById(R.id.price_header);
    mPriceHeader.setText(units.getCurrency().getSymbol());
    mPriceHeader.setOnClickListener(
        v -> currencies.showChangeCurrencyDialog());

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
        refreshViews();
      }
    });

    mFinalSpinner = view.findViewById(R.id.final_spinner);
    if (mFinalSpinner.getAdapter() == null) {
      mFinalSpinner.setAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType()));
      mFinalSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
          bus.post(getCompareUnit());
          refreshViews();
        }
      });
    }

    mSummaryText = view.findViewById(R.id.final_text_summary);

    addRowView();
    addRowView();

    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    LayoutInflater layoutInflater =
        LayoutInflater.from(new ContextThemeWrapper(getActivity(), R.style.FileNameEditText));
    EditText editText =
        (EditText) layoutInflater.inflate(R.layout.action_bar_edit_text, /* root= */ null);
    editText.setOnKeyListener((v, keyCode, event) -> {
      if (event.getAction() == KeyEvent.ACTION_DOWN &&
          keyCode == KeyEvent.KEYCODE_ENTER) {
        InputMethodManager imm =
            (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        return true;
      }
      return false;
    });
    editText.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {

      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {

      }

      @Override
      public void afterTextChanged(Editable s) {
        refreshViews();
      }
    });
    fileNameEditText.set(editText);
    if (lastKnownSavedState.isPresent()) {
      editText.setText(lastKnownSavedState.get().getName());
    }
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    fileNameEditText
        .whenPresent(editText -> {
          activity.getSupportActionBar().setCustomView(editText);
        });
    activity.getSupportActionBar().setDisplayShowCustomEnabled(true);
    activity.setTitle("");
    activity.getMenuInflater().inflate(R.menu.menu_main, menu);
    saveMenuItem.set(menu.findItem(R.id.action_save));
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
    refreshViews();
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
    refreshViews();
    return entryView;
  }

  @Override
  public SavedComparison saveState(Context context) {
    return getSavedState(context);
  }

  private SavedComparison getSavedState(Context context) {
    if (pendingSavedStateToRestore != null) {
      return pendingSavedStateToRestore;
    }

    ImmutableList.Builder<SavedUnitEntryRow> list = ImmutableList.builder();
    for (UnitEntryView entryView : mEntryViews) {
      list.add(entryView.saveState(context));
    }
    UnitType unitType = UnitType.fromName(
        unitTypeArrayAdapter.getItem(mUnitTypeSpinner.getSelectedItemPosition()),
        context.getResources());
    String finalSize = mFinalEditText.getText().toString();
    Unit finalUnit = ((UnitArrayAdapter) mFinalSpinner.getAdapter())
        .getUnit(mFinalSpinner.getSelectedItemPosition());
    String savedName =
        fileNameEditText.map(editText -> editText.getText().toString()).or("");

    String key = lastKnownSavedState.transform(SavedComparison::getKey)
        .or(draftKey);

    return new SavedComparison(key, savedName, unitType, list.build(), finalSize, finalUnit,
        units.getCurrency().getCurrencyCode());
  }

  @Override
  public void restoreState(SavedComparison comparison) {
    pendingSavedStateToRestore = comparison;

    if (mRowContainer == null || getContext() == null) {
      return;
    }

    Optional<EditText> fileNameEditTextOptional = fileNameEditText.toOptional();
    if (!fileNameEditTextOptional.isPresent()) {
      fileNameEditText.whenPresent(() -> restoreState(comparison));
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

    draftKey = comparison.getKey();

    fileNameEditTextOptional.get().setText(comparison.getName());

    adapter.notifyDataSetChanged();

    pendingSavedStateToRestore = null;
  }

  public void loadSavedComparison(SavedComparison savedComparison) {
    lastKnownSavedState = Optional.of(savedComparison);
    restoreState(savedComparison);
  }

  public void clear() {
    lastKnownSavedState = Optional.absent();

    if (!resumed.toOptional().isPresent()) {
      resumed.whenPresent(this::clear);
      return;
    }

    mRowContainer.removeAllViewsInLayout();
    mEntryViews.clear();
    addRowView();
    addRowView();
    mFinalEditText.setText("");
    mFinalSpinner.setAdapter(unitArrayAdapterFactory.create(units.getCurrentUnitType()));
    mSummaryText.setText("");
    fileNameEditText.whenPresent(editText -> editText.setText(""));
    draftKey = String.valueOf(System.currentTimeMillis());
    refreshViews();
  }

  public void save() {
    if (!Strings
        .isNullOrEmpty(fileNameEditText.map(editText -> editText.getText().toString()).orNull())) {
      save(getSavedState(getContext()));
      return;
    }

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
        String newName = name.getText().toString();
        Preconditions.checkState(!Strings.isNullOrEmpty(newName));
        fileNameEditText.whenPresent(editText -> {
          editText.setText(newName);
          save(getSavedState(getContext()));
        });
      });

      name.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
          if (mAlertDialog == null || s == null) {
            return;
          }

          mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE)
              .setEnabled(!Strings.isNullOrEmpty(s.toString()));
        }
      });
      alert.setNegativeButton(android.R.string.cancel, null);
      alert.setOnDismissListener(dialog -> mAlertDialog = null);
      mAlertDialog = alert.create();
    }

    if (mAlertDialog.isShowing()) {
      mAlertDialog.dismiss();
    } else {
      mAlertDialog.show();
      mAlertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
      mAlertDialog.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
      AlertDialogs.materialize(mAlertDialog);
    }
  }

  private void save(SavedComparison comparison) {
    if (!prefs.getBoolean(Keys.HAS_CLICKED_SAVE)) {
      prefs.putBoolean(Keys.HAS_CLICKED_SAVE, true);
      savedChangesCountdown = Optional.of(10);
      savedChangesCountdownTick.run();
    }

    Preconditions.checkNotNull(comparison.getKey());
    savedComparisonManager.putSavedComparison(comparison);
    lastKnownSavedState = Optional.of(comparison);
    refreshViews();
  }

  @Override
  public void onResume() {
    super.onResume();
    bus.register(this);
    if (pendingSavedStateToRestore != null) {
      restoreState(pendingSavedStateToRestore);
      refreshViews();
    }
    resumed.set(new Object());
  }

  @Override
  public void onPause() {
    super.onPause();
    bus.unregister(this);
    resumed.set(null);
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

  @Subscribe
  public void onNoteChanged(NoteChangedEvent noteChangedEvent) {
    refreshViews();
  }

  @Subscribe
  public void onCurrencyChanged(CurrencyChangedEvent event) {
    if (mPriceHeader != null) {
      mPriceHeader.setText(event.getCurrency().getSymbol());
      refreshViews();
    }
  }

  @Override
  public void onUnitEntryChanged(Optional<UnitEntry> unitEntry) {
    refreshViews();
    finishActionMode();
  }

  private CompareUnitChangedEvent getCompareUnit() {
    Unit unit = ((UnitArrayAdapter) mFinalSpinner.getAdapter())
        .getUnit(mFinalSpinner.getSelectedItemPosition());
    String size = NumberUtils.firstParsableDouble(
        mFinalEditText.getText().toString(), String.valueOf(unit.getDefaultQuantity()));
    return new CompareUnitChangedEvent(size, unit);
  }

  private void refreshViews() {
    SavedComparison currentState = getSavedState(getContext());

    boolean hasClickedSave = prefs.getBoolean(Keys.HAS_CLICKED_SAVE);
    if (hasClickedSave) {
      if (!savedChangesCountdown.isPresent()) {
        savedChangesStatus.setVisibility(View.GONE);
        savedChangesDivider.setVisibility(View.GONE);
      }
      saveMenuItem.whenPresent(item -> MenuItems.setEnabled(item,
          !currentState.isEmpty() && !(lastKnownSavedState.isPresent() && currentState
              .equals(lastKnownSavedState.get()))));
    } else {
      if (currentState.isEmpty() && lastKnownSavedState.transform(SavedComparison::isEmpty)
          .or(true)) {
        savedChangesStatus.setVisibility(View.INVISIBLE);
        saveMenuItem.whenPresent(item -> MenuItems.setEnabled(item, false));
        savedChangesDivider.setVisibility(View.INVISIBLE);
      } else if (currentState.equals(lastKnownSavedState.orNull())) {
        savedChangesStatus.setVisibility(View.VISIBLE);
        savedChangesStatus.setText(R.string.all_changes_saved);
        savedChangesDivider.setVisibility(View.VISIBLE);
        saveMenuItem.whenPresent(item -> MenuItems.setEnabled(item, false));
      } else {
        savedChangesStatus.setVisibility(View.VISIBLE);
        savedChangesStatus.setText(R.string.unsaved_changes);
        savedChangesDivider.setVisibility(View.VISIBLE);
        saveMenuItem.whenPresent(item -> MenuItems.setEnabled(item, true));
      }
    }

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

    StringBuilder finalSummary = new StringBuilder();
    UnitEntryWithIndex best = unitEntries.get(0);
    finalSummary.append(getString(R.string.main_final_summary, best.getIndex() + 1)).append('\n');
    appendSingleRowSummary(finalSummary, best.getUnitEntry(), compareUnit);

    finalSummary.append("\n\n");

    for (UnitEntryWithIndex entryWithIndex : unitEntries) {
      finalSummary.append(
          String.format(Locale.getDefault(), "%d: ", entryWithIndex.getIndex() + 1));

      appendSingleRowSummary(finalSummary, entryWithIndex.getUnitEntry(), compareUnit);
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
      CompareUnitChangedEvent compareUnitChangedEvent) {
    Unit compareUnit = compareUnitChangedEvent.getUnit();
    String compareSize = compareUnitChangedEvent.getSize();

    Function<Double, String> formatter = units.getFormatter();
    String formattedEntryCostString = formatter.apply(unitEntry.getCost());
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
        formatter.apply(unitEntry.pricePer(Double.parseDouble(compareSize), compareUnit));
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

    refreshViews();
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

    refreshViews();
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
