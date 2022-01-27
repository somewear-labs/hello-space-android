package com.somewearlabs.hellospace.data;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.protobuf.InvalidProtocolBufferException;
import com.somewear.gen.CoordinateProto;
import com.somewear.gen.LocationProto;
import com.somewear.gen.TimestampProto;
import com.somewearlabs.hellospace.R;
import com.somewearlabs.somewearcore.api.DataPayload;
import com.somewearlabs.somewearcore.api.DeviceConnectionState;
import com.somewearlabs.somewearcore.api.DevicePayload;
import com.somewearlabs.somewearcore.api.MessagePayload;
import com.somewearlabs.somewearcore.api.SomewearDevice;
import com.somewearlabs.somewearcore.common.EmailAddress;
import com.somewearlabs.somewearui.api.SomewearPillView;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DataActivity extends AppCompatActivity {

    private SomewearDevice device = SomewearDevice.getInstance();
    private CompositeDisposable disposable = new CompositeDisposable();
    private SimpleRecyclerViewAdapter recyclerViewAdapter = new SimpleRecyclerViewAdapter();
    private LinkedHashMap<Integer, String> payloads = new LinkedHashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data);

        // Configure buttons
        Button sendMessageButton = findViewById(R.id.sendMessageButton);
        Button sendDataButton = findViewById(R.id.sendDataButton);
        sendMessageButton.setOnClickListener(v -> sendMessage());
        sendDataButton.setOnClickListener(v -> sendData());

        // Configure status bar view
        SomewearPillView statusBarView = findViewById(R.id.statusBarView);
        statusBarView.setPresenter(this);

        // Configure payload list
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
                        .subscribe(this::updatePayloadView)
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
        updatePayloadView(message);
    }

    private void sendData() {
        /*
         * Want to send data that doesn't fit into one of our prebuilt types? You can send any
         * arbitrary byte array over satellite using DataPayload.
         */
        float latitude = 37.7796649f;
        float longitude = -122.4039177f;
        long timestampInSeconds = new Date().getTime() / 1000;

        CoordinateProto.CoordinateDto coordinate = CoordinateProto.CoordinateDto.newBuilder()
                .setLatitude(latitude)
                .setLongitude(longitude)
                .build();

        TimestampProto.Timestamp timestamp = TimestampProto.Timestamp.newBuilder()
                .setSeconds(timestampInSeconds)
                .build();

        LocationProto.LocationResponse location = LocationProto.LocationResponse.newBuilder()
                .setCoordinate(coordinate)
                .setTimestamp(timestamp)
                .build();
        DataPayload data = DataPayload.build(location.toByteArray());

        // Send the message via satellite
        device.send(data);

        // show outbound payloads as soon as we hand them off to be sent.
        updatePayloadView(data);
    }

    private void updatePayloadView(DevicePayload payload) {
        // Format payload content before rendering
        String content = "";

        if (payload instanceof MessagePayload) {
            MessagePayload messagePayload = (MessagePayload) payload;
            content = messagePayload.getContent();
        } else if (payload instanceof DataPayload) {
            DataPayload dataPayload = (DataPayload) payload;

            try {
                LocationProto.LocationResponse locationProto = LocationProto.LocationResponse.parseFrom(dataPayload.getData());
                CoordinateProto.CoordinateDto coordinate = locationProto.getCoordinate();

                content = coordinate.getLatitude() + ", " + coordinate.getLongitude();
            } catch (InvalidProtocolBufferException e) {
                Log.e("UserItemSource", "createOrUpdateUserItem: failed to parse DataPayload", e);
                return;
            }
        }

        String formatted = "{ id=" + payload.getParcelId() + ", status='" + payload.getStatus() + '\'' + ", content=" + content + " }";

        // Update view model
        payloads.put(payload.getParcelId(), formatted);

        // update the view
        recyclerViewAdapter.setItems(new ArrayList<String>(payloads.values()));
        recyclerViewAdapter.notifyDataSetChanged();
    }
}
