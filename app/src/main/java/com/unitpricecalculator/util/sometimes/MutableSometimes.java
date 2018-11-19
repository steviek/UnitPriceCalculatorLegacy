package com.unitpricecalculator.util.sometimes;

import javax.annotation.Nullable;

public interface MutableSometimes<T> extends Sometimes<T> {

  static <T> MutableSometimes<T> create() {
    return new DirectReferenceSometimes<>();
  }

  void set(@Nullable T reference);
}
