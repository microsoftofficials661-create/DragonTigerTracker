# Dragon Tiger Tracker (Android starter)

A simple offline Android app for manually recording colour combinations and actual game results.

## Included
- First and second colour selection (Red/Black)
- Rule-based suggestion using the rules from your note:
  - Red + Red -> Dragon
  - Red + Black -> Dragon
  - Black + Red -> Dragon
  - Black + Black -> Tiger
- Separate optional actual result entry (Dragon/Tiger)
- Save, view, and delete history
- Basic historical statistics
- Local persistence using Android SharedPreferences

## Important
The suggestion is only a configured rule, not a reliable prediction of a game's outcome. This starter intentionally does not include betting, stake escalation, or money-backup progression features.

## Open and run
1. Install Android Studio.
2. Extract this ZIP.
3. In Android Studio, choose **Open** and select the `DragonTigerTracker` folder.
4. Allow Gradle sync to finish.
5. Run the `app` configuration on an Android device or emulator.

Requires JDK 17 and an Android SDK suitable for compileSdk 35.
