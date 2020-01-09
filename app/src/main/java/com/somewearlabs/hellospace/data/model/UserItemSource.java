package com.somewearlabs.hellospace.data.model;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.somewearlabs.gen.LocationProto;
import com.somewearlabs.somewearcore.api.DataPayload;
import com.somewearlabs.somewearcore.api.DevicePayload;
import com.somewearlabs.somewearcore.api.MessagePayload;

import java.util.ArrayList;
import java.util.List;

public class UserItemSource {

    private List<UserItem> items = new ArrayList<>();
    private UserItemUpdateListener listener;

    public UserItemSource(UserItemUpdateListener listener) {
        this.listener = listener;
    }

    public void createOrUpdateUserItem(DevicePayload devicePayload) {
        UserItem userItem = new UserItem(devicePayload.getParcelId(), devicePayload.getStatus().name());

        if (devicePayload instanceof MessagePayload) {
            MessagePayload messagePayload = (MessagePayload) devicePayload;
            userItem.setItem(messagePayload.getContent());
        }
        else if (devicePayload instanceof DataPayload) {
            DataPayload dataPayload = (DataPayload) devicePayload;

            try {
                LocationProto.Location locationProto = LocationProto.Location.parseFrom(dataPayload.data);
                String formatted = locationProto.getLatitude() + ", " + locationProto.getLongitude();
                userItem.setItem(formatted);
            } catch (InvalidProtocolBufferException e) {
                Log.e("UserItemSource", "createOrUpdateUserItem: failed to parse DataPayload", e);
                return;
            }
        }

        // update model
        ArrayList<UserItem> updatedList = new ArrayList<>();
        boolean didUpdateParcel = false;

        for (UserItem existing : items) {
            if (existing.getId() == userItem.getId()) {
                existing.update(userItem);
                updatedList.add(existing);
                didUpdateParcel = true;
            }
            else {
                updatedList.add(existing);
            }
        }

        if (!didUpdateParcel) {
            updatedList.add(userItem);
        }

        items = updatedList;

        // tell the listener
        listener.didUpdate(items);
    }

    public interface UserItemUpdateListener {
        void didUpdate(List<UserItem> items);
    }
}
