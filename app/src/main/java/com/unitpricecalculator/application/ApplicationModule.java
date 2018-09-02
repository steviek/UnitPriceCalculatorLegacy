package com.unitpricecalculator.application;

import android.app.Application;

import dagger.Module;
import dagger.Provides;

@Module
public final class ApplicationModule {

    private final MyApplication application;

    public ApplicationModule(MyApplication application) {
        this.application = application;
    }

    @Provides
    MyApplication provideMyApplication() {
        return application;
    }

    @Provides
    Application provideApplication() {
        return application;
    }
}
