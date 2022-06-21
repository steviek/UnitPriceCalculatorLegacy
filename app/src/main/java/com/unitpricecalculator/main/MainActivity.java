package com.unitpricecalculator.main;

import static com.google.common.base.Preconditions.checkNotNull;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.play.core.review.ReviewInfo;
import com.google.android.play.core.review.ReviewManager;
import com.google.android.play.core.review.ReviewManagerFactory;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;
import com.unitpricecalculator.BaseActivity;
import com.unitpricecalculator.R;
import com.unitpricecalculator.comparisons.ComparisonFragment;
import com.unitpricecalculator.comparisons.ComparisonFragmentState;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.currency.Currencies;
import com.unitpricecalculator.databinding.MainActivityBinding;
import com.unitpricecalculator.events.AppLocaleChangedEvent;
import com.unitpricecalculator.events.SavedComparisonDeletedEvent;
import com.unitpricecalculator.export.ExportManager;
import com.unitpricecalculator.export.ImportManager;
import com.unitpricecalculator.initialscreen.InitialScreen;
import com.unitpricecalculator.initialscreen.InitialScreenManager;
import com.unitpricecalculator.json.ObjectMapper;
import com.unitpricecalculator.locale.AppLocaleManager;
import com.unitpricecalculator.saved.SavedComparisonManager;
import com.unitpricecalculator.saved.SavedFragment;
import com.unitpricecalculator.settings.SettingsFragment;
import com.unitpricecalculator.unit.Units;
import com.unitpricecalculator.util.RequestCodes;

import java.util.Currency;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public final class MainActivity extends BaseActivity
    implements MenuFragment.Callback, SavedFragment.Callback {

  @Inject
  Bus bus;

  @Inject
  Units units;

  @Inject
  ObjectMapper objectMapper;

  @Inject
  InitialScreenManager initialScreenManager;

  @Inject
  SavedComparisonManager savedComparisonManager;

  @Inject
  ExportManager exportManager;

  @Inject
  ImportManager importManager;

  @Inject
  AppLocaleManager localeManager;

  private MainActivityBinding viewBinding;
  // private ActionBarDrawerToggle mDrawerToggle;
  //private DrawerLayout mDrawerLayout;

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
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    viewBinding = MainActivityBinding.inflate(getLayoutInflater());
    setContentView(viewBinding.getRoot());

    mComparisonFragment = new ComparisonFragment();
    mSettingsFragment = new SettingsFragment();
    mSavedFragment = new SavedFragment();

    if (savedInstanceState == null && getIntent().getStringExtra("state") == null) {
      if (initialScreenManager.getInitialScreen() == InitialScreen.SAVED_COMPARISONS &&
          !savedComparisonManager.getSavedComparisons().isEmpty()) {
        currentState = State.SAVED;
        viewBinding.bottomNavigation.setSelectedItemId(R.id.saved);
      } else {
        currentState = State.MAIN;
      }
    } else if (savedInstanceState == null) {
      currentState = State.valueOf(getIntent().getStringExtra("state"));
    } else {
      currentState = State.valueOf(savedInstanceState.getString("state"));
      mComparisonFragment.restoreState(
          objectMapper.fromJson(ComparisonFragmentState.class,
              savedInstanceState.getString("mainFragment")));
    }

    getSupportFragmentManager()
        .beginTransaction()
        .replace(viewBinding.contentFrame.getId(), getFragment(currentState))
        .commit();

    viewBinding.bottomNavigation.setOnItemSelectedListener(item -> {
      if (item.getItemId() == R.id.current) {
        changeState(State.MAIN);
      } else if (item.getItemId() == R.id.saved) {
        changeState(State.SAVED);
      } else if (item.getItemId() == R.id.settings) {
        changeState(State.SETTINGS);
      }
      return true;
    });
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
      outState.putString(
          "mainFragment",
          objectMapper.toJson(mComparisonFragment.saveState(this)));

    }
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    // Sync the toggle state after onRestoreInstanceState has occurred.
    //mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    // mDrawerToggle.onConfigurationChanged(newConfig);
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
        return false;
    }
    return super.onPrepareOptionsMenu(menu);
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
        ReviewManager manager = ReviewManagerFactory.create(this);
        Task<ReviewInfo> request = manager.requestReviewFlow();
        request.addOnCompleteListener(task -> {
          if (task.isSuccessful()) {
            ReviewInfo reviewInfo = task.getResult();
            manager.launchReviewFlow(this, reviewInfo);
          } else {
            // Failed, just launch play store
            launchPlayStore();
          }
        });

        break;
      case SETTINGS:
        changeState(State.SETTINGS);
        break;
      case SAVED:
        changeState(State.SAVED);
        break;
      case SHARE:
        break;
      case BUY_COFFEE:
        Intent coffeeIntent = new Intent(Intent.ACTION_VIEW)
            .setData(Uri.parse("https://www.buymeacoffee.com/kideckel"));
        startActivity(coffeeIntent);
        break;
    }
  }

  @Override
  public void onBackPressed() {
    InitialScreen initialScreen = initialScreenManager.getInitialScreen();
    // If the user presses back on a different screen, navigate them back to their initial one.
    if (currentState != State.MAIN && initialScreen == InitialScreen.NEW_COMPARISON) {
      changeState(State.MAIN);
      return;
    } else if (currentState != State.SAVED && initialScreen == InitialScreen.SAVED_COMPARISONS) {
      // If the user presses back on a different screen, navigate them back to the main one.
      changeState(State.SAVED);
      return;
    }
    super.onBackPressed();
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

  @Subscribe
  public void onAppLocaleChanged(AppLocaleChangedEvent event) {
    startActivity(new Intent(this, MainActivity.class).putExtra("state", currentState.name()));
    finish();
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
    if (newState == currentState) {
      return;
    }

    if (currentState == State.MAIN) {
      comparisonFragmentState = mComparisonFragment.saveState(this);
    }

    int selectedItemId = R.id.current;
    switch (newState) {
      case MAIN:
        if (comparisonFragmentState != null) {
          mComparisonFragment.restoreState(comparisonFragmentState);
        } else {
          mComparisonFragment.clear();
        }
        selectedItemId = R.id.current;
        break;
      case SETTINGS:
        hideSoftKeyboard();
        selectedItemId = R.id.settings;
        break;
      case SAVED:
        hideSoftKeyboard();
        selectedItemId = R.id.saved;
        break;
    }
    currentState = newState;
    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.content_frame, getFragment(currentState))
        .commit();
    invalidateOptionsMenu();

    if (viewBinding.bottomNavigation.getSelectedItemId() != selectedItemId) {
      viewBinding.bottomNavigation.setSelectedItemId(selectedItemId);
    }
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

  @Override
  protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    if (resultCode != Activity.RESULT_OK) {
      return;
    }

    switch (requestCode) {
      case RequestCodes.RC_CREATE_FILE:
        exportManager.handleActivityResult(data);
      case RequestCodes.RC_OPEN_FILE:
        importManager.handleActivityResult(data);
        break;
    }
  }

  private Context getDisplayContext() {
    return localeManager.getCurrent().apply(this);
  }

  private void launchPlayStore() {
    if (isDestroyed()) {
      return;
    }
    Intent i = new Intent(Intent.ACTION_VIEW,
        Uri.parse(
            "https://play.google.com/store/apps/details?id=" +
                "com.unitpricecalculator"));
    startActivity(i);
  }
}
