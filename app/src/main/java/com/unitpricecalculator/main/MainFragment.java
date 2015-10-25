package com.unitpricecalculator.main;

import com.google.common.base.Optional;
import com.google.common.base.Strings;

import android.content.DialogInterface;
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

import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.MyApplication;
import com.unitpricecalculator.R;
import com.unitpricecalculator.events.CompareUnitChangedEvent;
import com.unitpricecalculator.events.SystemChangedEvent;
import com.unitpricecalculator.events.UnitTypeChangedEvent;
import com.unitpricecalculator.unit.Unit;
import com.unitpricecalculator.unit.UnitEntry;
import com.unitpricecalculator.unit.UnitType;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.NumberUtils;
import com.unitpricecalculator.util.SavesStateInJson;
import com.unitpricecalculator.util.abstracts.AbstractOnItemSelectedListener;
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher;
import com.unitpricecalculator.util.logger.Logger;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class MainFragment extends BaseFragment implements UnitEntryView.OnUnitEntryChangedListener,
        SavesStateInJson {

    private LinearLayout mRowContainer;
    private View mAddRowButton;
    private View mRemoveRowButton;
    private EditText mFinalEditText;
    private Spinner mFinalSpinner;
    private TextView mSummaryText;
    private AlertDialog mAlertDialog;

    private List<UnitEntryView> mEntryViews = new ArrayList<>();

    private JSONObject mSavedState;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        ((TextView) view.findViewById(R.id.price_header))
                .setText(Currency.getInstance(Locale.getDefault()).getSymbol());

        Spinner mUnitTypeSpinner = (Spinner) view.findViewById(R.id.unit_type_spinner);
        UnitTypeArrayAdapter mUnitTypeArrayAdapter = new UnitTypeArrayAdapter(getContext());
        mUnitTypeSpinner.setAdapter(mUnitTypeArrayAdapter);
        mUnitTypeSpinner.setOnItemSelectedListener(new AbstractOnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Logger.d("onItemSelected: %s", parent.getItemAtPosition(position));
                UnitType unitType = UnitType.fromName((String) parent.getItemAtPosition(position),
                        getResources());
                if (Units.getCurrentUnitType() != unitType) {
                    Units.setCurrentUnitType(unitType);
                    ((Spinner) parent).setAdapter(new UnitTypeArrayAdapter(getContext()));
                    mFinalSpinner.setAdapter(UnitArrayAdapter.of(getContext(), unitType));
                    CompareUnitChangedEvent event = getCompareUnit();
                    for (UnitEntryView entryView : mEntryViews) {
                        entryView.onCompareUnitChanged(event);
                    }
                    evaluateEntries();
                }
            }
        });

        mRowContainer = (LinearLayout) view.findViewById(R.id.row_container);

        for (int i = 0; i < mRowContainer.getChildCount(); i++) {
            UnitEntryView entryView = (UnitEntryView) mRowContainer.getChildAt(i);
            entryView.setOnUnitEntryChangedListener(this);
            mEntryViews.add(entryView);
            entryView.setRowNumber(i + 1);
        }

        mAddRowButton = view.findViewById(R.id.add_row_btn);
        mAddRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEntryViews.size() == 9) {
                    mAddRowButton.setEnabled(false);
                }
                addRowView();
            }
        });

        mRemoveRowButton = view.findViewById(R.id.remove_row_btn);
        mRemoveRowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEntryViews.size() == 0) {
                    throw new IllegalStateException();
                } else if (mEntryViews.size() == 1) {
                    mRemoveRowButton.setEnabled(false);
                }
                mAddRowButton.setEnabled(true);
                UnitEntryView entryView = mEntryViews.remove(mEntryViews.size() - 1);
                entryView.setOnUnitEntryChangedListener(null);
                mRowContainer.removeView(entryView);
                evaluateEntries();
            }
        });

        mFinalEditText = (EditText) view.findViewById(R.id.final_size);
        mFinalEditText.addTextChangedListener(new AbstractTextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                MyApplication.getInstance().getBus().post(getCompareUnit());
                evaluateEntries();
            }
        });

        mFinalSpinner = (Spinner) view.findViewById(R.id.final_spinner);
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

        mSummaryText = (TextView) view.findViewById(R.id.final_text_summary);

        return view;
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
    public JSONObject saveState() throws JSONException {
        JSONObject outState = new JSONObject();
        outState.put("numRows", mEntryViews.size());
        for (int i = 0; i < mEntryViews.size(); i++) {
            outState.put("row" + i, mEntryViews.get(i).saveState());
        }
        outState.put("finalSize", mFinalEditText.getText().toString());
        outState.put("finalUnit", Units.toJson(((UnitArrayAdapter) mFinalSpinner.getAdapter())
                .getUnit(mFinalSpinner.getSelectedItemPosition())));
        return outState;
    }

    @Override
    public void restoreState(JSONObject object) throws JSONException {
        if (mRowContainer == null || getContext() == null) {
            mSavedState = object;
            return;
        }

        int numRows = object.getInt("numRows");
        mRowContainer.removeAllViewsInLayout();
        mEntryViews.clear();
        for (int i = 0; i < numRows; i++) {
            UnitEntryView entryView = addRowView();
            entryView.restoreState(object.getJSONObject("row" + i));
        }
        mFinalEditText.setText(object.getString("finalSize"));
        Unit unit = Units.fromJson(object.getJSONObject("finalUnit"));
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
            final EditText name = new EditText(getContext());
            int sideMargin = getResources().getDimensionPixelOffset(R.dimen.horizontal_margin);
            name.setInputType(InputType.TYPE_CLASS_TEXT);
            name.setHint(R.string.enter_name);

            AlertDialog.Builder alert = new AlertDialog.Builder(getContext());
            alert.setMessage(R.string.give_name);
            alert.setView(name, sideMargin, 0, sideMargin, 0);
            alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        String savedName = name.getText().toString();
                        if (Strings.isNullOrEmpty(savedName)) {
                            DateFormat df = DateFormat.getDateTimeInstance();
                            savedName = getString(R.string.saved_from_date, df.format(new Date(0)));
                        }
                        SavedState state = new SavedState(savedName);
                        for (UnitEntryView entryView : mEntryViews) {
                            state.addSavedRow(entryView.saveState());
                        }
                        Set<String> savedStates = Prefs.getStringSet(Keys.SAVED_STATES);
                        savedStates.add(state.toJson().toString());
                        Prefs.putStringSet(Keys.SAVED_STATES, savedStates);
                    } catch (JSONException e) {
                        throw new IllegalStateException(e);
                    }
                }
            });
            alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
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
            try {
                restoreState(mSavedState);
            } catch (JSONException e) {
                Logger.e(e);
            }

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
        String size = NumberUtils.firstParsableDouble(mFinalEditText.getText().toString(), "1");
        Unit unit = ((UnitArrayAdapter) mFinalSpinner.getAdapter()).getUnit(mFinalSpinner.getSelectedItemPosition());
        return new CompareUnitChangedEvent(size, unit);
    }

    private void evaluateEntries() {
        CompareUnitChangedEvent compareUnit = getCompareUnit();
        double size = Double.parseDouble(compareUnit.getSize());
        Unit unit = compareUnit.getUnit();

        int entries = 0;
        int bestRow = 0;
        UnitEntry best = null;

        for (int i = 0; i < mEntryViews.size(); i ++) {
            UnitEntryView entryView = mEntryViews.get(i);
            Optional<UnitEntry> entry = entryView.getEntry();
            if (entry.isPresent()) {
                entries++;
                if (best == null || entry.get().pricePer(size, unit) < best.pricePer(size, unit)) {
                    best = entry.get();
                    bestRow = i;
                }
            }
        }

        if (entries >= 2 && best != null) {
            NumberFormat format = NumberFormat.getCurrencyInstance();
            mSummaryText.setText(getResources().getString(R.string.main_final_summary,
                    bestRow + 1,
                    format.format(best.getCost()),
                    best.getQuantityString(),
                    best.getSizeString(),
                    best.getUnit().getSymbol(),
                    format.format(best.pricePer(size, unit)),
                    compareUnit.getSize(),
                    unit.getSymbol()));

            for (UnitEntryView entryView : mEntryViews) {
                Optional<UnitEntry> entry = entryView.getEntry();
                if (entry.isPresent()) {
                    if (entry.get().pricePer(size, unit) <= best.pricePer(size, unit)) {
                        entryView.setEvaluation(UnitEntryView.Evaluation.GOOD);
                    } else {
                        entryView.setEvaluation(UnitEntryView.Evaluation.BAD);
                    }
                } else {
                    entryView.setEvaluation(UnitEntryView.Evaluation.NEUTRAL);
                }
            }
        } else {
            mSummaryText.setText("");
            for (UnitEntryView entryView : mEntryViews) {
                entryView.setEvaluation(UnitEntryView.Evaluation.NEUTRAL);
            }
        }
    }

    private static class SavedState {
        private String name;
        private List<JSONObject> savedRows;

        SavedState(String name) {
            this.name = name;
            this.savedRows = new ArrayList<>();
        }

        public void addSavedRow(JSONObject row) {
            savedRows.add(row);
        }

        public String getName() {
            return name;
        }

        public List<JSONObject> getSavedRows() {
            return savedRows;
        }

        public JSONObject toJson() throws JSONException {
            JSONObject json = new JSONObject();
            json.put("name", name);
            JSONArray array = new JSONArray();
            for (JSONObject row : savedRows) {
                array.put(row);
            }
            json.put("savedRows", savedRows);
            return json;
        }

        public static SavedState fromJson(JSONObject object) {
            try {
                String name = object.getString("name");
                SavedState savedState = new SavedState(name);
                JSONArray array = object.getJSONArray("savedRows");
                for (int i = 0; i < array.length(); i++) {
                    savedState.addSavedRow(array.getJSONObject(i));
                }
                return savedState;
            } catch (JSONException e) {
                throw new IllegalArgumentException(e);
            }
        }

    }
}
