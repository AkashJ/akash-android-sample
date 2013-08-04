package com.akash.android.sample.test;

import android.test.ActivityInstrumentationTestCase2;
import com.akash.android.sample.*;

public class HomeActivityTest extends ActivityInstrumentationTestCase2<HomeActivity> {

    public HomeActivityTest() {
        super(HomeActivity.class);
    }

    public void testActivity() {
        HomeActivity activity = getActivity();
        assertNotNull(activity);
    }
}

