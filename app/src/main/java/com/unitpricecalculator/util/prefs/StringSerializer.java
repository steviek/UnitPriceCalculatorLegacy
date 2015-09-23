package com.unitpricecalculator.util.prefs;

public interface StringSerializer<T> {

  String serialize(T object);
}
