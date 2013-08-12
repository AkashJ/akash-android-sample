package com.akash.android.sample.base;

import android.content.Context;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.akash.android.sample.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class BaseRowView extends RelativeLayout {

    private ImageView picture;
    private TextView name;
    private TextView location;
    private BaseAdapter adapter;

    public BaseRowView(final Context context, String url, String name, String location) {
        super(context);
        inflate(context, R.layout.base_row_view, this);
        this.picture = (ImageView) findViewById(R.id.image);
        this.name = (TextView) findViewById(R.id.name);
        this.location = (TextView) findViewById(R.id.location);

        ImageLoader imageLoader = ImageLoader.getInstance();

        imageLoader.displayImage(url, this.picture);
        this.name.setText(name);
        this.location.setText(location);
    }

    public abstract View.OnClickListener getOnClickListener();

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    public ImageView getPicture() {
        return picture;
    }

    public void setPicture(ImageView picture) {
        this.picture = picture;
    }

    public String getName() {
        return name.getText().toString();
    }


    public void setName(String name) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        this.name.setText(name);
    }

    public String getLocation() {
        return location.getText().toString();
    }

    public void setLocation(String location) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        this.location.setText(location);
    }
}
