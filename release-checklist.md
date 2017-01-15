Release Checklist
-----------------

1.  Update change list in `CHANGES.rst`
2.  Update *versionCode* and *versionName* in `worldclockwidget/build.gradle`
3.  Commit changes
4.  Create a signed release tag:
    `git tag -s -m "Release x.y.z" x.y.z`
5.  Build a release APK: `./gradlew clean build`
6.  Upload the release APK found in `worldclockwidget/build/outputs/apk` to Google Play
