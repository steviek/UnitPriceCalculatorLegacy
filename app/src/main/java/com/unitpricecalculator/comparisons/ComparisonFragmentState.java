package com.unitpricecalculator.comparisons;

import android.support.annotation.Nullable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public final class ComparisonFragmentState {

  private final SavedComparison currentComparison;
  @Nullable
  private final SavedComparison lastKnownSavedComparison;

  @JsonCreator
  public ComparisonFragmentState(
      @JsonProperty("currentComparison") SavedComparison currentComparison,
      @JsonProperty("lastKnownSavedComparison") @Nullable SavedComparison lastKnownSavedComparison) {
    this.currentComparison = currentComparison;
    this.lastKnownSavedComparison = lastKnownSavedComparison;
  }

  public SavedComparison getCurrentComparison() {
    return currentComparison;
  }

  @Nullable
  public SavedComparison getLastKnownSavedComparison() {
    return lastKnownSavedComparison;
  }
}
