package com.akash.android.sample;

import android.app.Application;
import com.facebook.model.GraphPlace;
import com.facebook.model.GraphUser;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.cache.memory.impl.LruMemoryCache;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.utils.StorageUtils;

import java.io.File;
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
                .memoryCacheSize(20 * 1024 * 1024) // 20 Mb
                .memoryCache(new LruMemoryCache(20 * 1024 * 1024))
                .defaultDisplayImageOptions(DisplayImageOptions.createSimple())
                .tasksProcessingOrder(QueueProcessingType.LIFO)
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
