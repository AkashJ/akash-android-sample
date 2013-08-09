package com.akash.android.sample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import com.akash.android.sample.base.BaseActivity;
import com.akash.android.sample.base.FragmentInterface;
import com.akash.android.sample.util.FragmentsUtility;
import com.facebook.Session;
import com.facebook.SessionState;
import com.google.inject.Inject;

public class HomeActivity extends BaseActivity implements FragmentInterface{

    @Inject FragmentManager fm;
    private Fragment[] fragments = new Fragment[FragmentsUtility.FRAGMENT_COUNT];
    private MenuItem settings;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);
        fragments[FragmentsUtility.LOGGED_OUT] = fm.findFragmentById(R.id.loggedOutFrag);
        fragments[FragmentsUtility.LOGGED_IN] = fm.findFragmentById(R.id.loggedInFrag);
        fragments[FragmentsUtility.SETTINGS] = fm.findFragmentById(R.id.userSettingsFragment);
        fragments[FragmentsUtility.FRIENDS] = fm.findFragmentById(R.id.viewFriendsFrag);
        fragments[FragmentsUtility.PLACES] = fm.findFragmentById(R.id.checkInFrag);
        fragments[FragmentsUtility.PICTURES] = fm.findFragmentById(R.id.viewPicturesFrag);
        FragmentTransaction transaction = fm.beginTransaction();
        for (Fragment fragment : fragments) {
            transaction.hide(fragment);
        }
        transaction.commit();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // only add the menu when the selection fragment is showing
        if (!fragments[FragmentsUtility.LOGGED_OUT].isVisible()) {
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
            showFragment(FragmentsUtility.SETTINGS, true);
            return true;
        }
        return false;
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            showFragment(FragmentsUtility.LOGGED_IN, false);
        } else {
            showFragment(FragmentsUtility.LOGGED_OUT, false);
        }
    }

    @Override
    protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
        if (isResumed) {
            int backStackSize = fm.getBackStackEntryCount();
            for (int i = 0; i < backStackSize; i++) {
                fm.popBackStack();
            }
            if (state.isOpened()) {
                showFragment(FragmentsUtility.LOGGED_IN, false);
            } else if (state.isClosed()) {
                showFragment(FragmentsUtility.LOGGED_OUT, false);
            }
        }
    }

    public  void onSelectViewFriends(final View v) {
        switchFragment(FragmentsUtility.FRIENDS);
    }

    public void onSelectCheckIn(final View v) {
        switchFragment(FragmentsUtility.PLACES);
    }

    public void onSelectViewPictures(final View v) {
        switchFragment(FragmentsUtility.PICTURES);
    }

    private void showFragment(int fragmentIndex, boolean addToBackStack) {
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

    public void switchFragment(int fragmentIndex) {
        if(isResumed){
            showFragment(fragmentIndex, true);
        }
    }

    public Fragment[] getFragments(){
        return this.fragments;
    }

    @Override
    public void performActionOnActivity(){
        //if we need to communicate from fragment to activity
    }
}

