package com.akash.android.sample.base;

import android.app.Activity;
import android.os.Bundle;
import com.akash.android.sample.AkashAndroidSampleApplication;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.google.inject.Inject;
import roboguice.fragment.RoboFragment;

public abstract class BaseFragment extends RoboFragment{

    @Inject protected AkashAndroidSampleApplication application;
    protected  UiLifecycleHelper uiHelper;
    protected FragmentInterface activityReference;
    protected  Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(final Session session, final SessionState state, final Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uiHelper = new UiLifecycleHelper(getActivity(), callback);
        uiHelper.onCreate(savedInstanceState);
    }

    @Override
    public void onAttach(Activity activityReference){
        super.onAttach(activityReference);
        this.activityReference = (FragmentInterface) activityReference;
    }

    @Override
    public void onResume() {
        super.onResume();
        uiHelper.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        uiHelper.onSaveInstanceState(bundle);
    }

    @Override
    public void onPause() {
        super.onPause();
        uiHelper.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        uiHelper.onDestroy();
    }

    protected abstract void onSessionStateChange(Session session, SessionState state, Exception exception);

}
