package com.unitpricecalculator.main;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.unitpricecalculator.BaseActivity;
import com.unitpricecalculator.application.MyApplication;
import com.unitpricecalculator.R;
import com.unitpricecalculator.comparisons.ComparisonFragment;
import com.unitpricecalculator.comparisons.SavedComparison;
import com.unitpricecalculator.saved.SavedFragment;

import java.io.IOException;

public final class MainActivity extends BaseActivity
        implements MenuFragment.Callback, SavedFragment.Callback {

    private ObjectMapper objectMapper;

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    private ComparisonFragment mComparisonFragment;
    private SettingsFragment mSettingsFragment;
    private SavedComparison mMainState;
    private SavedFragment mSavedFragment;

    private State mState;

    private enum State {
        MAIN, SETTINGS, SAVED
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        objectMapper = ((MyApplication) getApplication()).getObjectMapper();

        Preconditions.checkNotNull(getSupportActionBar());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout, R.string.app_name, R.string.app_name);

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mComparisonFragment = new ComparisonFragment();
        mSettingsFragment = new SettingsFragment();
        mSavedFragment = new SavedFragment();

        if (savedInstanceState != null) {
            mState = State.valueOf(savedInstanceState.getString("state"));
            try {
                mComparisonFragment.restoreState(
                        objectMapper.readValue(
                                savedInstanceState.getString("mainFragment"),
                                SavedComparison.class));
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        } else {
            mState = State.MAIN;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, getFragment(mState))
                .replace(R.id.menu_frame, new MenuFragment())
                .commit();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("state", mState.name());
        if (mComparisonFragment != null) {
            try {
                outState.putString(
                        "mainFragment",
                        objectMapper.writeValueAsString(mComparisonFragment.saveState()));
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
        switch (mState) {
            case MAIN:
                setTitle(R.string.app_name);
                getMenuInflater().inflate(R.menu.menu_main, menu);
                return true;
            case SETTINGS:
                setTitle(R.string.settings);
                return true;
            case SAVED:
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

    private void changeState(State state) {
        mDrawerLayout.closeDrawers();
        if (state == mState) {
            return;
        }

        if (mState == State.MAIN) {
            mMainState = mComparisonFragment.saveState();
        }

        switch (state) {
            case MAIN:
                mComparisonFragment.restoreState(mMainState);
                break;
            case SETTINGS:
                hideSoftKeyboard();
                break;
            case SAVED:
                hideSoftKeyboard();
                break;
        }
        mState = state;
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, getFragment(mState))
                .commit();
        invalidateOptionsMenu();
    }

    @Override
    public void onLoadSavedComparison(SavedComparison comparison) {
        mMainState = comparison;
        changeState(State.MAIN);
    }
}
