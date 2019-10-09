package com.unitpricecalculator.unit;

import android.app.Application;
import androidx.test.core.app.ApplicationProvider;
import dagger.Module;
import dagger.Provides;

@Module
public abstract class TestApplicationModule {

  private TestApplicationModule() {}

  @Provides
  static Application provideApplication() {
    return ApplicationProvider.getApplicationContext();
  }

}
