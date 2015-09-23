package com.unitpricecalculator.main;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;

import com.unitpricecalculator.BaseActivity;
import com.unitpricecalculator.R;

public final class MainActivity extends BaseActivity {

  private ActionBarDrawerToggle mDrawerToggle;

  private DrawerLayout mDrawerLayout;

  private MainFragment mFragment;

  private MenuFragment mMenu;

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name,
        R.string.app_name) {

    };

    mDrawerToggle.setDrawerIndicatorEnabled(true);
    mDrawerLayout.setDrawerListener(mDrawerToggle);

    mFragment = new MainFragment();
    mMenu = new MenuFragment();

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, mFragment)
        .replace(R.id.menu_frame, mMenu)
        .commit();

    if (savedInstanceState != null) {
      mFragment.onRestoreState(savedInstanceState.getBundle("mainFragment"));
    }
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mFragment != null) {
      Bundle b = new Bundle();
      mFragment.onSaveState(b);
      outState.putBundle("mainFragment", b);
    }
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Pass the event to ActionBarDrawerToggle, if it returns
    // true, then it has handled the app icon touch event
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    // Handle your other action bar items...

    return super.onOptionsItemSelected(item);
  }
}
