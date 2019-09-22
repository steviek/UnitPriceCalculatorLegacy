package com.unitpricecalculator.events;

import com.unitpricecalculator.unit.System;
import java.util.Set;

public final class SystemChangedEvent {

  private final System[] order;
  private final Set<System> includedSystems;

  public SystemChangedEvent(System[] order, Set<System> includedSystems) {
    this.order = order;
    this.includedSystems = includedSystems;
  }

  public System[] getOrder() {
    return order;
  }

  public Set<System> getIncludedSystems() {
    return includedSystems;
  }
}
