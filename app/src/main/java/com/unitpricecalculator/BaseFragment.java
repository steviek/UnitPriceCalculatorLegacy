package com.unitpricecalculator;

import android.support.v4.app.Fragment;

public abstract class BaseFragment extends Fragment {

    protected <T> T castOrThrow(Class<T> clazz, Object object) {
        try {
            return clazz.cast(object);
        } catch (ClassCastException e) {
            throw new IllegalArgumentException(object.getClass() + " must implement " + clazz + " interface");
        }
    }

}
