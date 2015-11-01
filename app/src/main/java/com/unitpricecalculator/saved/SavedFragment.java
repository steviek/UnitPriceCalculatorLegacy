package com.unitpricecalculator.saved;

import com.google.common.base.Strings;

import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.util.prefs.Keys;
import com.unitpricecalculator.util.prefs.Prefs;

import java.util.Collections;
import java.util.List;

public class SavedFragment extends BaseFragment {

    private ActionMode mActionMode;
    private Callback mCallback;
    private int mSelectedPosition;

    private SavedComparisonsArrayAdapter mAdapter;
    private List<SavedComparison> mSavedComparisons;

    private AlertDialog mAlertDialog;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {
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
                                String savedName = name.getText().toString();
                                if (!Strings.isNullOrEmpty(savedName)) {
                                    SavedComparison old = mAdapter.getItem(mSelectedPosition);
                                    SavedComparison renamed = old.rename(savedName);
                                    mSavedComparisons.remove(old);
                                    mSavedComparisons.add(renamed);
                                    Collections.sort(mSavedComparisons);
                                    mAdapter.notifyDataSetChanged();
                                    Prefs.putList(Keys.SAVED_STATES, mSavedComparisons);
                                }
                                if (mActionMode != null) {
                                    mActionMode.finish();
                                }
                            }
                        });
                        alert.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (mActionMode != null) {
                                    mActionMode.finish();
                                }
                            }
                        });
                        mAlertDialog = alert.create();
                    }

                    if (mAlertDialog.isShowing()) {
                        mAlertDialog.dismiss();
                    } else {
                        mAlertDialog.show();
                    }

                    return true;
                case R.id.action_delete:
                    SavedComparison comparison = mAdapter.getItem(mSelectedPosition);
                    mSavedComparisons.remove(comparison);
                    Prefs.putList(Keys.SAVED_STATES, mSavedComparisons);
                    mAdapter.notifyDataSetChanged();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            mSelectedPosition = -1;
            if (Build.VERSION.SDK_INT >= 21) {
                if (getActivity() != null && getActivity().getWindow() != null) {
                    getActivity().getWindow().setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
                }
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mCallback = castOrThrow(Callback.class, context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_saved, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSavedComparisons = Prefs.getList(SavedComparison.class, Keys.SAVED_STATES);
        Collections.sort(mSavedComparisons);

        ListView listView = (ListView) view.findViewById(R.id.list_view);
        mAdapter = new SavedComparisonsArrayAdapter(getContext(), mSavedComparisons);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mCallback.onLoadSavedComparison(mAdapter.getItem(position));
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                if (mActionMode != null) {
                    return false;
                }

                // Start the CAB using the ActionMode.Callback defined above
                mActionMode = getActivity().startActionMode(mActionModeCallback);
                mSelectedPosition = position;
                view.setSelected(true);
                return true;
            }
        });
    }

    public interface Callback {
        void onLoadSavedComparison(SavedComparison comparison);
    }
}
