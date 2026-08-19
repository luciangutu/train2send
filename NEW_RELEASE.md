# How to make a new release

1. Increase *versionCode* and *versionName* in `app/build.gradle.kts`.

2. Ensure your `local.properties` contains the keystore passwords:
```properties
keystore.password=your_password
key.password=your_password
```

3. Build the signed release APK:
```bash
./gradlew assembleRelease
```
The signed APK will be generated at: `app/build/outputs/apk/release/app-release.apk`

4. (Optional) Verify the signature:
```bash
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

5. Commit and push:
```bash
git add .
git commit -m "Release 1.2.1"
git push
git tag v1.2.1
git push origin v1.2.1
```

6. GitHub Release:
- **Tag**: v1.2.1
- **Title**: Train2Send 1.2.1
- **Attachment**: Rename `app-release.apk` to `train2send-1.2.1.apk` and upload.
