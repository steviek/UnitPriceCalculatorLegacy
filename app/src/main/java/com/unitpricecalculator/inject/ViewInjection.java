package com.unitpricecalculator.inject;

import android.view.View;

public final class ViewInjection {

  private ViewInjection() {}

  public static void inject(View view) {
    ((HasViewInjector) view.getContext()).viewInjector().inject(view);
  }

}
