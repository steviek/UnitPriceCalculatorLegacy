package com.unitpricecalculator.util.sometimes;

import static com.google.common.base.Preconditions.checkNotNull;

import com.google.common.collect.ImmutableList;

public final class CompositeSometimes<T> {

  private final ImmutableList<Sometimes<T>> sometimes;

  private CompositeSometimes(ImmutableList<Sometimes<T>> sometimes) {
    this.sometimes = checkNotNull(sometimes);
  }

  CompositeSometimes(Sometimes<T> first, Sometimes<T> second) {
    ImmutableList.Builder<Sometimes<T>> builder = ImmutableList.builder();
    builder.add(first);
    builder.add(second);
    this.sometimes = builder.build();
  }

  public void whenAllPresent(Runnable runnable) {
    for (Sometimes<T> s : sometimes) {
      if (!s.isPresent()) {
        s.whenPresent(ignored -> whenAllPresent(runnable));
        return;
      }
    }
    runnable.run();
  }

  public boolean areAllPresent() {
    for (Sometimes<T> s : sometimes) {
      if (!s.isPresent()) {
        return false;
      }
    }
    return true;
  }
}
