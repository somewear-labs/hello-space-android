### hello-space-android
Example Android application using the Somewear SDKs.

# Somewear UI SDK
The Somewear UI SDK wraps the Somewear Core SDK with UI that is used in the  Somewear Android application. It currently provides permission handling, bluetooth prompts, firmware update dialogs, and firmware update notification handling. You can directly use the Somewear Core SDK instead if your application requirements are not satisfied.
 
## Setup
1. Add Somewear's Maven Repository to your root `build.gradle`.
```groovy
allprojects {
    repositories {
        google()
        jcenter()
        maven {
            credentials {
                username somewearArtifactsUsername
                password somewearArtifactsPassword
            }
            url "https://somewear-artifacts.appspot.com"
        }
    }
}
```

2. Declare the missing variables in your `gradle.properties` file. For quick testing, you can add your credentials directly here. To avoid checking in these credentials, you can also add them to a `gradle.properties` in your home gradle directory (`~/.gradle/gradle.properties` on \*nix).
```
somewearArtifactsUsername=
somewearArtifactsPassword=
```

3. Add the Somewear UI SDK dependency to your app module's `build.gradle`.
```groovy
dependencies {
    ...
    implementation ('com.somewearlabs:somewear-ui:x.x.x')
    ...
}
```

4. Initialize SomewearUI.
```java
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize SomewearUI
        SomewearProperties properties = new SomewearProperties(this);
        SomewearUI.setup(properties);
    }
}
```

### Usage

Scan and pair to a Somewear hotspot:
```java
public class MainActivity extends AppCompatActivity {

    private SomewearUI somewearUI = SomewearUI.getInstance();
  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Button scanButton = findViewById(R.id.scanButton);
        scanButton.setOnClickListener(v -> somewearUI.toggleScan(this));
    }
}
```

Handle firmware updates:
```java
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Display firmware update dialogs as needed.
        somewearUI.configureFirmwareUpdateHandling(this);
    }
}
```

Send a payload over satellite:
```java
private SomewearDevice device = SomewearDevice.getInstance();

private void sendMessage() {
    String message = "Hello from space!";
    byte[] data = message.getBytes(StandardCharsets.UTF_8);

    DevicePayload payload = DevicePayload.build(data);
    device.sendData(payload);
}
```

Receive payload status updates (for outbound payloads) and payloads over satellite (inbound payloads):
```java
private CompositeDisposable disposable = new CompositeDisposable();

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    disposable.addAll(
            // Observe payload changes
            device.getPayload()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::didReceivePayload)
    );
}

@Override
protected void onDestroy() {
    disposable.clear();
    super.onDestroy();
}

private void didReceivePayload(DevicePayload payload) {
    Log.d("MyActivity", "Did receive payload: id=" + payload.getParcelId() + "; status=" + payload.getStatus());
}
```

Update UI based off device state:
```java
@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Button sendButton = findViewById(R.id.sendButton);
    TextView qualityTextView = findViewById(R.id.qualityTextView);
    TextView batteryTextView = findViewById(R.id.batteryTextView);
    TextView connectionStateTextView = findViewById(R.id.connectionStateTextView);
    TextView activityTextView = findViewById(R.id.activityTextView);

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
                    })
    );
}
```

### Logging
The SDK uses SLF4J-Android for logging. To enable logging, add a SLF4J compatible logging framework, such as [Logback](https://github.com/tony19/logback-android): 
```groovy
dependencies {
    ...
    implementation 'com.github.tony19:logback-android:1.3.0-3'
    ...
}
```
Add to `src/main/assets/logback.xml`
```xml
<configuration>
    <appender name="logcat" class="ch.qos.logback.classic.android.LogcatAppender">
        <tagEncoder>
            <pattern>%logger{12}</pattern>
        </tagEncoder>
        <encoder>
            <pattern>[%-20thread] %msg</pattern>
        </encoder>
    </appender>

    <root level="DEBUG">
        <appender-ref ref="logcat" />
    </root>
</configuration>
```
