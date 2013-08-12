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
import com.akash.android.sample.base.*;
import com.facebook.*;
import com.facebook.model.GraphObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import org.json.JSONException;
import roboguice.inject.InjectView;

import java.util.ArrayList;
import java.util.List;

public class ViewPicturesFragment extends BaseFragment {

    @InjectView(R.id.pictures_grid_view) GridView gridView;

    private static final String TAG = "ViewPicturesFragment";
    private static final int REAUTH_ACTIVITY_CODE = 102;
    private List<GridPictureElement> gridElements;
    private GridImageAdapter gridImageAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.view_pictures_fragment, container, false);
        gridElements = new ArrayList<GridPictureElement>();
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        gridImageAdapter = new GridImageAdapter(getActivity(), R.id.pictures_grid_view, gridElements);
        gridView.setAdapter(gridImageAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //TODO
            }
        });
        Session session = Session.getActiveSession();
        if (session != null && session.isOpened()) {
            //See if application already has saved gridPics
            if (savedInstanceState != null && savedInstanceState.containsKey("gridPics")) {
                ArrayList<GridPicture> savedFriends = (ArrayList<GridPicture>) savedInstanceState.getSerializable("gridPics");
                gridElements.clear();
                for (GridPicture picture : savedFriends) {
//                    try {
                        addImageToGrid(getActivity().getApplicationContext(), picture);
//                    } catch (JSONException e) {
//                        Log.e("LoggedIn", e.getMessage());
//                    }
                }
                gridImageAdapter.notifyDataSetChanged();
            } else {
                //if not then retrieve them
                getPictures(session);
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
        //Add current gridPictures data to bundle
        List<GridPicture> gridPictures = new ArrayList<GridPicture>(gridElements.size());
        for (GridPictureElement gridPictureElement : gridElements) {
            gridPictures.add(gridPictureElement.getGridPicture());
        }
        outState.putSerializable("gridPictures", (ArrayList<GridPicture>) gridPictures);
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
            // Get/Update users gridPics.
            getPictures(session);
        }
    }

    private void getPictures(final Session session) {
        final Activity activity = getActivity();
        Bundle requestParams = new Bundle();
        requestParams.putString("filter", "app_2305272732");
        requestParams.putString("fields", "from,picture,likes,type");
        Request picturesFromFeedRequest = new Request(session, "me/home", requestParams, HttpMethod.GET);
        picturesFromFeedRequest.setCallback(new Request.Callback() {
            @Override
            public void onCompleted(Response response) {
                System.out.println("");
                GraphObject graphObject = response.getGraphObject();
                //TODO - populate the grid view
            }
        });
        picturesFromFeedRequest.executeAsync();
    }

    private void addImageToGrid(Context applicationContext, GridPicture picture) {
        gridElements.add(new GridPictureElement(applicationContext, picture));
    }

    private static class GridPicture extends BaseObject {
        GridPicture(String imageUrl, Integer likeCount) {
            super(imageUrl, likeCount);
        }
    }

    private class GridPictureElement extends BaseGridView {

        GridPicture gridPicture;

        public GridPictureElement(final Context context, GridPicture gridPicture) {
            super(context, gridPicture.getImageUrl(), gridPicture.getCount());
            this.gridPicture = gridPicture;
        }

        public GridPictureElement(final Context context, String url, Integer likeCount) {
            super(context, url, likeCount);
            this.gridPicture = new GridPicture(url, likeCount);
        }

        @Override
        public View.OnClickListener getOnClickListener() {
            return new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setCancelable(false);
                    builder.setTitle("Grid Picture View");
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

        public GridPicture getGridPicture() {
            return gridPicture;
        }
    }

    public class GridImageAdapter extends ArrayAdapter<GridPictureElement> {

        private List<GridPictureElement> listElements;
        protected ImageLoader imageLoader = ImageLoader.getInstance();

        public GridImageAdapter(Context context, int resourceId, List<GridPictureElement> listElements) {
            super(context, resourceId, listElements);
            this.listElements = listElements;
            // Set up as an observer for list item changes to
            // refresh the view.
            for (GridPictureElement listElement : listElements) {
                listElement.setAdapter(this);
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.base_grid_view, null);
            }

            GridPictureElement gridPictureElement = listElements.get(position);
            if (gridPictureElement != null) {
                view.setOnClickListener(gridPictureElement.getOnClickListener());
                ImageView image = (ImageView) view.findViewById(R.id.image);
                TextView likeCount = (TextView) view.findViewById(R.id.like_count);
                if (image != null) {
                    imageLoader.displayImage("", image);
                    if (gridPictureElement.getImageUrl() != null && gridPictureElement.getImageUrl().length() > 0) {
                        imageLoader.displayImage(gridPictureElement.getImageUrl(), image);
                        image.setVisibility(View.VISIBLE);
                    }
                }
                if (likeCount != null) {
                    likeCount.setText(gridPictureElement.getLikeCount());
                }
            }
            return view;
        }
    }
}
