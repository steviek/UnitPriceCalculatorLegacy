package com.unitpricecalculator.util.sometimes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MutableSometimes<T> extends Sometimes<T> {

  void set(@Nullable T value);

  @NotNull
  static <T> MutableSometimes<T> create() {
    return new DirectReferenceSometimes<>();
  }
}
