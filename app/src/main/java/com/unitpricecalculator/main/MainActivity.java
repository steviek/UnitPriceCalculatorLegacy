package com.unitpricecalculator.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BaseActivity;
import com.unitpricecalculator.R;
import com.unitpricecalculator.comparisons.ComparisonFragment;
import com.unitpricecalculator.comparisons.ComparisonFragmentState;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.currency.Currencies;
import com.unitpricecalculator.events.SavedComparisonDeletedEvent;
import com.unitpricecalculator.saved.SavedFragment;
import com.unitpricecalculator.unit.Units;
import java.io.IOException;
import java.util.Currency;
import javax.inject.Inject;

public final class MainActivity extends BaseActivity
    implements MenuFragment.Callback, SavedFragment.Callback {

  @Inject
  ObjectMapper objectMapper;

  @Inject
  Bus bus;

  @Inject
  Units units;

  private ActionBarDrawerToggle mDrawerToggle;
  private DrawerLayout mDrawerLayout;

  private ComparisonFragment mComparisonFragment;
  private SettingsFragment mSettingsFragment;
  private SavedFragment mSavedFragment;

  @Nullable
  private ComparisonFragmentState comparisonFragmentState;

  private State currentState;

  private enum State {
    MAIN, SETTINGS, SAVED
  }

  @Override
  protected final void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Preconditions.checkNotNull(getSupportActionBar());
    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    getSupportActionBar().setHomeButtonEnabled(true);

    mDrawerLayout = findViewById(R.id.drawer_layout);
    mDrawerToggle = new ActionBarDrawerToggle(
        this, mDrawerLayout, R.string.app_name, R.string.app_name);

    mDrawerToggle.setDrawerIndicatorEnabled(true);
    mDrawerLayout.setDrawerListener(mDrawerToggle);

    mComparisonFragment = new ComparisonFragment();
    mSettingsFragment = new SettingsFragment();
    mSavedFragment = new SavedFragment();

    if (savedInstanceState != null) {
      currentState = State.valueOf(savedInstanceState.getString("state"));
      try {
        mComparisonFragment.restoreState(
            objectMapper.readValue(
                savedInstanceState.getString("mainFragment"),
                ComparisonFragmentState.class));
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    } else {
      currentState = State.MAIN;
    }

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, getFragment(currentState))
        .replace(R.id.menu_frame, new MenuFragment())
        .commit();
  }

  @Override
  protected void onStart() {
    super.onStart();
    bus.register(this);
  }

  @Override
  protected void onStop() {
    super.onStop();
    bus.unregister(this);
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString("state", currentState.name());
    if (mComparisonFragment != null) {
      try {
        outState.putString(
            "mainFragment",
            objectMapper.writeValueAsString(mComparisonFragment.saveState(this)));
      } catch (JsonProcessingException e) {
        throw Throwables.propagate(e);
      }
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
  public boolean onPrepareOptionsMenu(Menu menu) {
    menu.clear();
    switch (currentState) {
      case MAIN:
        return false;
      case SETTINGS:
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        setTitle(R.string.settings);
        return true;
      case SAVED:
        getSupportActionBar().setCustomView(null);
        getSupportActionBar().setDisplayShowCustomEnabled(false);
        setTitle(R.string.saved_comparisons);
        return true;
    }
    return super.onPrepareOptionsMenu(menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Pass the event to ActionBarDrawerToggle, if it returns
    // true, then it has handled the app icon touch event
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    // Handle your other action bar items...
    switch (item.getItemId()) {
      case R.id.action_save:
        mComparisonFragment.save();
        break;
      case R.id.action_clear:
        mComparisonFragment.clear();
        break;

    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onMenuEvent(MenuFragment.MenuEvent event) {
    switch (event) {
      case FEEDBACK:
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("message/rfc822");
        intent.putExtra(Intent.EXTRA_EMAIL, "sixbynineapps@gmail.com");
        intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.feedback_subject));
        try {
          startActivity(Intent.createChooser(intent, getString(R.string.send_email)));
        } catch (ActivityNotFoundException e) {
          Toast.makeText(this, R.string.no_email_client, Toast.LENGTH_SHORT).show();
        }
        break;
      case NEW:
        changeState(State.MAIN);
        break;
      case RATE:
        Intent i = new Intent(Intent.ACTION_VIEW,
            Uri.parse(
                "https://play.google.com/store/apps/details?id=" +
                    "com.unitpricecalculator"));
        startActivity(i);
        break;
      case SETTINGS:
        changeState(State.SETTINGS);
        break;
      case SAVED:
        changeState(State.SAVED);
        break;
      case SHARE:
        break;
    }
  }

  @Subscribe
  public void onSavedComparisonDeleted(SavedComparisonDeletedEvent event) {
    if (comparisonFragmentState == null) {
      return;
    }

    SavedComparison draftComparison = comparisonFragmentState.getCurrentComparison();
    if (draftComparison != null && draftComparison.getKey().equals(event.getKey())) {
      comparisonFragmentState = null;
    }
  }

  private Fragment getFragment(State state) {
    switch (state) {
      case SETTINGS:
        return mSettingsFragment;
      case MAIN:
        return mComparisonFragment;
      case SAVED:
        return mSavedFragment;
    }
    throw new IllegalArgumentException("Unexpected state: " + state);
  }

  private void changeState(State newState) {
    mDrawerLayout.closeDrawers();
    if (newState == currentState) {
      return;
    }

    if (currentState == State.MAIN) {
      comparisonFragmentState = mComparisonFragment.saveState(this);
    }

    switch (newState) {
      case MAIN:
        if (comparisonFragmentState != null) {
          mComparisonFragment.restoreState(comparisonFragmentState);
        } else {
          mComparisonFragment.clear();
        }
        break;
      case SETTINGS:
        hideSoftKeyboard();
        break;
      case SAVED:
        hideSoftKeyboard();
        break;
    }
    currentState = newState;
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, getFragment(currentState))
        .commit();
    invalidateOptionsMenu();
  }

  @Override
  public void onLoadSavedComparison(SavedComparison comparison) {
    if (Strings.isNullOrEmpty(comparison.getCurrencyCode())) {
      comparison = comparison.addCurrency(units.getCurrency().getCurrencyCode());
    } else {
      Optional<Currency> currency = Currencies.parseCurrencySafely(comparison.getCurrencyCode());
      if (currency.isPresent()) {
        units.setCurrency(currency.get());
      }
    }
    comparisonFragmentState = new ComparisonFragmentState(comparison, comparison);
    changeState(State.MAIN);
  }
}
