package com.akash.android.sample;

import android.app.Application;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

import java.util.List;

public class AkashAndroidSampleApplication extends Application {

    private GraphUser user;
    private List<GraphUser> friends;
    private List<GraphPlace> places;

    @Override
    public void onCreate() {
        super.onCreate();
        // Create global configuration and initialize ImageLoader with this configuration
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
                .threadPriority(Thread.NORM_PRIORITY - 2)
                .denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
                .writeDebugLogs()
                .build();
        ImageLoader.getInstance().init(config);
    }

    public List<GraphUser> getFriends() {
        return friends;
    }

    public void setSelectedFriends(List<GraphUser> users) {
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
