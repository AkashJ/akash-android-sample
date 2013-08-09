package com.akash.android.sample.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.akash.android.sample.R;
import com.akash.android.sample.base.BaseFragment;
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
    private static final String PREF = "HomePref";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        return inflater.inflate(R.layout.logged_in_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        profilePictureView.setCropped(true);
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            if(savedInstanceState != null && savedInstanceState.containsKey("profileId") && savedInstanceState.containsKey("name") && savedInstanceState.containsKey("location") && savedInstanceState.containsKey("education")){
                //If data is available from saved instance then use it and don't make a request
                userNameView.setText(savedInstanceState.getString("name"));
                userLocationView.setText(savedInstanceState.getString("location"));
                userEducationView.setText(savedInstanceState.getString("education"));
                profilePictureView.setProfileId(savedInstanceState.getString("profileId"));
            }else{
                //If data is available from shared preferences then it may be old so display it and then make request to get updated data
                SharedPreferences settings = getActivity().getSharedPreferences(PREF, 0);
                userNameView.setText(settings.getString("name", "Loading..."));
                userLocationView.setText(settings.getString("location", "Loading..."));
                userEducationView.setText(settings.getString("education", "Loading..."));
                profilePictureView.setProfileId(settings.getString("profileId", null));
                makeMeRequest(session);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        //Add current profile data to bundle
        outState.putInt("profileId", profilePictureView.getId());
        outState.putString("name", userNameView.getText().toString());
        outState.putString("location", userLocationView.getText().toString());
        outState.putString("education", userEducationView.getText().toString());

        //Add the current active fragment index to shared preferences
        SharedPreferences settings = getActivity().getSharedPreferences(PREF, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("profileId", profilePictureView.getId());
        editor.putString("name", userNameView.getText().toString());
        editor.putString("location", userLocationView.getText().toString());
        editor.putString("education", userEducationView.getText().toString());

        // Commit the edits!
        editor.commit();
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
