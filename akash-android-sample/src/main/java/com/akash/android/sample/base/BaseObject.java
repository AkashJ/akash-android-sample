package com.akash.android.sample.base;

import java.io.Serializable;

public class BaseObject implements Serializable {

    private String name;
    private String location;
    private String imageUrl;
    private Integer count;

    public BaseObject(String... params) {
        name = params[0];
        location = params[1];
        imageUrl = params[2];
    }

    public BaseObject(String imageUrl, Integer count) {
        this.imageUrl = imageUrl;
        this.count = count;
    }

    public String getName() {
        return name != null ? name : "";
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location != null ? location : "";
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImageUrl() {
        return imageUrl != null ? imageUrl : "";
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
