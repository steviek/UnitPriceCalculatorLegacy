package com.unitpricecalculator.util;

import android.content.Context;

public interface SavesState<T> {
    T saveState(Context context);

    void restoreState(T object);
}
