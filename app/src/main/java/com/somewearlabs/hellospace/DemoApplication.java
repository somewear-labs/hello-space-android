package com.somewearlabs.hellospace;

import android.app.Application;

import com.somewearlabs.somewearcore.api.SomewearDevice;
import com.somewearlabs.uisupport.api.SomewearUI;
import com.somewearlabs.uisupport.api.SomewearUIProperties;
import com.somewearlabs.uisupport.service.DfuService;


public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SomewearUI
        SomewearUIProperties properties = new SomewearUIProperties(this);
        properties.setDisplayFirmwareUpdateProgressNotification(false);
        properties.setForceFirmwareUpdate(true);
        properties.setDfuServiceClass(DfuService.class);
        SomewearUI.setup(properties);
    }
}
