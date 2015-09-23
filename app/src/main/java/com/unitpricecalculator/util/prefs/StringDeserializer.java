package com.unitpricecalculator.util.prefs;

public interface StringDeserializer<T> {

  T deserialize(String s);
}
