package com.unitpricecalculator.json;

import dagger.Module;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;
import dagger.multibindings.Multibinds;
import java.util.Map;

@InstallIn(SingletonComponent.class)
@Module
public interface JsonMapperModule {

  @Multibinds
  Map<Class<?>, JsonSerializer> multibindJsonSerializerMap();
}
