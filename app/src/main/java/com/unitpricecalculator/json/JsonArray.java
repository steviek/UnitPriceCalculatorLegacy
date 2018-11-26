package com.unitpricecalculator.json;

import com.unitpricecalculator.json.JsonUtils.JsonThrowingSupplier;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Wrapper around {@link org.json.JSONArray} with some needed improvements.
 */
final class JsonArray {

  private final ObjectMapper objectMapper;
  private final JSONArray array;

  JsonArray(ObjectMapper objectMapper) {
    this(objectMapper, new JSONArray());
  }

  JsonArray(ObjectMapper objectMapper, JSONArray array) {
    this.objectMapper = objectMapper;
    this.array = array;
  }

  JsonArray add(JsonObject jsonObject) {
    jsonObject.addSelfTo(array);
    return this;
  }

  JsonObject getJsonObject(int index) {
    return wrappingJsonExceptions(() -> new JsonObject(objectMapper, array.getJSONObject(index)));
  }

  JsonArray getJsonArray(int index) {
    return wrappingJsonExceptions(() -> new JsonArray(objectMapper, array.getJSONArray(index)));
  }

  String getString(int index) {
    return wrappingJsonExceptions(() -> array.getString(index));
  }

  int length() {
    return array.length();
  }

  List<String> toStringList() {
    int length = array.length();
    List<String> list = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      final int index = i;
      list.add(wrappingJsonExceptions(() -> array.getString(index)));
    }
    return list;
  }

  <T> List<T> toList(Class<T> clazz) {
    if (clazz.equals(String.class)) {
      return (List<T>) toStringList();
    }
    int length = array.length();
    List<T> list = new ArrayList<>(length);
    for (int i = 0; i < length; i++) {
      final int index = i;
      list.add(wrappingJsonExceptions(
          () -> objectMapper
              .fromJsonObject(clazz, new JsonObject(objectMapper, array.getJSONObject(index)))));
    }
    return list;
  }

  void putSelfIn(JSONObject jsonObject, String key) {
    wrappingJsonExceptions(() -> jsonObject.put(key, array));
  }

  @Override
  public String toString() {
    return array.toString();
  }

  private <T> T wrappingJsonExceptions(JsonThrowingSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (JSONException e) {
      throw new RuntimeException("Error while parsing " + array.toString(), e);
    }
  }
}
