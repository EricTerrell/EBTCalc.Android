# EBTCalc for Android

EBTCalc is a Reverse Polish Notation (RPN) calculator with custom buttons, programmed in Javascript, using a convenient editor. EBTCalc is open source.

# Copyright

EBTCalc for Android &#169; Copyright 2020, [`Eric Bergman-Terrell`](https://www.ericbt.com)

# Screenshots

![`EBTCalc Screenshot`](https://ericbt.com/artwork/EBTCalc/main_screen.png "EBTCalc Screenshot, Main Window")

![`EBTCalc Screenshot`](https://ericbt.com/artwork/EBTCalc/add_custom_button_1.png "EBTCalc Screenshot, Edit Window")

# Links

* [`website`](https://ericbt.com/ebtcalc)
* [`reference`](https://ericbt.com/ebtcalc/reference)
* [`Google Play`](https://play.google.com/store/apps/details?id=com.ericbt.rpncalcpaid)
* [`GitHub Repo (Android)`](https://github.com/EricTerrell/EBTCalc.Android)
* [`GitHub Repo (Desktop)`](https://github.com/EricTerrell/EBTCalc)

# Desktop Version

A version of [`EBTCalc`](https://github.com/EricTerrell/EBTCalc) for Windows and Linux is also available.

# How to Build

1.  Load in Android Studio
2.  Run!

Note: This app uses the [`Mozilla Rhino Javascript engine`](https://developer.mozilla.org/en-US/docs/Mozilla/Projects/Rhino).
The "app/libs/js.jar" file is the Rhino [`1.7.7`](https://github.com/mozilla/rhino/releases/download/Rhino1_7_7_RELEASE/rhino1.7.7.zip) Javascript engine.
You will probably not want to use a more recent version as later versions were not supported by Android.

# Rhino

This app uses the [`Rhino`](https://github.com/mozilla/rhino) Javascript engine. To update, copy the latest .jar to
app\libs. Then update the dependency (File / Project Structure / Dependencies).

# Javascript Compatibility

See [`this page`](https://mozilla.github.io/rhino/compat/engines.html) to determine the level of ES2015 support.

# License

[`GPL3`](https://www.gnu.org/licenses/gpl-3.0.en.html)

# Feedback

Please submit your feedback to EBTCalc@EricBT.com.