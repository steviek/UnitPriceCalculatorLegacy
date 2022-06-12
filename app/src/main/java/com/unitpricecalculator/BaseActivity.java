package com.unitpricecalculator;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.unitpricecalculator.locale.AppLocale;
import com.unitpricecalculator.locale.AppLocaleManager;

public abstract class BaseActivity extends AppCompatActivity {

  protected final void hideSoftKeyboard() {
    View view = this.getCurrentFocus();
    if (view != null) {
      InputMethodManager imm =
          (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
  }

  @Override
  protected void attachBaseContext(Context newBase) {
    AppLocaleManager manager = AppLocaleManager.getInstance();
    AppLocale locale = manager.getCurrent();
    if (locale == AppLocale.MATCH_DEVICE) {
      super.attachBaseContext(newBase);
    } else {
      Configuration configuration = new Configuration();
      configuration.setLocale(locale.toLocale());
      super.attachBaseContext(newBase.createConfigurationContext(configuration));
    }
  }
}
