package com.unitpricecalculator.inject;

import android.view.View;
import dagger.MapKey;

@MapKey
public @interface ViewKey {
  Class<? extends View> value();
}
