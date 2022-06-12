package com.unitpricecalculator.application;

import com.squareup.otto.Bus;
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

import javax.inject.Singleton;

@InstallIn(SingletonComponent.class)
@Module
public interface SingletonModule {

  @Singleton
  @Provides
  static Bus provideBus() {
    return new Bus();
  }
}
