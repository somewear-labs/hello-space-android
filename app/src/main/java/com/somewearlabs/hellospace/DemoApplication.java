package com.somewearlabs.hellospace;

import android.app.Application;

import com.somewearlabs.somewearcore.api.SomewearProperties;
import com.somewearlabs.somewearui.api.SomewearUI;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SomewearUI
        SomewearProperties properties = new SomewearProperties(this);
        SomewearUI.setup(properties);
    }
}
