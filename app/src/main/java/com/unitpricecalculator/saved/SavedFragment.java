package com.unitpricecalculator.saved;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import com.google.common.base.Strings;
import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.inject.FragmentScoped;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;
import dagger.android.ContributesAndroidInjector;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

public class SavedFragment extends BaseFragment {

    @dagger.Module
    public interface Module {
        @ContributesAndroidInjector
        @FragmentScoped
        SavedFragment contributeAndroidInjector();
    }

    @Inject Prefs prefs;
    @Inject Callback callback;

    private ActionMode actionMode;
    private int selectedPosition;

    private SavedComparisonsArrayAdapter adapter;
    private List<SavedComparison> savedComparisons;

    private AlertDialog alertDialog;
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.menu_action, menu);
            if (Build.VERSION.SDK_INT >= 21) {
                getActivity().getWindow().setStatusBarColor(Color.BLACK);
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_rename:
                    if (alertDialog == null) {
                        final EditText name = new EditText(getContext());
                        int sideMargin = getResources().getDimensionPixelOffset(R.dimen.horizontal_margin);
                        name.setInputType(InputType.TYPE_CLASS_TEXT);
                        name.setHint(R.string.enter_name);

                        alertDialog = new AlertDialog.Builder(getContext())
                        .setMessage(R.string.give_name)
                        .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                            String savedName = name.getText().toString();
                            if (!Strings.isNullOrEmpty(savedName)) {
                                SavedComparison old = adapter.getItem(selectedPosition);
                                SavedComparison renamed = old.rename(savedName);
                                savedComparisons.remove(old);
                                savedComparisons.add(renamed);
                                Collections.sort(savedComparisons);
                                adapter.notifyDataSetChanged();
                                prefs.putList(Keys.SAVED_STATES, savedComparisons);
                            }
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                        }).setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                            if (actionMode != null) {
                                actionMode.finish();
                            }
                        }).create();
                        alertDialog.setView(name, sideMargin, 0, sideMargin, 0);
                    }

                    if (alertDialog.isShowing()) {
                        alertDialog.dismiss();
                    } else {
                        alertDialog.show();
                    }

                    return true;
                case R.id.action_delete:
                    SavedComparison comparison = adapter.getItem(selectedPosition);
                    savedComparisons.remove(comparison);
                    prefs.putList(Keys.SAVED_STATES, savedComparisons);
                    adapter.notifyDataSetChanged();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            selectedPosition = -1;
            if (Build.VERSION.SDK_INT >= 21) {
                if (getActivity() != null && getActivity().getWindow() != null) {
                    getActivity()
                            .getWindow()
                            .setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        savedComparisons = prefs.getList(SavedComparison.class, Keys.SAVED_STATES);
        Collections.sort(savedComparisons);

        ListView listView = view.findViewById(R.id.list_view);
        adapter = new SavedComparisonsArrayAdapter(getContext(), savedComparisons);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view1, position, id) ->
                callback.onLoadSavedComparison(adapter.getItem(position)));
        listView.setOnItemLongClickListener((parent, view12, position, id) -> {
            if (actionMode != null) {
                return false;
            }

            // Start the CAB using the ActionMode.Callback defined above
            actionMode = getActivity().startActionMode(actionModeCallback);
            selectedPosition = position;
            view12.setSelected(true);
            return true;
        });
    }

    public interface Callback {
        void onLoadSavedComparison(SavedComparison comparison);
    }
}
