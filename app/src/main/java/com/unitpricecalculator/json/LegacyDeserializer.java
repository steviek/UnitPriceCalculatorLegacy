package com.unitpricecalculator.json;

interface LegacyDeserializer<T> {
  T fromJson(ObjectMapper objectMapper, JsonObject json);
}
