package com.unitpricecalculator.json;

interface JsonSerializer<T> {

  JsonObject toJson(ObjectMapper objectMapper, T instance);

  T fromJson(ObjectMapper objectMapper, JsonObject json);
}
