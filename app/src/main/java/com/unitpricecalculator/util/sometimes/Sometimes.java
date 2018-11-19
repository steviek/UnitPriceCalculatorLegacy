package com.unitpricecalculator.util.sometimes;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.unitpricecalculator.util.Consumer;

public interface Sometimes<T> {

  void whenPresent(Consumer<T> consumer);

  Optional<T> toOptional();

  boolean isPresent();

  default void whenPresent(Runnable runnable) {
    whenPresent(ignored -> runnable.run());
  }

  default <U> Optional<U> map(Function<T,U> function) {
    return toOptional().transform(function);
  }

  default T or(T fallback) {
    return toOptional().or(fallback);
  }

  default CompositeSometimes<T> or(Sometimes<T> other) {
    return new CompositeSometimes<>(this, other);
  }
}
