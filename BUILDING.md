How to build World Clock & Weather
==================================

The app uses [Gradle](http://www.gradle.org/) as build system with the
[Android Plugin](https://developer.android.com/studio/releases/gradle-plugin.html).
It can either be built using Android Studio or Gradle on the command line.

Requirements
------------

*   Java 8 JDK (either Oracle or OpenJDK)
*   Android SDK

Using Android Studio
--------------------

Import the project as a Gradle Project into Android Studio. Building and
debugging on a device or emulator should work out of the box.

Using the Command Line
----------------------

To create a clean release build execute `./gradlew clean build`.
The APKs can be found in `worldclockwidget/build/outputs/apk`.

Other useful commands are:
*   `./gradlew tasks` - shows all available tasks
*   `./gradlew assembleDebug` - builds the debug APK
*   `./gradlew installDebug` - builds and installs the debug APK on a device or emulator
*   `./gradlew check` - runs all tests and code quality checks like Android lint etc. The output of the tools can be found in `worldclockwidget/build/output` and `worldclockwidget/build/reports`.

A note on signing release APKs
------------------------------

The build signs the release APK if the following Gradle properties are set:

*   `signingKeyStoreFile` - The path to the keystore with the signing key (either absolute or relative to the worldclockwidget subfolder)
*   `signingKeyStorePassword` - The password for the keystore
*   `signingKeyAlias` - The name of the signing key in the keystore
*   `signingKeyPassword` - The password for the signing key

Gradle properties can either be set on
*   the command line with the `-P` option, for example `-Pmyprop=myvalue`.
*   as an environment variable, for example: `export ORG_GRADLE_PROJECT_myprop=myvalue`
*   in a `gradle.properties` file next to the `build.gradle` file.
