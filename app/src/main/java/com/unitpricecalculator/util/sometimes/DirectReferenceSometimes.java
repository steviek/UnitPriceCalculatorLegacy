package com.unitpricecalculator.util.sometimes;

import com.google.common.base.Optional;
import com.unitpricecalculator.util.Consumer;
import java.util.ArrayDeque;
import javax.annotation.Nullable;

final class DirectReferenceSometimes<T> implements MutableSometimes<T> {

  @Nullable
  private T reference;

  private ArrayDeque<Consumer<T>> pendingConsumers = new ArrayDeque<>();

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
  public void whenPresent(Consumer<T> consumer) {
    if (reference != null) {
      consumer.consume(reference);
    } else {
      pendingConsumers.push(consumer);
    }
  }

  @Override
  public Optional<T> toOptional() {
    return Optional.fromNullable(reference);
  }
}
