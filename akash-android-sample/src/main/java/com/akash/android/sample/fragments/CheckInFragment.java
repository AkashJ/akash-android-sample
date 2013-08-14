package com.akash.android.sample.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
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
import com.facebook.model.GraphPlace;
import com.nostra13.universalimageloader.core.ImageLoader;
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
    private static final int MIN_TIME = 60000;
    private static final int MIN_DISTANCE = 0;

    @InjectView(R.id.check_in_list_view)
    ListView checkInListView;
    PlaceListAdapter placeListAdapter;
    private List<PlaceListElement> placeRows;
    private LocationManager locationManager;
    private String provider;
    private boolean requestStarted = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.check_in_fragment, container, false);
        placeRows = new ArrayList<PlaceListElement>();
        placeRows.add(new PlaceListElement(getActivity().getApplicationContext(), new Place(null, "Loading...", "", "")));

        //set up location manager
        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        provider = locationManager.getBestProvider(criteria, false);
        onLocationChanged(locationManager.getLastKnownLocation(provider));
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
        int paddingDp = (int) (paddingPixel * density);
        header.setPadding(paddingDp, paddingDp, paddingDp, paddingDp);
        header.setTextAppearance(getActivity(), R.style.H1_light);
        checkInListView.addHeaderView(header);
        placeListAdapter = new PlaceListAdapter(getActivity(), R.id.check_in_list_view, placeRows);
        checkInListView.setAdapter(placeListAdapter);
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            //See if savedInstance already has saved places
            if (savedInstanceState != null && savedInstanceState.containsKey("places")) {
                ArrayList<Place> savedPlaces = (ArrayList<Place>) savedInstanceState.getSerializable("places");
                placeRows.clear();
                for (Place place : savedPlaces) {
                    try {
                        addPlaceToList(getActivity().getApplicationContext(), place);
                    } catch (JSONException e) {
                        Log.e("CheckIn", e.getMessage());
                    }
                }
                placeListAdapter.notifyDataSetChanged();
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
        //Add current places data to bundle
        List<Place> places = new ArrayList<Place>(placeRows.size());
        for (PlaceListElement placeListElement : placeRows) {
            places.add(placeListElement.getPlace());
        }
        outState.putSerializable("places", (ArrayList<Place>) places);
    }


    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        this.requestStarted = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            placeRows.clear();
            placeRows.add(new PlaceListElement(getActivity().getApplicationContext(), new Place(null, "Problem getting location", "", "")));
            requestStarted = false;
        } else {
            Session session = Session.getActiveSession();
            if (session != null && session.isOpened() && !requestStarted) {
                // Get/Update places.
                requestStarted = true;
                getPlaces(session, location);
            }
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
        final FragmentActivity activity = getActivity();
        Request placeRequest = Request.newPlacesSearchRequest(session, location, RADIUS_IN_METERS, RESULTS_LIMIT, "",
                new Request.GraphPlaceListCallback() {
                    @Override
                    public void onCompleted(List<GraphPlace> places, Response response) {
                        if (session == Session.getActiveSession() && activity != null && !activity.isFinishing()) {
                            placeRows.clear();
                            if (places != null && places.size() > 0) {
                                //save the places in application
                                application.setSelectedPlaces(places);

                                //Set the places list
                                for (GraphPlace graphPlace : places) {
                                    try {
                                        Map<String, Object> map = graphPlace.asMap();
                                        String location = (String) ((JSONObject) map.get("location")).get("street");
                                        addPlaceToList(activity.getApplicationContext(), new Place(graphPlace, (String) map.get("name"), location, ""));
                                    } catch (JSONException e) {
                                        Log.e("CheckIn", e.getMessage());
                                    }
                                }
                            } else {
                                placeRows.add(new PlaceListElement(activity.getApplicationContext(), new Place(null, "No Places Found", "", "")));
                                requestStarted = false;
                            }
                            placeListAdapter.notifyDataSetChanged();
                        }
                    }
                });
        placeRequest.executeAsync();
    }

    private void addPlaceToList(Context applicationContext, Place place) throws JSONException {
        placeRows.add(new PlaceListElement(applicationContext, place));
    }

    private static class Place extends BaseObject {

        GraphPlace graphPlace;

        Place(GraphPlace graphPlace, String... params) {
            super(params);
            this.graphPlace = graphPlace;
        }

        public GraphPlace getGraphPlace() {
            return graphPlace;
        }

    }

    private class PlaceListElement extends BaseRowView {

        private Place place;

        public PlaceListElement(final Context context, Place place) {
            super(context, place.getImageUrl(), place.getName(), place.getLocation());
            this.place = place;
        }

        @Override
        public View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    GraphPlace place = ((PlaceListElement) view).getPlace().getGraphPlace();
                    if (place != null) {
                        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "", getActivity().getResources().getString(R.string.progress_dialog), true);

                        Bundle params = new Bundle();
                        params.putString("place", place.getId());
                        params.putString("message", "Testing open graph api check In");
                        final String location = place.asMap().get("location").toString();
                        params.putString("coordinates", location);
                        final Request request = Request.newPostRequest(Session.getActiveSession(), "me/checkins", place, new Request.Callback() {
                            @Override
                            public void onCompleted(Response response) {

                            }
                        });
                        AsyncTask<Void, Void, Response> task =
                                new AsyncTask<Void, Void, Response>() {
                                    @Override
                                    protected Response doInBackground(Void... voids) {
                                        return request.executeAndWait();
                                    }

                                    @Override
                                    protected void onPostExecute(Response response) {
                                        if (progressDialog != null) {
                                            progressDialog.dismiss();
                                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                            builder.setCancelable(false);
                                            builder.setTitle("Check In");
                                            builder.setMessage("Successfully checked in at " + location);
                                            builder.setPositiveButton(getActivity().getString(R.string.ok_text), new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                            builder.show();

                                        }
                                    }
                                };
                        task.execute();
                    }
                }
            };
        }

        public Place getPlace() {
            return place;
        }
    }

    private class PlaceListAdapter extends ArrayAdapter<PlaceListElement> {
        private List<PlaceListElement> listElements;
        protected ImageLoader imageLoader = ImageLoader.getInstance();

        public PlaceListAdapter(Context context, int resourceId, List<PlaceListElement> placeListElements) {
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
                    imageLoader.displayImage("", image);
                    if (placeListElement.getImageUrl() != null && placeListElement.getImageUrl().length() > 0) {
                        imageLoader.displayImage(placeListElement.getImageUrl(), image);
                        image.setVisibility(View.VISIBLE);
                    } else {
                        image.setVisibility(View.GONE);
                        image.invalidate();
                    }
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
