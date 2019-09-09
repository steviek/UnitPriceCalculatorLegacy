package com.unitpricecalculator.comparisons;

import androidx.annotation.Nullable;
import java.util.Objects;

public final class ComparisonFragmentState {

  private final SavedComparison currentComparison;
  @Nullable
  private final SavedComparison lastKnownSavedComparison;

  public ComparisonFragmentState(
      SavedComparison currentComparison,
      @Nullable SavedComparison lastKnownSavedComparison) {
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ComparisonFragmentState that = (ComparisonFragmentState) o;
    return Objects.equals(currentComparison, that.currentComparison) &&
        Objects.equals(lastKnownSavedComparison, that.lastKnownSavedComparison);
  }

  @Override
  public int hashCode() {
    int result = currentComparison.hashCode();
    result =
        31 * result + (lastKnownSavedComparison != null ? lastKnownSavedComparison.hashCode() : 0);
    return result;
  }
}
