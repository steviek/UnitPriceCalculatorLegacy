package com.unitpricecalculator.comparisons

enum class Order(val number: Int) {
  NONE(0),
  PRICE_ASCENDING(1),
  PRICE_DESCENDING(2),
  NUMBER_ASCENDING(3),
  NUMBER_DESCENDING(4),
  SIZE_ASCENDING(5),
  SIZE_DESCENDING(6);

  companion object {
    @JvmStatic
    fun forNumber(number: Int?): Order = values().firstOrNull { it.number == number } ?: NONE
  }
}