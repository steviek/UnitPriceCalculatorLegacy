package com.unitpricecalculator.json;

import dagger.Module;
import dagger.multibindings.Multibinds;
import java.util.Map;

@Module(includes = SerializersModule.class)
public interface JsonMapperModule {

  @Multibinds
  Map<Class<?>, JsonSerializer> multibindJsonSerializerMap();
}
