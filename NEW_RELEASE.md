# How to make a new release

1. Increase *versionCode* and *versionName* in `app/build.gradle.kts`.

2. Ensure your `local.properties` contains the keystore passwords:
```properties
keystore.password=your_password
key.password=your_password
```

3. Build the signed release Android App Bundle (AAB):
```bash
./gradlew :app:bundleRelease
```
The signed AAB will be generated at: `app/build/outputs/bundle/release/app-release.aab`

4. (Optional) Verify the AAB using bundletool:
```bash
bundletool validate --bundle app/build/outputs/bundle/release/app-release.aab
```

5. Commit and push:
- Increase the version, tag it, and push to GitHub.

6. GitHub Release:
- **Tag**: v1.x.y
- **Title**: Train2Send 1.x.y
- **Attachment**: Rename `app-release.aab` to `train2send-1.x.y.aab` and upload.
