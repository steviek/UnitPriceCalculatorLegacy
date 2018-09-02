package com.unitpricecalculator;

import android.content.Context;
import android.support.v4.app.Fragment;

import dagger.android.support.AndroidSupportInjection;

public abstract class BaseFragment extends Fragment {

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    protected <T> T castOrThrow(Class<T> clazz, Object object) {
        try {
            return clazz.cast(object);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(
                    object.getClass() + " must implement " + clazz + " interface");
        }
    }

}
