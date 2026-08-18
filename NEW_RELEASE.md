1. Increase *versionCode* and *versionName* in `app/build.gradle.kts`

2. Build release
```
./gradlew assembleRelease
```

this will spit `app/build/outputs/apk/release/app-release-unsigned.apk`


3. Sign APK

```
apksigner sign \
  --ks train4send-release.jks \
  --out train4send-1.1.1.apk \
  app/build/outputs/apk/release/app-release-unsigned.apk
```

4. Verify
```
apksigner verify --verbose train4send-1.1.1.apk
```