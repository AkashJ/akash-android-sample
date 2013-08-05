package com.akash.android.sample.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.akash.android.sample.R;
import com.akash.android.sample.base.BaseFragment;
import com.akash.android.sample.util.FragmentsUtility;
import com.facebook.*;
import com.facebook.model.GraphUser;
import com.facebook.widget.ProfilePictureView;
import org.json.JSONArray;
import org.json.JSONException;
import roboguice.inject.InjectView;

public class LoggedInFragment extends BaseFragment {

    @InjectView(R.id.user_pic)          private ProfilePictureView profilePictureView;
    @InjectView(R.id.user_name)         private TextView userNameView;
    @InjectView(R.id.user_location)     private TextView userLocationView;
    @InjectView(R.id.user_education)    private TextView userEducationView;

    private static final String TAG = "LoggedInFragment";
    private static final int REAUTH_ACTIVITY_CODE = 100;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.logged_in_fragment, container, false);
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            makeMeRequest(session);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profilePictureView.setCropped(true);
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
            // Get the user's data.
            makeMeRequest(session);
        }
    }

    private void makeMeRequest(final Session session) {
        Request meRequest = Request.newMeRequest(session,
                new Request.GraphUserCallback() {
                    @Override
                    public void onCompleted(GraphUser user, Response response) {
                        if (session == Session.getActiveSession()) {
                            if (user != null) {
                                //Save the user details in application
                                application.setUser(user);

                                //Set the user details
                                profilePictureView.setProfileId(user.getId());
                                userNameView.setText(user.getName());
                                if (user.getLocation() != null) {
                                    userLocationView.setText(user.getLocation().getProperty("name").toString());
                                } else {
                                    userLocationView.setText("Location not available.");
                                }
                                try {
                                    JSONArray education = user.getInnerJSONObject().getJSONArray("education");
                                    if (education != null && education.length() > 0) {
                                        userEducationView.setText(education.getJSONObject(education.length() - 1).getJSONObject("school").getString("name"));
                                    } else {
                                        userEducationView.setText("Education not available.");
                                    }
                                } catch (JSONException e) {
                                    Log.e("LoggedIn", e.getMessage());
                                }
                            }
                        }
                        if (response.getError() != null) {
                            FacebookRequestError error = response.getError();
                            // Handle the error
                        }
                    }
                });
        meRequest.executeAsync();
    }
}
