package com.somewearlabs.hellospace;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.somewearlabs.somewearcore.api.DataPayload;
import com.somewearlabs.somewearcore.api.DeviceConnectionState;
import com.somewearlabs.somewearcore.api.DevicePayload;
import com.somewearlabs.somewearcore.api.FirmwareUpdateStatus;
import com.somewearlabs.somewearcore.api.SomewearDevice;
import com.somewearlabs.somewearcore.api.SomewearDeviceCallback;
import com.somewearlabs.uisupport.api.SomewearStatusBarView;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DemoActivity extends FragmentActivity {

    private SomewearDevice device = SomewearDevice.getInstance();
    private SomewearDeviceCallback deviceCallback = device.callback();
    private CompositeDisposable disposable = new CompositeDisposable();
    private List<String> payloadEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> sendMessage());

        RecyclerView eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(eventsAdapter);

        // Configure status bar view
        SomewearStatusBarView statusBarView = findViewById(R.id.statusBarView);
        statusBarView.setPresenter(this);

        // Observe payload changes
        deviceCallback.registerAsPayloadListener(this::didReceivePayload);

        // Observe Firmware update status changes
        deviceCallback.registerAsFirmwareUpdateListener(this::didReceiveFirmwareUpdate);

        disposable.addAll(
                // Observe connectivity changes
                device.getConnectionState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(connectionState -> {
                            // Hide sendButton when not connected
                            boolean isVisible = connectionState == DeviceConnectionState.Connected;
                            sendButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
                        })
        );

        Log.d("DemoActivity","connectionState=" + device.getConnectionState().getValue().name());
    }

    @Override
    protected void onDestroy() {
        // Unregister any callbacks/observers
        deviceCallback.unregisterListeners();
        disposable.clear();
        super.onDestroy();
    }

    private void sendMessage() {
        String message = "Hello from space!";
        byte[] data = message.getBytes(StandardCharsets.UTF_8);
        DevicePayload payload = DataPayload.build(data);
        device.sendData(payload);

        Log.d("DemoActivity","connectionState=" + device.getConnectionState().getValue().name());

        // Add a send event
        addEvent("id=" + payload.getParcelId() + "; msg=" + message);
    }

    private void didReceivePayload(DevicePayload payload) {
        // When we receive a payload, add an event
        addEvent("id=" + payload.getParcelId() + "; status=" + payload.getStatus());
    }

    private void didReceiveFirmwareUpdate(FirmwareUpdateStatus status) {
        switch (status) {
            case None:
                break;
            case Available:
                presentFirmwareUpdateAvailableDialog();
                break;
            case Required:
                presentFirmwareUpdateRequiredDialog();
                break;
        }
    }

    private void addEvent(String eventMessage) {
        String event = String.format(Locale.getDefault(), "%1$tH:%1$tM:%1$tS %2$s", new Date(), eventMessage);
        payloadEvents.add(0, event);
        eventsAdapter.notifyItemInserted(0);
    }

    private void presentFirmwareUpdateAvailableDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Available")
                .setMessage("Your Somewear hotspot has an update available. Update to get the most out your device!")
                .setPositiveButton("Update", (dialog, which) -> startFirmwareUpdate())
                .setNegativeButton("Cancel", (dialog, which) -> {}) // ignore
                .create()
                .show();
    }

    private void presentFirmwareUpdateRequiredDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Update Required")
                .setMessage("A critical software update is required for your Somewear hotspot. You cannot use your Somewear hotspot until this update is installed.")
                .setPositiveButton("Update", (dialog, which) -> startFirmwareUpdate())
                .setNegativeButton("Disconnect", (dialog, which) -> deviceCallback.disconnect())
                .create()
                .show();
    }

    private void startFirmwareUpdate() {
        deviceCallback.startFirmwareUpdate(result -> {
            switch (result) {
                case Success:
                    break;
                case Busy:
                    break;
                case DeviceDisconnected:
                case Error:
                    // try again before this?
                    deviceCallback.disconnect();
                    break;
            }
        });
    }

    private RecyclerView.Adapter eventsAdapter = new RecyclerView.Adapter() {
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return DemoActivity.EventViewHolder.build(parent);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
            TextView textView = (TextView) holder.itemView;
            textView.setText(payloadEvents.get(position));
        }

        @Override
        public int getItemCount() {
            return payloadEvents.size();
        }
    };

    private static class EventViewHolder extends RecyclerView.ViewHolder {

        private EventViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        static DemoActivity.EventViewHolder build(ViewGroup parent) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new DemoActivity.EventViewHolder(v);
        }
    }
}
