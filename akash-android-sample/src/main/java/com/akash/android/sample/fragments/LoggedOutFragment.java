package com.akash.android.sample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.akash.android.sample.R;
import com.akash.android.sample.base.BaseFragment;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.widget.LoginButton;
import roboguice.inject.InjectView;

import java.util.Arrays;

public class LoggedOutFragment extends BaseFragment {

    @InjectView(R.id.login_button)
    private LoginButton loginButton;

    private static final String TAG = "LoggedOutFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.logged_out_frag, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Set up the permissions on the login button
        Session session = Session.getActiveSession();
        if (session == null || session.isClosed()) {
            loginButton.setReadPermissions(Arrays.asList("user_education_history", "user_location", "friends_location", "read_stream"));
        }
    }


    @Override
    protected void onSessionStateChange(Session session, SessionState state, Exception exception) {
        //No need to do anything
    }
}
