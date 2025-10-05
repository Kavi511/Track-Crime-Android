 ## 🚔📱 CriminalIntent (Android)

Overview 🧭

CriminalIntent is an Android app that lets you track crime reports and attach photos captured from the device camera. It demonstrates modern Android app patterns, secure file sharing via FileProvider, and runtime-friendly image scaling for smooth UI.

Features ✨

- Create, edit, and delete crimes 📝
- Pick a suspect from Contacts and dial a sample number 👤☎️
- Generate and share a crime report 📨
- Take Photo button to open the camera 📸
- Save full-resolution photos securely via FileProvider 🔐
- Show scaled thumbnail in the detail screen 🖼️
- Tap thumbnail to view the full-size photo using an implicit viewer intent 🔎

Requirements 🔧

- Android Studio Ladybug or newer 🐞
- Gradle 8+ 🧱
- Android SDK 24+ (tested on 24–34) 📦

Project Structure 🗂️

- app/src/main/java/com/example/criminalintent
  - Crime.java: model with unique photo filename per crime 🆔
  - CrimeLab.java: data access and helper methods (incl. getPhotoFile) 🧪
  - CrimeFragment.java: UI logic; camera intent, thumbnail, full-size viewer 🧩
  - CrimeActivity/CrimePagerActivity/CrimeListActivity: navigation scaffolding 🧭
  - PictureUtils.java: bitmap scaling helpers 🖼️
- app/src/main/res/layout/fragment_crime.xml: detail UI with ImageView + Take Photo button 🖼️📸
- app/src/main/AndroidManifest.xml: FileProvider + permissions 📜
- app/src/main/res/xml/files.xml: FileProvider paths 🗺️

Setup 🚀

1) Open the project folder in Android Studio. 🧰
2) Allow Gradle sync to complete. 🔄
3) Connect a device (recommended) or start an emulator with a camera. 📱🧪

Build & Run ▶️

- Android Studio: Run ▶ on the `app` module (Debug).
- Command line (Windows PowerShell at repo root):

```bash
./gradlew assembleDebug
```

Camera & Photo Flow (How it works) 📷

- The Take Photo button triggers a camera intent to capture a photo. 🎯
- Photos are stored in the app’s private files directory using a FileProvider URI. 🔐
- A scaled bitmap thumbnail is displayed in the `ImageView` using `PictureUtils`. 🖼️
- Tapping the thumbnail opens the full-size photo using an implicit viewer intent. 🔍

FileProvider Configuration 🗃️

- Manifest authority: `com.example.criminalintent.fileprovider`
- Paths file: `res/xml/files.xml`

Permissions & Features (Manifest) 🔒

- Permissions
  - `android.permission.CAMERA` 📷
  - `android.permission.READ_CONTACTS` 📇
  - Optional: `READ/WRITE_EXTERNAL_STORAGE` (legacy) 💾
- Features (not required to install on devices without camera)
  - `android.hardware.camera` 📷
  - `android.hardware.camera.autofocus` 🎯
  - `android.hardware.camera.flash` ⚡

Troubleshooting 🛠️

- Camera doesn’t open when tapping Take Photo
  - Ensure a camera app exists on the device/emulator. Some emulators ship without one; use a physical device or install a camera app. 📱
  - Verify the Manifest contains the Camera permission and FileProvider entry. 📜
  - Confirm `files.xml` exists at `res/xml/files.xml` with `<files-path .../>`. 🗂️
  - If a third-party camera blocks returning results, the app will still open a camera via fallbacks. 🔁

- Thumbnail not showing
  - Confirm a photo file exists for the current crime (unique filename is set in `Crime`). 🖼️
  - Ensure the app has permission to read the stored file (FileProvider handles this for viewer intents). 🔐

- Full-size photo won’t open
  - Check that there is at least one image viewer app installed on the device. 🔎

Notes 📝

- The main, login, and register screens are intended to use a dark theme. 🌙

License 📄

This project is provided for educational purposes.





