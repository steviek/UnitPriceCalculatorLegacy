package com.unitpricecalculator.application;

import android.app.Application;
import android.content.Context;
import com.squareup.otto.Bus;
import com.unitpricecalculator.inject.ApplicationContext;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;
import javax.inject.Singleton;

@Module
public interface SingletonModule {

  @Singleton
  @Provides
  static Bus provideBus() {
    return new Bus();
  }

  @Binds
  @ApplicationContext
  Context bindContext(Application application);
}
