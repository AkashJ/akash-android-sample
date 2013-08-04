package com.akash.android.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import com.akash.android.sample.base.BaseActivity;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

public class HomeActivity extends BaseActivity {

    private static final int LOGGED_OUT = 0;
    private static final int LOGGED_IN = 1;
    private static final int SETTINGS = 2;
    private static final int FRAGMENT_COUNT = SETTINGS+1;
    private boolean isResumed = false;
    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private UiLifecycleHelper uiHelper;
    private MenuItem settings;
    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);

        setContentView(R.layout.home);
        FragmentManager fm = getSupportFragmentManager();
        fragments[LOGGED_OUT] = fm.findFragmentById(R.id.loggedOutFrag);
        fragments[LOGGED_IN] = fm.findFragmentById(R.id.loggedInFrag);
        fragments[SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);
        FragmentTransaction transaction = fm.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.hide(fragment);
        }
        transaction.commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // only add the menu when the selection fragment is showing
        if (fragments[LOGGED_IN].isVisible()) {
            if (menu.size() == 0) {
                settings = menu.add(R.string.settings);
            }
            return true;
        } else {
            menu.clear();
            settings = null;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.equals(settings)) {
            showFragment(SETTINGS, true);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        isResumed = true;
        uiHelper.onResume();
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            showFragment(LOGGED_IN, false);
        } else {
            showFragment(LOGGED_OUT, false);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        uiHelper.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
        isResumed = false;
        uiHelper.onPause();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        uiHelper.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    private void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
            FragmentManager manager = getSupportFragmentManager();
            int backStackSize = manager.getBackStackEntryCount();
            for (int i = 0; i < backStackSize; i++) {
                manager.popBackStack();
            }
            if (state.isOpened()) {
                showFragment(LOGGED_IN, false);
            } else if (state.isClosed()) {
                showFragment(LOGGED_OUT, false);
            }
        }
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction transaction = fm.beginTransaction();
        for (int i = 0; i < fragments.length; i++) {
            if (i == fragmentIndex) {
                transaction.show(fragments[i]);
            } else {
                transaction.hide(fragments[i]);
            }
        }
        if (addToBackStack) {
            transaction.addToBackStack(null);
        }
        transaction.commit();
        supportInvalidateOptionsMenu();
    }

}

