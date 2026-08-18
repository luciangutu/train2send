# Running Train4Send on Ubuntu (Emulator)

Step-by-step guide to build and run the app on an Android emulator on Ubuntu.

---

## Prerequisites

### 1. Install JDK 17

```bash
sudo apt update
sudo apt install openjdk-17-jdk
```

Verify:

```bash
java -version
# openjdk version "17.x.x"
```

### 2. Install Android Studio

Download from https://developer.android.com/studio

```bash
# Extract to /opt
sudo tar -xzf android-studio-*.tar.gz -C /opt/

# Launch
/opt/android-studio/bin/studio.sh
```

Or install via snap:

```bash
sudo snap install android-studio --classic
```

### 3. Install Required SDK Components

On first launch, Android Studio will prompt you to install SDK components. Make sure you have:

- **Android SDK Platform 35** (API 35)
- **Android SDK Build-Tools 35.0.0**
- **Android Emulator**
- **Android SDK Platform-Tools**

You can also install via command line:

```bash
# Set ANDROID_HOME (add to ~/.bashrc)
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/emulator
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

# Install components
sdkmanager "platforms;android-35" "build-tools;35.0.0" "emulator" "platform-tools"
```

### 4. Verify KVM (Hardware Acceleration)

The emulator needs KVM for acceptable performance:

```bash
# Verify KVM is available
kvm-ok
# INFO: /dev/kvm exists
# KVM acceleration can be used
```

If `kvm-ok` reports it's not available, install it:

```bash
sudo apt install qemu-kvm cpu-checker
# Then check /dev/kvm permissions — your user needs read/write access:
ls -la /dev/kvm
# If permission denied, add yourself to the kvm group:
sudo usermod -aG kvm $USER
# Log out and back in
```

---

## Setup

### 5. Clone / Open the Project

```bash
cd ~/Dropbox/Other/train4send
```

Open in Android Studio: **File → Open → select the train4send folder**

Wait for Gradle sync to complete (first time takes a few minutes to download dependencies).

### 6. Create an Emulator (AVD)

1. In Android Studio: **Tools → Device Manager → Create Virtual Device**
2. Pick a device (e.g., **Pixel 7**)
3. Select system image: **API 35 (VanillaIceCream)** — download if needed
4. Finish

Or via command line:

```bash
# List available system images
sdkmanager --list | grep "system-images;android-35"

# Download a system image
sdkmanager "system-images;android-35;google_apis;x86_64"

# Create AVD
avdmanager create avd -n train4send_device -k "system-images;android-35;google_apis;x86_64" -d pixel_7
```

---

## Run

### 7. Launch the Emulator

From Android Studio: click the green ▶ Run button with your AVD selected.

Or from terminal:

```bash
emulator -avd train4send_device
```

### 8. Build and Install

From Android Studio: **Run → Run 'app'** (Shift+F10)

Or from terminal:

```bash
cd ~/Dropbox/Other/train4send
./gradlew installDebug
```

The app will launch automatically on the emulator.

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| Gradle sync fails | Check JDK 17 is set: File → Settings → Build → Gradle → Gradle JDK |
| Emulator won't start | Ensure KVM is enabled: `ls -la /dev/kvm` |
| Emulator is very slow | Ensure you're using x86_64 image + KVM, not ARM |
| "SDK location not found" | Create `local.properties` with `sdk.dir=/home/YOUR_USER/Android/Sdk` |
| Port already in use | Kill stale ADB: `adb kill-server && adb start-server` |

---

## Run Without Android Studio (Terminal Only)

If you prefer not using the IDE:

```bash
# 1. Set environment
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/emulator:$ANDROID_HOME/platform-tools

# 2. Start emulator in background
emulator -avd train4send_device &

# 3. Wait for device to boot
adb wait-for-device

# 4. Build and install
cd ~/Dropbox/Other/train4send
./gradlew installDebug

# 5. Launch the app
adb shell am start -n com.train4send/.MainActivity
```


### remove any lingering Gradle daemons
```
pkill -f "GradleDaemon" 2>/dev/null; pkill -f "gradle" 2>/dev/null; sleep 2; echo "done"
```