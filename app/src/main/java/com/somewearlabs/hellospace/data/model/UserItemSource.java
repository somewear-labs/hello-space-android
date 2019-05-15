package com.somewearlabs.hellospace.data.model;

import android.util.Log;

import com.google.protobuf.InvalidProtocolBufferException;
import com.somewearlabs.gen.LocationProto;
import com.somewearlabs.gen.MessageProto;
import com.somewearlabs.gen.PackageProto;
import com.somewearlabs.somewearcore.api.DataPayload;
import com.somewearlabs.somewearcore.api.DevicePayload;
import com.somewearlabs.somewearcore.api.SomewearDeviceCallback;

import java.util.ArrayList;
import java.util.List;

public class UserItemSource {

    private List<UserItem> items = new ArrayList<>();
    private UserItemUpdateListener listener;

    public UserItemSource(SomewearDeviceCallback deviceCallback, UserItemUpdateListener listener) {
        this.listener = listener;

        // Observe any updates from the device
        deviceCallback.registerAsPayloadListener(this::createOrUpdateUserItem);
    }

    public void createOrUpdateUserItem(DevicePayload devicePayload) {
        // we're only handling data payloads here.
        if (!(devicePayload instanceof DataPayload)) { return; }

        // Convert from data payload to object the app can handle
        DataPayload dataPayload = (DataPayload)devicePayload;

        byte[] data = dataPayload.getData();
        UserItem userItem = new UserItem(dataPayload.getParcelId(), dataPayload.getStatus().name());

        // We can get payloads that are just status updates and have no data
        if (data.length > 0) {
            try {
                // Parse out Package proto which also contains type info.
                PackageProto.Package packageProto = PackageProto.Package.parseFrom(data);
                switch (packageProto.getType()) {
                    case Message:
                        // Convert from wire format to app object format
                        MessageProto.Message messageProto = MessageProto.Message.parseFrom(packageProto.getData());
                        Message message = ProtoMapper.messageFromProto(messageProto);

                        userItem.setItem(message);
                        break;

                    case Location:
                        // Convert from wire format to app object format
                        LocationProto.Location locationProto = LocationProto.Location.parseFrom(packageProto.getData());
                        Location location = ProtoMapper.locationFromProto(locationProto);

                        userItem.setItem(location);
                        break;

                    case Unknown:
                    case UNRECOGNIZED:
                        Log.w("DataActivity", "createOrUpdateParcel: unknown package type; type=" + packageProto.getType());
                }
            } catch (InvalidProtocolBufferException e) {
                Log.e("DataActivity", "createOrUpdateParcel: failed to parse package", e);
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
