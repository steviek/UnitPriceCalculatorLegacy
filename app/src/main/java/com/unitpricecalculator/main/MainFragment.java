package com.unitpricecalculator.main;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.unitpricecalculator.BaseFragment;
import com.unitpricecalculator.R;

public final class MainFragment extends BaseFragment {

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_main, container, false);

    return view;
  }
}
