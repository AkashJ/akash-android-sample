package com.akash.android.sample.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.akash.android.sample.R;
import com.akash.android.sample.base.BaseFragment;
import com.akash.android.sample.base.BaseObject;
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
    FriendsListAdapter friendsListAdapter;
    private List<PeopleListElement> friendRows;

    private static final String TAG = "ViewFriendsFragment";
    private static final int REAUTH_ACTIVITY_CODE = 101;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.view_friends_fragment, container, false);
        friendRows = new ArrayList<PeopleListElement>();
        friendRows.add(new PeopleListElement(getActivity().getApplicationContext(), new Friend("Loading...", "", "")));
        return view;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView header = new TextView(getActivity());
        header.setText("Friends List");
        int paddingPixel = 5;
        float density = getActivity().getResources().getDisplayMetrics().density;
        int paddingDp = (int) (paddingPixel * density);
        header.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        header.setTextAppearance(getActivity(), R.style.H1_light);
        friendListView.addHeaderView(header);
        friendsListAdapter = new FriendsListAdapter(getActivity(), R.id.friends_list_view, friendRows);
        friendListView.setAdapter(friendsListAdapter);
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            //See if application already has saved friends
            if (savedInstanceState != null && savedInstanceState.containsKey("friends")) {
                ArrayList<Friend> savedFriends = (ArrayList<Friend>) savedInstanceState.getSerializable("friends");
                friendRows.clear();
                for (Friend friend : savedFriends) {
                    try {
                        addFriendToList(getActivity().getApplicationContext(), friend);
                    } catch (JSONException e) {
                        Log.e("LoggedIn", e.getMessage());
                    }
                }
                friendsListAdapter.notifyDataSetChanged();
            } else {
                //if not then retrieve them
                getFriends(session);
            }
        }
    }

    @Override
    public void onAttach(Activity activityReference) {
        super.onAttach(activityReference);
        this.activityReference = (FragmentInterface) activityReference;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Add current friends data to bundle
        List<Friend> friends = new ArrayList<Friend>(friendRows.size());
        for (PeopleListElement friendListElement : friendRows) {
            friends.add(friendListElement.getFriend());
        }
        outState.putSerializable("friends", (ArrayList<Friend>) friends);
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
        final Activity activity = getActivity();
        Request friendRequest = Request.newMyFriendsRequest(session,
                new Request.GraphUserListCallback() {
                    @Override
                    public void onCompleted(List<GraphUser> friends, Response response) {
                        if (session == Session.getActiveSession() && activity != null && !activity.isFinishing() && !activity.isChangingConfigurations()) {
                            friendRows.clear();
                            if (friends != null && friends.size() > 0) {
                                //save the friends in application
                                application.setSelectedFriends(friends);

                                //Set the friends list
                                for (GraphUser friend : friends) {
                                    try {
                                        Map<String, Object> map = friend.asMap();
                                        JSONObject pictureData = ((JSONObject) map.get("picture")).getJSONObject("data");
                                        String location = friend.getLocation() != null ? friend.getLocation().getProperty("name").toString() : "";
                                        String url = (String) pictureData.get("url");
                                        url = "http" + url.split("https")[1];
                                        addFriendToList(activity.getApplicationContext(), new Friend(friend.getName(), location, url));
                                    } catch (JSONException e) {
                                        Log.e("LoggedIn", e.getMessage());
                                    }
                                }
                            } else {
                                friendRows.add(new PeopleListElement(activity.getApplicationContext(), null, "No Friends Found", ""));
                            }
                            friendsListAdapter.notifyDataSetChanged();
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

    private void addFriendToList(Context applicationContext, Friend friend) throws JSONException {
        friendRows.add(new PeopleListElement(applicationContext, friend));
    }

    private static class Friend extends BaseObject {
        Friend(String... params) {
            super(params);
        }
    }

    private class PeopleListElement extends BaseRowView {

        Friend friend;

        public PeopleListElement(final Context context, Friend friend) {
            super(context, friend.getImageUrl(), friend.getName(), friend.getLocation());
            this.friend = friend;
        }

        public PeopleListElement(final Context context, String url, String name, String location) {
            super(context, url, name, location);
            this.friend = new Friend(name, location, url);
        }

        @Override
        public View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    builder.setTitle("Friend Details View");
                    builder.setMessage("functionality under construction");
                    builder.setPositiveButton(getActivity().getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
                }
            };
        }

        public Friend getFriend() {
            return friend;
        }
    }

    private class FriendsListAdapter extends ArrayAdapter<PeopleListElement> {
        private List<PeopleListElement> listElements;

        public FriendsListAdapter(Context context, int resourceId, List<PeopleListElement> peopleListElements) {
            super(context, resourceId, peopleListElements);
            this.listElements = peopleListElements;
            // Set up as an observer for list item changes to
            // refresh the view.
            for (PeopleListElement listElement : peopleListElements) {
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
