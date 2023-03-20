# Android SDK for Guru Example

This repository contains a simple example usage of the Guru Android SDK.

## Installation

Move the .aar to the `./guru-android-sdk` directory. Contact us to get the
latest .aar release.

## Local Development (for Guru developers)

If you are a Guru employee developing the SDK you can build and install in
one-shot with the following command (run the root of the SDK's source tree):

```bash
./gradlew assemble && cp ./guru-android-sdk/build/outputs/aar/guru-android-sdk-release.aar ../guru-android-sdk-example/guru-android-sdk/guru-android-sdk-release.aar
```
