package com.somewearlabs.hellospace;

import android.app.Application;

import com.somewearlabs.somewearcore.api.SomewearDevice;
import com.somewearlabs.uisupport.api.SomewearUI;
import com.somewearlabs.uisupport.api.SomewearUIProperties;


public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SomewearUI
        SomewearUIProperties properties = new SomewearUIProperties(this);
        properties.setDisplayFirmwareUpdateProgressNotification(false);
        SomewearUI.setup(properties);
    }
}
