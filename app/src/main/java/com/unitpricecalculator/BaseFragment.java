package com.unitpricecalculator;

import android.content.Context;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {

  @Override
  public void onAttach(Context context) {
    super.onAttach(context);
  }

  protected BaseActivity getBaseActivity() {
    return (BaseActivity) getActivity();
  }
}
