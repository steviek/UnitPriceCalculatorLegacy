package com.unitpricecalculator.view;

import com.google.common.base.Preconditions;

public final class Fragments {

  private Fragments() {}

  public static <T> T castCallback(Object object, Class<T> callbackClass) {
    Preconditions.checkNotNull(object);
    Preconditions.checkNotNull(callbackClass);

    try {
      return callbackClass.cast(object);
    } catch (ClassCastException e) {
      throw new RuntimeException(
          object.getClass() + " must implement " + callbackClass.getSimpleName() + " interface", e);
    }
  }
}
