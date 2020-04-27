package com.unitpricecalculator.util.sometimes;

import java.util.ArrayDeque;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DirectReferenceSometimes<T> implements MutableSometimes<T> {

  @Nullable
  private T reference;

  private ArrayDeque<Consumer<? super T>> pendingConsumers = new ArrayDeque<>();

  DirectReferenceSometimes() {}

  @Override
  public boolean isPresent() {
    return reference != null;
  }

  @Override
  public void set(@Nullable T reference) {
    this.reference = reference;

    if (reference != null) {
      while (!pendingConsumers.isEmpty()) {
        pendingConsumers.pop().consume(reference);
      }
    }
  }

  @Override
  public void whenPresent(@NotNull Consumer<? super T> consumer) {
    T reference = this.reference;
    if (reference == null) {
      pendingConsumers.push(consumer);
    } else {
      consumer.consume(reference);
    }
  }

  @Nullable
  @Override
  public T orNull() {
    return reference;
  }
}
