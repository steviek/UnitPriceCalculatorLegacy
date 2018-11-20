package com.unitpricecalculator.events;

public final class SavedComparisonDeletedEvent {

  private final String key;

  public SavedComparisonDeletedEvent(String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }
}
