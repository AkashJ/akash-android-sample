package com.akash.android.sample.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.akash.android.sample.R;
import com.akash.android.sample.base.BaseFragment;
import com.akash.android.sample.base.FragmentInterface;
import com.facebook.Session;
import com.facebook.SessionState;

public class ViewFriendsFragment extends BaseFragment {

    private static final String TAG = "ViewFriendsFragment";
    private static final int REAUTH_ACTIVITY_CODE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.view_friends_fragment, container, false);
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            getPlaces(session);
        }
        return view;
    }

    @Override
    public void onAttach(Activity activityReference){
        super.onAttach(activityReference);
        this.activityReference = (FragmentInterface) activityReference;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REAUTH_ACTIVITY_CODE) {
            uiHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onSessionStateChange(final Session session, SessionState state, Exception exception) {
        if (session != null && session.isOpened()) {
            // Get/Update users friends.
            getPlaces(session);
        }
    }

    private void getPlaces(final Session session) {

    }
}