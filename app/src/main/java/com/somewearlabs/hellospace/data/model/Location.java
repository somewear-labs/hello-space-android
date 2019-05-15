package com.somewearlabs.hellospace.data.model;

import android.support.annotation.NonNull;

import java.util.Date;

public class Location {
    private float latitude;
    private float longitude;
    private Date timestamp;

    public Location(float latitude, float longitude, @NonNull Date timestamp) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
    }

    public float getLatitude() {
        return latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    @NonNull
    public Date getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "Location{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                ", timestamp=" + timestamp +
                '}';
    }
}
