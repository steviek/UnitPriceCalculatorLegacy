package com.unitpricecalculator.main;

import android.os.Bundle;

import com.unitpricecalculator.BaseActivity;
import com.unitpricecalculator.R;

public final class MainActivity extends BaseActivity {

  private MainFragment mFragment;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    mFragment = new MainFragment();
    getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, mFragment).commit();
  }
}
