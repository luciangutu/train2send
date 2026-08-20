# How to make a new release

1. Increase *versionCode* and *versionName* in `app/build.gradle.kts`.

2. Ensure your `local.properties` contains the keystore passwords:
```properties
keystore.password=your_password
key.password=your_password
```

3. Build the signed release artifacts:

### Option A: Android App Bundle (AAB) - Recommended for Google Play
```bash
./gradlew :app:bundleRelease
```
The signed AAB will be generated at: `app/build/outputs/bundle/release/app-release.aab`

### Option B: APK - For sideloading or GitHub Releases
```bash
./gradlew assembleRelease
```
The signed APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

4. (Optional) Verify the artifacts:

- **Verify AAB**:
  ```bash
  bundletool validate --bundle app/build/outputs/bundle/release/app-release.aab
  ```
- **Verify APK**:
  ```bash
  apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
  ```

5. Commit and push:
```bash
git add .
git commit -m "Release 1.x.y"
git push
git tag v1.x.y
git push origin v1.x.y
```

6. GitHub Release:
- **Tag**: v1.x.y
- **Title**: Train2Send 1.x.y
- **Attachments**: 
  - Rename `app-release.aab` to `train2send-1.x.y.aab`
  - Rename `app-release.apk` to `train2send-1.x.y.apk`
  - Upload both.
