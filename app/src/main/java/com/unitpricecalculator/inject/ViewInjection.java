package com.unitpricecalculator.inject;

import android.view.View;
import dagger.android.HasAndroidInjector;

public final class ViewInjection {

  private ViewInjection() {}

  public static void inject(View view) {
    ((HasAndroidInjector) view.getContext()).androidInjector().inject(view);
  }

}
