package com.unitpricecalculator.util;

public interface SavesState<T> {
    T saveState();

    void restoreState(T object);
}
