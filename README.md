# Android SDK for Guru Example

This repository contains a simple example usage of the 
[Guru SDK for Android](https://github.com/Formguru/guru-android-sdk).

## Local Development

In order to try out a new local build of the SDK, run the following
from the `guru-android-sdk` directory:
```bash
mkdir -p ../guru-android-sdk-example/guru-android-sdk
./gradlew assemble && cp ./guru-android-sdk/build/outputs/aar/guru-android-sdk-release.aar ../guru-android-sdk-example/guru-android-sdk/guru-android-sdk-release.aar
```