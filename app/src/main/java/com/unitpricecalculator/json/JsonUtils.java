package com.unitpricecalculator.json;

import org.json.JSONException;

final class JsonUtils {

  private JsonUtils() {}

  interface JsonThrowingRunnable {
    void run() throws JSONException;
  }

  interface JsonThrowingSupplier<T> {
    T get() throws JSONException;
  }

  static <T> T wrappingJsonExceptions(String string, JsonThrowingSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (JSONException e) {
      throw new RuntimeException("Error while parsing " + string, e);
    }
  }
}
