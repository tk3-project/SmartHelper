# SmartHelper

## ChangeLog
* Add `enableLocationAndActivityTracking` and `disableLocationAndActivityTracking` methods in SettingFragment


* Add new Services
* Rename some files
* Change the structure of the project

## Structure
```
|-- app
    |-- manifests
        |-- AndroidManifest.xml
    |-- java
        |-- Services
            |-- DetectedActvitiesIntentService.java
            |-- DetectedActvitiesService.java
            |-- DetectedLocationService.java
        |-- ui.main
            |-- SettingFragment.java
            |-- DisplayFragment.java
            |-- SectionsPagerAdapter.java
        |-- MainActivity.java
        |-- Scenarios.java
        |-- Constants.java
    |-- res
        |-- drawable
            |-- ic_lancher_background_xml
            |-- selected.xml
        |-- layout
            |-- activity_main.xml
            |-- display_fragment.xml
            |-- setting_fragment.xml
        |-- values
            |-- colors.xml
            |-- dimens.xml
            |-- strings.xml
            |-- styles.xml
|-- Gradle Scripts
    |-- build.gradle (:app)
```                       
