package com.unitpricecalculator.main;

import com.google.common.base.Preconditions;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.Menu;
import android.view.MenuItem;

import com.unitpricecalculator.BaseActivity;
import com.unitpricecalculator.R;
import com.unitpricecalculator.util.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public final class MainActivity extends BaseActivity implements MenuFragment.Callback {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;

    private MainFragment mMainFragment;
    private SettingsFragment mSettingsFragment;
    private JSONObject mMainState;

    private State mState;

    private enum State {
        MAIN, SETTINGS
    }

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Preconditions.checkNotNull(getSupportActionBar());
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.app_name, R.string.app_name);

        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mMainFragment = new MainFragment();
        mSettingsFragment = new SettingsFragment();

        if (savedInstanceState != null) {
            mState = State.valueOf(savedInstanceState.getString("state"));
            try {
                mMainFragment.restoreState(new JSONObject(savedInstanceState.getString("mainFragment")));
            } catch (JSONException e) {
                Logger.e(e);
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
        if (mMainFragment != null) {
            try {
                outState.putString("mainFragment", mMainFragment.saveState().toString());
            } catch (JSONException e) {
                Logger.e(e);
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
        switch (mState) {
            case SETTINGS:
                menu.clear();
                return true;
            case MAIN:
                menu.clear();
                getMenuInflater().inflate(R.menu.menu_main, menu);
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
                mMainFragment.save();
                break;
            case R.id.action_clear:
                mMainFragment.clear();
                break;

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onMenuEvent(MenuFragment.MenuEvent event) {
        switch (event) {
            case FEEDBACK:
                break;
            case NEW:
                changeState(State.MAIN);
                break;
            case RATE:
                Intent i = new Intent(Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=com.unitpricecalculator"));
                startActivity(i);
                break;
            case SETTINGS:
                changeState(State.SETTINGS);
                break;
            case SAVED:
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
                return mMainFragment;
        }
        throw new IllegalArgumentException("Unexpected state: " + state);
    }

    private void changeState(State state) {
        mDrawerLayout.closeDrawers();
        if (state == mState) {
            return;
        }

        try {
            switch (state) {
                case MAIN:
                    mMainFragment.restoreState(mMainState);
                    break;
                case SETTINGS:
                    mMainState = mMainFragment.saveState();
                    hideSoftKeyboard();
                    break;
            }
        } catch (JSONException e) {
            Logger.e(e);
        }
        mState = state;
        getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, getFragment(mState)).commit();
        invalidateOptionsMenu();
    }
}
