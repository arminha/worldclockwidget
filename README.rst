World Clock & Weather
=====================

A simple application to display the local time and current weather conditions in places all over the world.
It comes with two home screen widgets which show weather and time or time only.

.. image:: https://travis-ci.org/arminha/worldclockwidget.svg?branch=master
    :target: https://travis-ci.org/arminha/worldclockwidget

Features
--------

* Show local time and weather conditions in more than 4000 places.
* Add your own places: Add a place in the same time zone and change its name and coordinates to your desired location to show the actual weather condition there!
* A compact clock widget, displaying the local time in your favorite locations.
* A widget showing the time and weather, which is re-sizable to show multiple locations on the home screen.
* Simple and clean UI
* Customizable widget colors (including transparent background)

The Internet connectivity permission is **only** used to retrieve current weather conditions.

Download
--------

The latest version of the app is available on `Google Play <https://play.google.com/store/apps/details?id=ch.corten.aha.worldclock>`_
and on `F-Droid <https://f-droid.org/repository/browse/?fdid=ch.corten.aha.worldclock>`_.

Screenshots
-----------

`wiki/Screenshots <https://github.com/arminha/worldclockwidget/wiki/Screenshots>`_

Building
--------

We use `Gradle <http://www.gradle.org/>`__.
Run ``./gradlew assembleDebug`` to create a debug build or ``./gradlew assembleRelease`` to create a release build.

OpenWeatherMap API key
######################

Using the OpenWeatherMap API requires an API key. A default key is stored in the file ``./worldclockwidget/default_owm_api_key``. It can be overwritten by setting the Gradle project property ``owmApiKey``.

Libraries
---------

The app uses and includes the following libraries:

* `ActionBarSherlock <http://actionbarsherlock.com/>`_ (also on `GitHub <https://github.com/JakeWharton/ActionBarSherlock>`__)
* `ColorPickerPreference <https://github.com/attenzione/android-ColorPickerPreference>`_
* `google-gson <https://code.google.com/p/google-gson/>`_
* `joda-time-android <https://github.com/dlew/joda-time-android>`_

Acknowledgements
----------------

* Special thanks to `vyick <http://vyick.wordpress.com/>`_ for beta testing and feature suggestions!
* Weather data is provided by `Open Weather Map <http://openweathermap.org/>`_.
* The weather icons are based on `Meteocons <http://www.alessioatzeni.com/meteocons/>`_ from Alessio Atzeni.
* Time zone and geographical data is provided by `GeoNames <http://www.geonames.org/>`_.

