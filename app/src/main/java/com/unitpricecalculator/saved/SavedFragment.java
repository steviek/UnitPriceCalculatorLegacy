package com.unitpricecalculator.saved;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import com.google.common.base.Strings;
import com.squareup.otto.Bus;
import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.events.SavedComparisonDeletedEvent;
import com.unitpricecalculator.inject.FragmentScoped;
import com.unitpricecalculator.util.AlertDialogs;
import com.unitpricecalculator.util.Localization;
import com.unitpricecalculator.util.abstracts.AbstractTextWatcher;
import dagger.android.ContributesAndroidInjector;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.inject.Inject;

public class SavedFragment extends BaseFragment {

  @dagger.Module
  public interface Module {

    @ContributesAndroidInjector()
    @FragmentScoped
    SavedFragment contributeAndroidInjector();

  }

  @Inject
  SavedComparisonManager savedComparisonManager;
  @Inject
  Callback callback;
  @Inject
  Bus bus;

  private ActionMode actionMode;
  private int selectedPosition;

  private SavedComparisonsArrayAdapter adapter;
  private List<SavedComparison> savedComparisons;
  private ArrayList<SavedComparison> filteredComparisons;

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
            EditText nameEditText = new EditText(getContext());
            int sideMargin = getResources().getDimensionPixelOffset(R.dimen.horizontal_margin);
            nameEditText.setInputType(InputType.TYPE_CLASS_TEXT);
            nameEditText.setHint(R.string.enter_name);
            String name = adapter.getItem(selectedPosition).getName();
            nameEditText.setText(name);
            nameEditText.setSelectAllOnFocus(true);
            nameEditText.requestFocus();

            alertDialog = new AlertDialog.Builder(getContext())
                .setMessage(R.string.give_name)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                  String savedName = nameEditText.getText().toString();
                  if (!Strings.isNullOrEmpty(savedName)) {
                    SavedComparison old = adapter.getItem(selectedPosition);
                    SavedComparison renamed = old.rename(savedName);
                    savedComparisonManager.putSavedComparison(renamed);
                    savedComparisons.remove(old);
                    savedComparisons.add(selectedPosition, renamed);
                    adapter.notifyDataSetChanged();
                  }
                  if (actionMode != null) {
                    actionMode.finish();
                  }
                }).setNegativeButton(android.R.string.cancel, (dialog, which) -> {
                  if (actionMode != null) {
                    actionMode.finish();
                  }
                }).setOnDismissListener(dialog -> alertDialog = null).create();
            alertDialog.setView(nameEditText, sideMargin, 0, sideMargin, 0);

            nameEditText.addTextChangedListener(new AbstractTextWatcher() {
              @Override
              public void afterTextChanged(Editable s) {
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
                    .setEnabled(!Strings.isNullOrEmpty(s.toString()));
              }
            });
          }

          if (alertDialog.isShowing()) {
            alertDialog.dismiss();
          } else {
            alertDialog.show();
            AlertDialogs.materialize(alertDialog);
          }

          return true;
        case R.id.action_delete:
          SavedComparison comparison = adapter.getItem(selectedPosition);
          savedComparisons.remove(comparison);
          filteredComparisons.remove(comparison);
          savedComparisonManager.removeSavedComparison(comparison);
          bus.post(new SavedComparisonDeletedEvent(comparison.getKey()));
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
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    setHasOptionsMenu(true);
    return inflater.inflate(R.layout.fragment_saved, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    savedComparisons = savedComparisonManager.getSavedComparisons();
    filteredComparisons = new ArrayList<>(savedComparisons);

    ListView listView = view.findViewById(R.id.list_view);
    adapter = new SavedComparisonsArrayAdapter(getContext(), filteredComparisons);
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

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public void onPrepareOptionsMenu(@NonNull Menu menu) {
    super.onPrepareOptionsMenu(menu);
    getActivity().getMenuInflater().inflate(R.menu.menu_saved_comparisons, menu);
    getBaseActivity().getSupportActionBar().setCustomView(null);
    getBaseActivity().getSupportActionBar().setDisplayShowCustomEnabled(false);
    getActivity().setTitle(R.string.saved_comparisons);
    SearchView searchView = (SearchView) menu.findItem(R.id.action_search).getActionView();
    searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
      @Override
      public boolean onQueryTextSubmit(String query) {
        return true;
      }

      @Override
      public boolean onQueryTextChange(String newText) {
        if (savedComparisons == null || filteredComparisons == null) {
          return true;
        }

        if (newText == null || newText.isEmpty()) {
          if (filteredComparisons.size() == savedComparisons.size()) {
            // Nothing to do, all the comparisons are already set.
            return true;
          } else {
            filteredComparisons.clear();
            filteredComparisons.addAll(savedComparisons);
            adapter.notifyDataSetChanged();
          }
        } else {
          String queryWithoutAccentsInLowerCase =
              Localization.stripAccents(newText).toLowerCase(Locale.getDefault());
          ArrayList<SavedComparison> newFilteredComparisons = new ArrayList<>();
          for (SavedComparison comparison : savedComparisons) {
            if (Localization.stripAccents(
                comparison.getName()).toLowerCase(Locale.getDefault())
                .contains(queryWithoutAccentsInLowerCase)) {
              newFilteredComparisons.add(comparison);
            }
          }
          if (filteredComparisons.equals(newFilteredComparisons)) {
            return true;
          }
          filteredComparisons.clear();
          filteredComparisons.addAll(newFilteredComparisons);
          adapter.notifyDataSetChanged();
        }
        return true;
      }
    });
  }

  public interface Callback {

    void onLoadSavedComparison(SavedComparison comparison);
  }
}
