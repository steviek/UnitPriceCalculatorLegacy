package com.unitpricecalculator.util;

import android.content.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface SavesState<T> {
    @Nullable
    T saveState(@NotNull Context context);

    void restoreState(@NotNull T object);
}
