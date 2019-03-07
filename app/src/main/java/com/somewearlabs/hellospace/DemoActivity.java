package com.somewearlabs.hellospace;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.somewearlabs.somewearcore.api.DeviceConnectionState;
import com.somewearlabs.somewearcore.api.DevicePayload;
import com.somewearlabs.somewearcore.api.FirmwareUpdateStatus;
import com.somewearlabs.somewearcore.api.SomewearDevice;
import com.somewearlabs.somewearcore.api.SomewearDeviceCallback;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

public class DemoActivity extends Activity {

    private SomewearDevice device = SomewearDevice.getInstance();
    private SomewearDeviceCallback deviceCallback = device.callback();
    private CompositeDisposable disposable = new CompositeDisposable();
    private List<String> payloadEvents = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> toggleScan());

        Button sendButton = findViewById(R.id.sendButton);
        sendButton.setOnClickListener(v -> sendMessage());

        TextView qualityTextView = findViewById(R.id.qualityTextView);
        TextView batteryTextView = findViewById(R.id.batteryTextView);
        TextView connectionStateTextView = findViewById(R.id.connectionStateTextView);
        TextView activityTextView = findViewById(R.id.activityTextView);

        RecyclerView eventsRecyclerView = findViewById(R.id.eventsRecyclerView);
        eventsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        eventsRecyclerView.setAdapter(eventsAdapter);

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
                        }),

                // Observe quality changes
                device.getQuality()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(quality -> {
                            qualityTextView.setText(getString(R.string.quality_text_view, quality));
                        }),

                // Observe battery changes
                device.getBattery()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(battery -> {
                            batteryTextView.setText(getString(R.string.battery_text_view, battery));
                        }),

                // Observe connection state changes
                device.getConnectionState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(connectionState -> {
                            connectionStateTextView.setText(getString(R.string.connection_state_text_view, connectionState));
                        }),

                // Observe activity state changes
                device.getActivityState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(activityState -> {
                            activityTextView.setText(getString(R.string.activity_state_text_view, activityState));
                        }),

                device.getFirmwareUpdateStatus().subscribe()
        );
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
        DevicePayload payload = DevicePayload.build(data);
        device.sendData(payload);

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

    private void toggleScan() {
        deviceCallback.toggleScan(result -> {
            switch(result) {
                case Success:
                    break;
                case BleUnsupported:
                    // if the device doesn't have BLE, we can't use Somewear and the user shouldn't be here anyway
                    break;
                case BleDisabled:
                    Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivity(intent);
                    break;
                case LocationPermissionRequired:
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, 50);
                    }
                    break;
            }
        });
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
