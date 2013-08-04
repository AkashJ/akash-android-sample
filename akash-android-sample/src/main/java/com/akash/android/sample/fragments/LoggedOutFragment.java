package com.akash.android.sample.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.akash.android.sample.R;
import com.akash.android.sample.base.BaseFragment;

public class LoggedOutFragment extends BaseFragment {

    private static final String TAG = "LoggedOutFragment";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.logged_out_frag, container, false);
    }

}
