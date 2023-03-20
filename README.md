# Android SDK for Guru Example

This repository contains a simple example usage of the Guru Android SDK.

## Installation

Move the .aar to the `./guru-android-sdk` directory. Contact us to get the
latest .aar release.

## Usage

### With Analysis

First, create a `GuruVideo` with the activity and your API Key:

https://github.com/Formguru/guru-android-sdk-example/blob/d637294f25a54b7e4463989a521ed27125287146/app/src/main/java/com/example/guruandroidsdkexample/MainActivity.kt#L40-L45

Then initialize the camera - see [CameraSetupFragment.kt](https://github.com/Formguru/guru-android-sdk-example/blob/main/app/src/main/java/com/example/guruandroidsdkexample/CameraSetupFragment.kt) for an example using Android's CameraX library.

Finally, feed the camera frames into your `GuruVideo` instance and get back `InferenceResults`, which contain both the person `Keypoints` and an `Analysis` of the movement:

https://github.com/Formguru/guru-android-sdk-example/blob/d637294f25a54b7e4463989a521ed27125287146/app/src/main/java/com/example/guruandroidsdkexample/MainActivity.kt#L66-L68

### Keypoints-only

To run pose inference _without_ sending the keypoints to Guru for analysis you can use the `PoseTracker` class. Instantiate it with your API Key:

https://github.com/Formguru/guru-android-sdk-example/blob/d637294f25a54b7e4463989a521ed27125287146/app/src/main/java/com/example/guruandroidsdkexample/MainActivity.kt#L47-L50

Set up your camera, as before. Then consume the camera stream with your
`PoseTracker` instance, which returns `Keypoints`.

https://github.com/Formguru/guru-android-sdk-example/blob/d637294f25a54b7e4463989a521ed27125287146/app/src/main/java/com/example/guruandroidsdkexample/MainActivity.kt#L65-L71

## Local Development (for Guru developers)

If you are a Guru employee developing the SDK you can build and install in
one-shot with the following command (run the root of the SDK's source tree):

```bash
mkdir -p ../guru-android-sdk-example/guru-android-sdk
./gradlew assemble && cp ./guru-android-sdk/build/outputs/aar/guru-android-sdk-release.aar ../guru-android-sdk-example/guru-android-sdk/guru-android-sdk-release.aar
```
