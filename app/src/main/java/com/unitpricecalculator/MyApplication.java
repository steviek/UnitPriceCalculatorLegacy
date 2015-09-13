package com.unitpricecalculator;

import android.app.Application;

public final class MyApplication extends Application {

  private static MyApplication instance;

  @Override
  public void onCreate() {
    super.onCreate();
    instance = this;
  }

  public static MyApplication getInstance() {
    return instance;
  }
}
