package com.somewearlabs.hellospace;

import android.app.Application;

import com.somewearlabs.uisupport.api.SomewearUI;
import com.somewearlabs.uisupport.api.SomewearUIProperties;


public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SomewearUI
        SomewearUIProperties properties = new SomewearUIProperties(this);
        SomewearUI.setup(properties);
    }
}
