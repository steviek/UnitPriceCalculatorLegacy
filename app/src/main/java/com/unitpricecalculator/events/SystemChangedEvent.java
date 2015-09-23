package com.unitpricecalculator.events;

import com.unitpricecalculator.unit.System;

public final class SystemChangedEvent {

  private final System[] order;

  public SystemChangedEvent(System[] order) {
    this.order = order;
  }

  public System[] getOrder() {
    return order;
  }
}
