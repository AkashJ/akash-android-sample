package com.akash.android.sample.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.akash.android.sample.R;
import com.akash.android.sample.base.BaseFragment;
import com.akash.android.sample.base.BaseRowView;
import com.akash.android.sample.base.FragmentInterface;
import com.facebook.*;
import com.facebook.model.GraphPlace;
import org.json.JSONException;
import org.json.JSONObject;
import roboguice.inject.InjectView;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CheckInFragment extends BaseFragment implements LocationListener {

    private static final String TAG = "CheckInFragment";
    private static final int REAUTH_ACTIVITY_CODE = 103;
    private static final int RADIUS_IN_METERS = 600;
    private static final int RESULTS_LIMIT = 15;
    private static final int MIN_TIME = 120000;
    private static final int MIN_DISTANCE = 1;

    @InjectView(R.id.check_in_list_view)
    ListView checkInListView;
    private List<PlaceListElement> placeRows;
    private LocationManager locationManager;
    private String provider;
    private boolean requestStarted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.check_in_fragment, container, false);
        placeRows = new ArrayList<PlaceListElement>();
        placeRows.add(new PlaceListElement(getActivity().getApplicationContext()));

        //set up location manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        provider = locationManager.getBestProvider(new Criteria(), false);
        return view;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TextView header = new TextView(getActivity());
        header.setText("Places NearBy");
        int paddingPixel = 5;
        float density = getActivity().getResources().getDisplayMetrics().density;
        int paddingDp = (int)(paddingPixel * density);
        header.setPadding(paddingDp,paddingDp,paddingDp,paddingDp);
        header.setTextAppearance(getActivity(), R.style.H1_light);
        checkInListView.addHeaderView(header);
        checkInListView.setAdapter(new CustomListAdapter(getActivity(), R.id.friends_list_view, placeRows));
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            if(savedInstanceState != null && savedInstanceState.containsKey("friends")){
                placeRows.clear();
                List<PlaceListElement> savedFriends = (List<PlaceListElement>) savedInstanceState.getSerializable("friends");
                for(PlaceListElement itr: savedFriends){
                    placeRows.add(itr);
                }
            }
        }
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
        //ignore
    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //Add current friends data to bundle
        outState.putSerializable("friends", (ArrayList) placeRows);
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        this.requestStarted = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened() && location != null && !requestStarted) {
            // Get/Update places.
            requestStarted = true;
            getPlaces(session, location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.i("LOC", "Provider" + provider + " status changed to " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.i("LOC", "Enabled new provider" + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.i("LOC", "Disabled provider" + provider);
    }


    private void getPlaces(final Session session, final Location location) {
        final Activity activity = getActivity();
        Request placeRequest = Request.newPlacesSearchRequest(session, location, RADIUS_IN_METERS, RESULTS_LIMIT, "",
                new Request.GraphPlaceListCallback() {
                    @Override
                    public void onCompleted(List<GraphPlace> places, Response response) {
                        if (session == Session.getActiveSession() && activity != null && !activity.isFinishing() && !activity.isChangingConfigurations()) {
                            placeRows.clear();
                            if (places != null && places.size() > 0) {
                                //save the friends in application
                                application.setSelectedPlaces(places);

                                //Set the places list
                                for (GraphPlace place : places) {
                                    try {
                                        Map<String, Object> map = place.asMap();
                                        String location = (String) ((JSONObject) map.get("location")).get("street");
                                        placeRows.add(new PlaceListElement(activity.getApplicationContext(), null, (String) map.get("name"), location, place));
                                    } catch (JSONException e) {
                                        Log.e("LoggedIn", e.getMessage());
                                    }
                                }
                            } else {
                                placeRows.add(new PlaceListElement(activity.getApplicationContext(), null, "No Places Found", "", null));
                                requestStarted = false;
                            }
                        }
                    }
                });
        placeRequest.executeAsync();
    }

    private class PlaceListElement extends BaseRowView {

        public PlaceListElement(final Context context) {
            super(context, null, "Loading", "", null);
        }

        public PlaceListElement(final Context context, String url, String name, String location, GraphPlace place) {
            super(context, url, name, location, place);
        }

        @Override
        public View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    builder.setTitle("Check In");
                    builder.setMessage("functionality under construction");
                    builder.setPositiveButton(getActivity().getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    builder.show();
//                    GraphPlace place = (GraphPlace) ((BaseRowView) view).getGraphObject();
//                    if (place != null) {
//                        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", getActivity().getResources().getString(R.string.progress_dialog), true);
//
//                        Bundle params = new Bundle();
//                        params.putString("place", place.getId());
//                        params.putString("message", "Testing open graph api check In");
//                        params.putString("coordinates", place.asMap().get("location").toString());
//                        final Request request = Request.newPostRequest(Session.getActiveSession(), "me/checkins", place, new Request.Callback() {
//                            @Override
//                            public void onCompleted(Response response) {
//
//                            }
//                        });
//
//                        AsyncTask<Void, Void, Response> task =
//                                new AsyncTask<Void, Void, Response>() {
//                                    @Override
//                                    protected Response doInBackground(Void... voids) {
//                                        return request.executeAndWait();
//                                    }
//
//                                    @Override
//                                    protected void onPostExecute(Response response) {
//                                        if (progressDialog != null) {
//                                            progressDialog.dismiss();
//                                        }
//                                    }
//                                };
//                        task.execute();
//                    }
                }
            };
        }
    }

    private class CustomListAdapter extends ArrayAdapter<PlaceListElement> {
        private List<PlaceListElement> listElements;

        public CustomListAdapter(Context context, int resourceId, List<PlaceListElement> placeListElements) {
            super(context, resourceId, placeListElements);
            this.listElements = placeListElements;
            // Set up as an observer for list item changes to
            // refresh the view.
            for (PlaceListElement listElement : placeListElements) {
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

            PlaceListElement placeListElement = listElements.get(position);
            if (placeListElement != null) {
                view.setOnClickListener(placeListElement.getOnClickListener());
                ImageView image = (ImageView) view.findViewById(R.id.image);
                TextView name = (TextView) view.findViewById(R.id.name);
                TextView location = (TextView) view.findViewById(R.id.location);
                if (image != null) {
                    image = placeListElement.getPicture();
                }
                if (name != null) {
                    name.setText(placeListElement.getName());
                }
                if (location != null) {
                    location.setText(placeListElement.getLocation());
                }
            }
            return view;
        }
    }
}
