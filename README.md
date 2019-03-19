### hello-space-android
Example Android application using the Somewear SDKs.

# Somewear UI SDK
The Somewear UI SDK wraps the Somewear Core SDK with UI that is used in the  Somewear Android application. It currently provides device scanning, device info, permission handling, bluetooth prompts, firmware update dialogs, and firmware update notification handling. You can directly use the Somewear Core SDK instead if your application requirements are not satisfied.
 
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
        SomewearUIProperties properties = new SomewearUIProperties(this);
        SomewearUI.setup(properties);
    }
}
```

### Usage

Add the status bar view to your layout so the user scan for a Somewear device and see info such as current battery level.
```java
<com.somewearlabs.uisupport.api.SomewearStatusBarView
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
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

Send an SMS message over satellite:
```java
private SomewearDevice device = SomewearDevice.getInstance();

private void sendSmsMessage() {
    String message = "Hello from space!";
    MessagePayload payload = MessagePayload.build(message, PhoneNumber.build("916-555-1111"));
    device.sendData(payload);
}
```

Send a byte payload over satellite:
```java
private SomewearDevice device = SomewearDevice.getInstance();

private void sendBytes() {
    String message = "Hello from space!";
    byte[] data = message.getBytes(StandardCharsets.UTF_8);
    
    DataPayload payload = DataPayload.build(data);
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
    if (payload instanceof MessagePayload) {
        MessagePayload messagePayload = (MessagePayload) payload;
        Log.d("MyActivity", "Did receive message: content=" + messagePayload.getContent() + "; id=" + payload.getParcelId() + "; status=" + payload.getStatus());
    }
    else if (payload instanceof DataPayload) {
        DataPayload dataPayload = (DataPayload) payload;
        String base64Data = Base64.encodeToString(dataPayload.getData(), Base64.DEFAULT);
        Log.d("MyActivity", "Did receive data: content=" + base64Data + "; id=" + payload.getParcelId() + "; status=" + payload.getStatus());
    }
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
