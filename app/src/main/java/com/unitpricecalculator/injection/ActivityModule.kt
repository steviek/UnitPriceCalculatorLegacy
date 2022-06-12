package com.unitpricecalculator.injection

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@InstallIn(ActivityComponent::class)
@Module
object ActivityModule {
    @Provides
    fun provideAppCompatActivity(activity: Activity): AppCompatActivity {
        return activity as AppCompatActivity
    }
}