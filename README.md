# Pose Estimation For Mobile implemented in Java using TF Lite

## Description
A replica of PoseEstimation project on github which implemented in Kotlin
(https://github.com/edvardHua/PoseEstimationForMobile)



## Building in Android Studio with TensorFlow Lite AAR from JCenter.
The build.gradle is configured to use TensorFlow Lite's nightly build.

If you see a build error related to compatibility with Tensorflow Lite's Java API (example: method X is
undefined for type Interpreter), there has likely been a backwards compatible
change to the API. You will need to pull new app code that's compatible with the
nightly build and may need to first wait a few days for our external and internal
code to merge.
