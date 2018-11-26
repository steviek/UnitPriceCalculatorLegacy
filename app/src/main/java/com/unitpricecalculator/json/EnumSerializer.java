package com.unitpricecalculator.json;

import com.google.common.base.Function;

final class EnumSerializer<T extends Enum<T>> implements JsonSerializer<T> {

  private static final String NAME = "name";

  private final Function<String, T> valueOfFunction;

  EnumSerializer(Function<String, T> valueOfFunction) {
    this.valueOfFunction = valueOfFunction;
  }

  @Override
  public JsonObject toJson(ObjectMapper objectMapper, T instance) {
    return new JsonObject(objectMapper).put(NAME, instance.name());
  }

  @Override
  public T fromJson(ObjectMapper objectMapper, JsonObject json) {
   return valueOfFunction.apply(json.getStringOrThrow(NAME));
  }
}
