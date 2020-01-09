package com.somewearlabs.hellospace;

import android.app.Application;

import com.somewearlabs.somewearui.api.SomewearUI;
import com.somewearlabs.somewearui.api.SomewearUIProperties;


public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SomewearUI
        SomewearUIProperties properties = new SomewearUIProperties(this);
        SomewearUI.setup(properties);
    }
}
