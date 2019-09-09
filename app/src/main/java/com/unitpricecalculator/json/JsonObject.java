package com.unitpricecalculator.json;

import static com.google.common.base.Preconditions.checkNotNull;

import androidx.annotation.Nullable;
import com.unitpricecalculator.json.JsonUtils.JsonThrowingSupplier;
import javax.annotation.Nonnull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/** Wrapper around {@link org.json.JSONObject} with some needed improvements. */
final class JsonObject {

  private final ObjectMapper objectMapper;
  private final JSONObject object;

  JsonObject(ObjectMapper objectMapper) {
    this(objectMapper, new JSONObject());
  }

  JsonObject(ObjectMapper objectMapper, String json) {
    this(objectMapper, JsonUtils.wrappingJsonExceptions(json, () -> new JSONObject(json)));
  }

  JsonObject(ObjectMapper objectMapper, JSONObject object) {
    this.objectMapper = objectMapper;
    this.object = object;
  }

  JsonObject put(String key, @Nonnull String value) {
    wrappingJsonExceptions(() -> object.put(key, value));
    return this;
  }

  JsonObject putNullable(String key, @Nullable String value) {
    if (value == null) {
      return this;
    }
    return put(key, value);
  }

  JsonObject put(String key, @Nonnull Object value) {
    checkNotNull(value);
    if (value instanceof JsonObject) {
      throw new IllegalArgumentException("Do not pass JsonObject directly, use class for " + value);
    }
    wrappingJsonExceptions(() -> putJsonObject(key, objectMapper.toJsonObject(value)));
    return this;
  }

  JsonObject putNullable(String key, @Nullable Object value) {
    if (value == null) {
      return this;
    }
    return put(key, value);
  }

  private JsonObject putJsonObject(String key, JsonObject jsonObject) {
    wrappingJsonExceptions(() -> object.put(key, jsonObject.object));
    return this;
  }

  JsonObject put(String key, JsonArray jsonArray) {
    jsonArray.putSelfIn(object, key);
    return this;
  }

  String getStringOrThrow(String key) {
    return wrappingJsonExceptions(() -> object.getString(key));
  }

  @Nullable
  String getStringOrNull(String key) {
    if (object.has(key)) {
      return getStringOrThrow(key);
    } else {
      return null;
    }
  }

  JsonArray getJsonArrayOrThrow(String key) {
    return wrappingJsonExceptions(() -> new JsonArray(objectMapper, object.getJSONArray(key)));
  }

  <T> T getOrThrow(Class<T> clazz, String key) {
    return wrappingJsonExceptions(() -> objectMapper.fromJsonObject(clazz, getJsonObject(key)));
  }

  @Nullable
  <T> T getOrNull(Class<T> clazz, String key) {
    if (object.has(key)) {
      return getOrThrow(clazz, key);
    } else {
      return null;
    }
  }

  private JsonObject getJsonObject(String key) {
    return wrappingJsonExceptions(() -> new JsonObject(objectMapper, object.getJSONObject(key)));
  }

  void addSelfTo(JSONArray array) {
    array.put(object);
  }

  @Override
  public String toString() {
    return object.toString();
  }

  private <T> T wrappingJsonExceptions(JsonThrowingSupplier<T> supplier) {
    try {
      return supplier.get();
    } catch (JSONException e) {
      throw new RuntimeException("Error while parsing " + object.toString(), e);
    }
  }
}
