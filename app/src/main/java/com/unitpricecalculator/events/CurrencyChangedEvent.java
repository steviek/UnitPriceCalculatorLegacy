package com.unitpricecalculator.events;

import java.util.Currency;

public final class CurrencyChangedEvent {

  private final Currency currency;

  public CurrencyChangedEvent(Currency currency) {
    this.currency = currency;
  }

  public Currency getCurrency() {
    return currency;
  }
}
