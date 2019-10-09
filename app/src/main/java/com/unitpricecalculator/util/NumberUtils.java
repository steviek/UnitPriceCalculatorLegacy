package com.unitpricecalculator.util;


public final class NumberUtils {

  private NumberUtils() {
  }

  /**
   * Returns the first provided String that can be parsed as a double.
   *
   * @throws IllegalArgumentException if neither String can be parsed as a double
   */
  public static String firstParsableDouble(String s1, String s2) {
    if (parsesNicelyDouble(s1)) {
      return s1;
    } else if (parsesNicelyDouble(s2)) {
      return s2;
    } else {
      throw new IllegalStateException(
          "Expected one of " + s1 + ", " + s2 + " to be parsable, but neither were");
    }
  }

  public static boolean parsesNicelyDouble(String s) {
    return Localization.parseDoubleSafely(s).isPresent();
  }

}
