package com.unitpricecalculator.util;

public interface SavesState<T extends Jsonable> {
    T saveState();

    void restoreState(T object);
}
