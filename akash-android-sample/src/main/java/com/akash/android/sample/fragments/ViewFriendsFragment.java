package com.akash.android.sample.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.akash.android.sample.R;
import com.akash.android.sample.base.BaseFragment;
import com.akash.android.sample.base.BaseRowView;
import com.akash.android.sample.base.FragmentInterface;
import com.facebook.*;
import com.facebook.model.GraphUser;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ViewFriendsFragment extends BaseFragment {

    @InjectView(R.id.friends_list_view)
    ListView friendListView;
    private List<PeopleListElement> friendRows;

    private static final String TAG = "ViewFriendsFragment";
    private static final int REAUTH_ACTIVITY_CODE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.view_friends_fragment, container, false);
        friendRows = new ArrayList<PeopleListElement>();
        friendRows.add(new PeopleListElement(getActivity().getApplicationContext()));
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            getFriends(session);
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        friendListView.setAdapter(new ActionListAdapter(getActivity(), R.id.friends_list_view, friendRows));
    }

    @Override
    public void onAttach(Activity activityReference) {
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
            getFriends(session);
        }
    }

    private void getFriends(final Session session) {
        Request friendRequest = Request.newMyFriendsRequest(session,
                new Request.GraphUserListCallback() {
                    @Override
                    public void onCompleted(List<GraphUser> friends, Response response) {
                        if (session == Session.getActiveSession()) {
                            if (friends != null && friends.size() > 0) {
                                //save the friends in application
                                application.setSelectedFriends(friends);

                                //Set the friends list
                                friendRows.clear();
                                for (GraphUser friend : friends) {
                                    try {
                                        Map<String, Object> map = friend.asMap();
                                        JSONObject pictureData = ((JSONObject) map.get("picture")).getJSONObject("data");
                                        String location = friend.getLocation() != null ? friend.getLocation().getProperty("name").toString() : "";
                                        String url = (String) pictureData.get("url");
                                        url = "http"+url.split("https")[1];
                                        friendRows.add(new PeopleListElement(getActivity().getApplicationContext(), url, friend.getName(), location));
                                    } catch (JSONException e) {
                                        Log.e("LoggedIn", e.getMessage());
                                    }
                                }
                            }
                        }
                        if (response.getError() != null) {
                            FacebookRequestError error = response.getError();
                            // Handle the error
                        }
                    }
                });
        Bundle params = new Bundle();
        params.putString("fields", "id,name,picture,location");
        friendRequest.setParameters(params);
        friendRequest.executeAsync();
    }

    private class PeopleListElement extends BaseRowView {

        public PeopleListElement(final Context context) {
            super(context, null, "Loading", "");
        }

        public PeopleListElement(final Context context, String url, String name, String location) {
            super(context, url, name, location);
        }

        @Override
        public View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Do nothing
                }
            };
        }
    }

    private class ActionListAdapter extends ArrayAdapter<PeopleListElement> {
        private List<PeopleListElement> listElements;

        public ActionListAdapter(Context context, int resourceId, List<PeopleListElement> baseRowViews) {
            super(context, resourceId, baseRowViews);
            this.listElements = baseRowViews;
            // Set up as an observer for list item changes to
            // refresh the view.
            for (BaseRowView listElement : baseRowViews) {
                listElement.setAdapter(this);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.base_row_view, null);
            }

            PeopleListElement peopleListElement = listElements.get(position);
            if (peopleListElement != null) {
                view.setOnClickListener(peopleListElement.getOnClickListener());
                ImageView image = (ImageView) view.findViewById(R.id.image);
                TextView name = (TextView) view.findViewById(R.id.name);
                TextView location = (TextView) view.findViewById(R.id.location);
                if (image != null) {
                    image = peopleListElement.getPicture();
                }
                if (name != null) {
                    name.setText(peopleListElement.getName());
                }
                if (location != null) {
                    location.setText(peopleListElement.getLocation());
                }
            }
            return view;
        }
    }
}
