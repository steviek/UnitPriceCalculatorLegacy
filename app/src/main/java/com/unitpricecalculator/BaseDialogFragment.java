package com.unitpricecalculator;

import android.content.Context;
import androidx.fragment.app.DialogFragment;
import dagger.android.support.AndroidSupportInjection;

public abstract class BaseDialogFragment extends DialogFragment {

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }
}
