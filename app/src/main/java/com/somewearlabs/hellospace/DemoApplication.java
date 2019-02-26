package com.somewearlabs.hellospace;

import android.app.Application;

import com.somewearlabs.somewearcore.api.SomewearDevice;
import com.somewearlabs.somewearcore.api.SomewearProperties;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SomewearDevice
        SomewearProperties properties = new SomewearProperties(this, DeviceNotificationActivity.class);
        SomewearDevice.setup(properties);
    }
}
