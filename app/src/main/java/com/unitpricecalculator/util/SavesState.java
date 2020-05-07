package com.unitpricecalculator.util;

import android.content.Context;
import org.jetbrains.annotations.NotNull;

public interface SavesState<T> {
    @NotNull
    T saveState(@NotNull Context context);

    void restoreState(@NotNull T object);
}
