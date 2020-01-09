package com.somewearlabs.hellospace.data;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.somewearlabs.gen.LocationProto;
import com.somewearlabs.hellospace.R;
import com.somewearlabs.hellospace.data.model.UserItem;
import com.somewearlabs.hellospace.data.model.UserItemSource;
import com.somewearlabs.somewearcore.api.DataPayload;
import com.somewearlabs.somewearcore.api.DeviceConnectionState;
import com.somewearlabs.somewearcore.api.MessagePayload;
import com.somewearlabs.somewearcore.api.SomewearDevice;
import com.somewearlabs.somewearcore.common.EmailAddress;
import com.somewearlabs.somewearui.api.SomewearStatusBarView;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DataActivity extends AppCompatActivity {

    private SomewearDevice device = SomewearDevice.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();
    private UserItemSource userItemSource;
    private SimpleRecyclerViewAdapter recyclerViewAdapter = new SimpleRecyclerViewAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // Whenever UserItemSource updates it's items, we update the view.
        userItemSource = new UserItemSource(this::userItemsDidUpdate);

        // Configure buttons
        Button sendMessageButton = findViewById(R.id.sendMessageButton);
        Button sendDataButton = findViewById(R.id.sendDataButton);
        sendMessageButton.setOnClickListener(v -> sendMessage());
        sendDataButton.setOnClickListener(v -> sendData());

        // Configure status bar view
        SomewearStatusBarView statusBarView = findViewById(R.id.statusBarView);
        statusBarView.setPresenter(this);

        // Configure UserItem list
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addItemDecoration(new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL));

        disposable.addAll(
                // Observe connectivity changes
                device.getConnectionState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(connectionState -> {
                            // Hide buttons when not connected
                            boolean isVisible = connectionState == DeviceConnectionState.Connected;
                            int visibility = isVisible ? View.VISIBLE : View.INVISIBLE;
                            sendMessageButton.setVisibility(visibility);
                            sendDataButton.setVisibility(visibility);
                        }),

                // Observe any updates from the device
                device.getPayload()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(payload -> {
                            userItemSource.createOrUpdateUserItem(payload);
                        })
        );
    }

    @Override
    protected void onDestroy() {
        // Unregister any callbacks/observers
        disposable.dispose();
        super.onDestroy();
    }

    private void sendMessage() {
        // Craft a message
        String content = "Hello from space!";
        EmailAddress email = EmailAddress.build("someweardev@gmail.com");
        MessagePayload message = MessagePayload.build(content, email);

        // Send the message via satellite
        device.send(message);

        // show outbound payloads as soon as we hand them off to be sent.
        userItemSource.createOrUpdateUserItem(message);
    }

    private void sendData() {
        /*
         * Want to send data that doesn't fit into one of our prebuilt types? You can send any
         * arbitrary byte array over satellite using DataPayload.
         *
         * Note: You can actually create a LocationPayload, this is just a simple example using a
         *       protobuf to send structured data over the wire.
         */
        float latitude = 37.7796649f;
        float longitude = -122.4039177f;
        Date timestamp = new Date();
        LocationProto.Location proto = LocationProto.Location.newBuilder()
                .setLatitude(latitude)
                .setLongitude(longitude)
                .setTimestamp(timestamp.getTime() / 1000)
                .build();
        DataPayload data = DataPayload.build(proto.toByteArray());

        // Send the message via satellite
        device.send(data);

        // show outbound payloads as soon as we hand them off to be sent.
        userItemSource.createOrUpdateUserItem(data);
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
