<div align="center">

<h1>
  <img src="assets/flow-logo.svg" width="36" alt="">
  Flow
</h1>

Notes & Tasks app for Android. Fully native. Just your stuff, no bullshit

[**Features**](#features) · [**Usage**](https://github.com/jvqtil/Flow/wiki/Usage) · [**Screenshots**](#screenshots) · [**Contributing**](#contributing)

<a href="https://github.com/jvqtil/Flow/releases/latest">
  <img src="https://img.shields.io/badge/Download%20APK-8B5CF6?style=for-the-badge&logo=android&logoColor=white" alt="Download APK">
</a>

</div>

## Features

- Minimal editor
- Local-only storage
- Folders for organizing entries [1]
- Moving entries between folders
- Swipe gestures
- Drag-and-drop reordering
- Undo after deleting entries, attachments and folders
- File attachments
- Smooth, yet fast animations
- Material UI
- Dynamic colors on Android 12+
- AMOLED background option
- Predictive back gesture support (Android 16+)
- Customizable UI and editor fonts
- Configurable keyboard behavior
- Export and import

[1] Turned off by default. Enable it in Settings.

## Screenshots

### Dark theme

| Home                                    | Editing                                       | Settings                                        |
|-----------------------------------------|-----------------------------------------------|-------------------------------------------------|
| ![Home](screenshots/Flow_Home_Dark.png) | ![Editing](screenshots/Flow_Editing_Dark.png) | ![Settings](screenshots/Flow_Settings_Dark.png) |

<details>
<summary><h3>Light theme</h3></summary>

| Home                                     | Editing                                        | Settings                                         |
|------------------------------------------|------------------------------------------------|--------------------------------------------------|
| ![Home](screenshots/Flow_Home_Light.png) | ![Editing](screenshots/Flow_Editing_Light.png) | ![Settings](screenshots/Flow_Settings_Light.png) |

</details>

## Contributing

### Building
Clone the repository and open it in Android Studio.

Build it with:
  ```bash
  ./gradlew assembleRelease
  ```

Found a bug, have an idea, or want to improve something?
Issues and pull requests are welcome.
