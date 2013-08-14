package com.akash.android.sample.base;

import android.content.Context;
import android.util.Pair;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.akash.android.sample.R;
import com.nostra13.universalimageloader.core.ImageLoader;

public abstract class BaseGridView extends RelativeLayout{

    private ImageView imageView;
    private TextView likesCountView;
    private String imageUrl;
    private BaseAdapter adapter;
    protected static ImageLoader imageLoader = ImageLoader.getInstance();

    public BaseGridView(Context context, String imageUrl, Integer likeCount) {
        super(context);
        inflate(context, R.layout.base_grid_view, this);
        this.imageView = (ImageView) findViewById(R.id.image);
        this.likesCountView = (TextView) findViewById(R.id.like_count);
        this.imageUrl = imageUrl;
        imageLoader.displayImage(imageUrl, this.imageView);
        this.likesCountView.setText(likeCount != null ? String.valueOf(likeCount) : String.valueOf(0));
    }

    public void setAdapter(BaseAdapter adapter) {
        this.adapter = adapter;
    }

    public abstract View.OnClickListener getOnClickListener();

    public void setLikesCount(String name) {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        this.likesCountView.setText(name);
    }

    public String getLikeCount() {
        return likesCountView.getText().toString();
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
