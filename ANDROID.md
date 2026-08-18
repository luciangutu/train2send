# Installing Train4Send on Samsung Galaxy S23

Step-by-step guide to compile and install the app directly on your physical device.

---

## Prerequisites on Ubuntu

Make sure you have these installed (see UBUNTU.md for details):

- JDK 17
- Android SDK (Platform 35, Build-Tools, Platform-Tools)
- USB cable (USB-C to USB-C or USB-C to USB-A)

Set environment variables in `~/.bashrc`:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
```

---

## Prepare Your Samsung S23

### 1. Enable Developer Options

1. Open **Settings**
2. Scroll to **About phone**
3. Tap **Software information**
4. Tap **Build number** 7 times rapidly
5. You'll see "Developer mode has been enabled"

### 2. Enable USB Debugging

1. Go back to **Settings → Developer options**
2. Toggle on **USB debugging**
3. (Recommended) Toggle on **Stay awake** — keeps screen on while charging

### 3. Set USB Mode to File Transfer

When you plug in the USB cable, pull down the notification shade and tap the USB notification. Select **File transfer / Android Auto** (not just charging).

---

## Connect Phone to Ubuntu

### 4. Plug In and Authorize

```bash
# Plug in your S23 via USB, then check connection:
adb devices
```

On your phone, you'll see a popup: **"Allow USB debugging?"** — tap **Allow** (check "Always allow from this computer" for convenience).

Run `adb devices` again — you should see:

```
List of devices attached
XXXXXXXXXXXXXXX    device
```

If you see "unauthorized", re-check the popup on your phone.

### 5. Fix USB Permissions (if needed)

If `adb devices` shows nothing or permission errors:

```bash
# Add udev rule for Samsung
sudo tee /etc/udev/rules.d/51-android.rules << 'EOF'
SUBSYSTEM=="usb", ATTR{idVendor}=="04e8", MODE="0666", GROUP="plugdev"
EOF

sudo udevadm control --reload-rules
sudo udevadm trigger

# Restart ADB
adb kill-server
adb start-server
adb devices
```

Samsung's USB vendor ID is `04e8`.

---

## Build and Install

### 6. Build the Debug APK

```bash
cd ~/Dropbox/Other/train4send
./gradlew assembleDebug
```

The APK is generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

### 7. Install on Phone

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

Or build + install in one step:

```bash
./gradlew installDebug
```

### 8. Launch the App

```bash
adb shell am start -n com.train4send/.MainActivity
```

Or just find **Train4Send** in your app drawer.

---

## Build a Release APK (Signed)

For a release build you can install without Android Studio's debug certificate:

### 9. Generate a Keystore (One Time)

```bash
keytool -genkey -v -keystore train4send-release.jks \
  -keyalg RSA -keysize 2048 -validity 10000 \
  -alias train4send
```

You'll be prompted for a password and certificate details.

### 10. Configure Signing in Gradle

Add to `app/build.gradle.kts` inside the `android` block:

```kotlin
signingConfigs {
    create("release") {
        storeFile = file("../train4send-release.jks")
        storePassword = "your_password"
        keyAlias = "train4send"
        keyPassword = "your_password"
    }
}

buildTypes {
    release {
        signingConfig = signingConfigs.getByName("release")
        isMinifyEnabled = true
        proguardFiles(
            getDefaultProguardFile("proguard-android-optimize.txt"),
            "proguard-rules.pro"
        )
    }
}
```

### 11. Build and Install Release

```bash
./gradlew assembleRelease
adb install app/build/outputs/apk/release/app-release.apk
```

---

## Wireless Debugging (Optional — No Cable)

Once you've connected via USB at least once:

### On the phone:

1. **Settings → Developer options → Wireless debugging** → toggle ON
2. Tap **Wireless debugging** to enter its settings
3. Tap **Pair device with pairing code** — note the IP:port and pairing code

### On Ubuntu:

```bash
# Pair (one time)
adb pair 192.168.x.x:XXXXX
# Enter the pairing code when prompted

# Connect
adb connect 192.168.x.x:XXXXX

# Verify
adb devices
# Should show your device

# Now you can unplug USB and deploy wirelessly
./gradlew installDebug
```

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `adb devices` shows empty | Check USB cable supports data (not charge-only), set USB mode to File Transfer |
| "unauthorized" | Tap Allow on the phone popup, or revoke + re-authorize in Developer Options |
| Install fails "INSTALL_FAILED_UPDATE_INCOMPATIBLE" | Uninstall old version first: `adb uninstall com.train4send` |
| App crashes on launch | Check logs: `adb logcat -s "AndroidRuntime"` |
| Build fails with "SDK not found" | Create `local.properties`: `sdk.dir=/home/YOUR_USER/Android/Sdk` |
| Slow build | First build downloads dependencies. Subsequent builds are faster. Add `org.gradle.parallel=true` to `gradle.properties` |

---

## Quick Reference

```bash
# Full workflow from scratch:
cd ~/Dropbox/Other/train4send
adb devices                          # verify phone connected
./gradlew installDebug               # build + install
adb shell am start -n com.train4send/.MainActivity  # launch

# View logs:
adb logcat --pid=$(adb shell pidof com.train4send)

# Uninstall:
adb uninstall com.train4send
```
