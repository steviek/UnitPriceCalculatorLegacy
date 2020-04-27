package com.unitpricecalculator.util.sometimes;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface Sometimes<T> {
  boolean isPresent();

  @Nullable
  T orNull();

  void whenPresent(@NotNull Consumer<? super T> consumer);

  default Optional<T> toOptional() {
    return Optional.fromNullable(orNull());
  }

  default <R> Optional<R> map(@NotNull Function<? super T,  R> function) {
    return toOptional().transform(function);
  }

  @NotNull
  default T or(T fallback) {
    T value = orNull();
    return value == null ? fallback : value;
  }

  interface Consumer<T> {
    void consume(@NotNull T value);
  }
}