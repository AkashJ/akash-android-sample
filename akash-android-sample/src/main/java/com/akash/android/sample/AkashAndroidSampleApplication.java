package com.akash.android.sample;

import android.app.Application;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;

import java.util.List;

public class AkashAndroidSampleApplication extends Application {

    private GraphUser user;
    private List<GraphUser> friends;
    private List<GraphPlace> places;

    public List<GraphUser> getFriends() {
        return friends;
    }

    public void setSelectedUsers(List<GraphUser> users) {
        friends = users;
    }

    public List<GraphPlace> getPlaces() {
        return places;
    }

    public void setSelectedPlaces(List<GraphPlace> places) {
        this.places = places;
    }


    public GraphUser getUser() {
        return user;
    }

    public void setUser(GraphUser user) {
        this.user = user;
    }
}
