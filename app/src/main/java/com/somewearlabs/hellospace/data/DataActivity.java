package com.somewearlabs.hellospace.data;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;

import com.somewearlabs.gen.PackageProto;
import com.somewearlabs.hellospace.R;
import com.somewearlabs.hellospace.data.model.Location;
import com.somewearlabs.hellospace.data.model.Message;
import com.somewearlabs.hellospace.data.model.ProtoMapper;
import com.somewearlabs.hellospace.data.model.UserItem;
import com.somewearlabs.hellospace.data.model.UserItemSource;
import com.somewearlabs.somewearcore.api.DataPayload;
import com.somewearlabs.somewearcore.api.DeviceConnectionState;
import com.somewearlabs.somewearcore.api.SomewearDevice;
import com.somewearlabs.somewearcore.api.SomewearDeviceCallback;
import com.somewearlabs.uisupport.api.SomewearStatusBarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class DataActivity extends FragmentActivity {

    private SomewearDevice device = SomewearDevice.getInstance();
    private SomewearDeviceCallback deviceCallback = device.callback();
    private Disposable disposable;
    private UserItemSource userItemSource;
    private SimpleRecyclerViewAdapter recyclerViewAdapter = new SimpleRecyclerViewAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // Whenever UserItemSource updates it's items, we update the view.
        userItemSource = new UserItemSource(deviceCallback, this::userItemsDidUpdate);

        // Configure buttons
        Button sendMessageButton = findViewById(R.id.sendMessageButton);
        sendMessageButton.setOnClickListener(v -> sendMessage());

        Button sendLocationButton = findViewById(R.id.sendLocationButton);
        sendLocationButton.setOnClickListener(v -> sendLocation());

        // Configure status bar view
        SomewearStatusBarView statusBarView = findViewById(R.id.statusBarView);
        statusBarView.setPresenter(this);

        // Configure UserItem list
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        // Observe connectivity changes
        disposable = device.getConnectionState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectionState -> {
                    // Hide buttons when not connected
                    boolean isVisible = connectionState == DeviceConnectionState.Connected;
                    sendMessageButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
                    sendLocationButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
                });
    }

    @Override
    protected void onDestroy() {
        // Unregister any callbacks/observers
        deviceCallback.unregisterListeners();
        disposable.dispose();
        super.onDestroy();
    }

    private void sendMessage() {
        // Craft a message
        String content = "Hello from space!";
        String email = "someweardev@gmail.com";
        Date timestamp = new Date();
        Message message = new Message(email, "", timestamp, content);

        // Let's send data over the wire in a structured & condensed format.
        PackageProto.Package packageProto = ProtoMapper.packageFromMessage(message);
        sendPackage(packageProto);
    }

    private void sendLocation() {
        // Craft a location
        float latitude = 37.7796649f;
        float longitude = -122.4039177f;
        Date timestamp = new Date();
        Location location = new Location(latitude, longitude, timestamp);

        // Let's send data over the wire in a structured & condensed format.
        PackageProto.Package packageProto = ProtoMapper.packageFromLocation(location);
        sendPackage(packageProto);
    }

    private void sendPackage(PackageProto.Package packageProto) {
        byte[] payloadData = packageProto.toByteArray();

        DataPayload payload = DataPayload.build(payloadData);
        device.sendData(payload);

        // show outbound payloads as soon as we hand them off to be sent.
        userItemSource.createOrUpdateUserItem(payload);
    }

    private void userItemsDidUpdate(List<UserItem> items) {
        // prepare model for presentation
        List<String> viewModels = new ArrayList<>();
        for (UserItem p : items) {
            viewModels.add(p.toString());
        }

        // update the view
        recyclerViewAdapter.setItems(viewModels);
        recyclerViewAdapter.notifyDataSetChanged();
    }
}
