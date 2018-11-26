package com.unitpricecalculator.json;

import static com.google.common.base.Preconditions.checkArgument;

import android.support.annotation.Nullable;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import dagger.Reusable;
import java.util.Collection;
import java.util.Map;
import javax.inject.Inject;

@Reusable
public final class ObjectMapper {

  private final Map<Class<?>, JsonSerializer> serializers;
  private final Map<Class<?>, LegacyDeserializer> legacyDeserializers;

  @Inject
  ObjectMapper(Map<Class<?>, JsonSerializer> serializers,
      Map<Class<?>, LegacyDeserializer> legacyDeserializers) {
    this.serializers = serializers;
    this.legacyDeserializers = legacyDeserializers;
  }

  public <T> T fromJson(Class<T> clazz, String json) {
    checkArgument(!Strings.isNullOrEmpty(json));
    return fromJsonObject(clazz, new JsonObject(this, json));
  }

  <T> T fromJsonObject(Class<T> clazz, JsonObject jsonObject) {
    try {
      return (T) getSerializerOrThrow(clazz).fromJson(this, jsonObject);
    } catch (Exception e) {
      if (legacyDeserializers.containsKey(clazz)) {
        return (T) legacyDeserializers.get(clazz).fromJson(this, jsonObject);
      } else {
        throw e;
      }
    }
  }

  public <T> String toJson(T object) {
    return toJsonObject(object).toString();
  }

  <T> JsonObject toJsonObject(T object) {
    return ((JsonSerializer<T>) getSerializerOrThrow(object.getClass())).toJson(this, object);
  }

  <T> JsonArray toJsonArray(Collection<? extends T> objects) {
    JsonArray array = new JsonArray(this);
    for (T object : objects) {
      array.add(toJsonObject(object));
    }
    return array;
  }

  private JsonSerializer<?> getSerializerOrThrow(Class<?> clazz) {
    JsonSerializer<?> serializer = serializers.get(clazz);
    if (serializer == null) {
      throw new IllegalArgumentException("No serializer registered for " + clazz);
    }

    return serializer;
  }

  @Nullable
  public <T> T fromJsonNullable(Class<T> clazz, @Nullable String json) {
    if (Strings.isNullOrEmpty(json)) {
      return null;
    }
    return fromJson(clazz, json);
  }

  public <T> Optional<T> fromJsonOptional(Class<T> clazz, @Nullable String json) {
    return Optional.fromNullable(fromJsonNullable(clazz, json));
  }

}
