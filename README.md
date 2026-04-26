# Open WebUI Bridge Sample

This sample Android app demonstrates a bridge between Meta Wearables DAT camera access and an
Open WebUI server. It streams a frame from Meta glasses, JPEG-encodes the current preview frame,
uploads snapshots through Open WebUI's files API, and sends them to Open WebUI's chat-completions
API with a user prompt.

## What it demonstrates

- DAT app registration and camera permission flow
- Glasses camera streaming through `mwdat-camera`
- MockDeviceKit testing support inherited from the CameraAccess sample
- A minimal Open WebUI API client using chat sessions and `POST /api/v1/chat/completions`
- Vision-style chat-completions payloads with a base64 JPEG data URL and Open WebUI file metadata
- A configurable system prompt that tells the model it is responding through Meta glasses
- Copy, share, and Android text-to-speech playback for assistant responses

## Prerequisites

- Android Studio
- JDK 11 or newer
- Android SDK 31+
- GitHub personal access token with `read:packages` for DAT SDK dependencies
- Open WebUI reachable from the Android device or emulator
- An Open WebUI API key from Settings > Account
- A vision-capable model configured in Open WebUI

## Building

1. Open this repository in Android Studio.
1. Add `github_token=<your token>` to `local.properties` at the repository root, or set
   `GITHUB_TOKEN` in your environment.
1. Sync Gradle and run the `app` configuration.

### Local debug build from PowerShell

From Windows PowerShell, use Android Studio's bundled JBR and Gradle wrapper:

```powershell
Push-Location "<path-to-repo>\openwebui-meta-wearables-companion"; $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; $env:Path = "$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat assembleDebug; Pop-Location
```

On macOS or Linux (bash/zsh), using the JBR bundled with Android Studio:

```bash
# macOS default Android Studio install
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"
# Linux default Android Studio install
# export JAVA_HOME="$HOME/android-studio/jbr"
export PATH="$JAVA_HOME/bin:$PATH"
(cd <path-to-repo>/openwebui-meta-wearables-companion && ./gradlew assembleDebug)
```

The debug APK is created under `app/build/outputs/apk/debug/`.

## Running

1. Enable Developer Mode for your glasses in the Meta AI app.
1. Launch the sample and connect through the Meta AI registration flow.
1. The bridge starts automatically after an active device appears. Use **Stop bridge** to return to
  device selection when needed.
1. Enter:
  - Open WebUI API endpoint, for example for an emulator talking to the host machine
   - API key
  - model id as it appears in Open WebUI, or tap **Load models** and choose one from the list
  - system prompt
  - prompt
1. Tap **Start camera** when you want the camera stream active.
1. Tap **Snapshot ask** or **Voice ask** to capture a frame and send it to Open WebUI.
1. Use **Copy**, **Share**, or **Speak** on the response when it returns.

The app allows cleartext HTTP because local Open WebUI development commonly uses `http://` LAN or
emulator URLs. Use HTTPS and stricter network security settings for a production app.

### Install on an Android phone in developer mode

1. On the phone, enable **Developer options** and **USB debugging**.
1. Connect the phone by USB and accept the debugging prompt on the device.
1. Install the debug build with:

```powershell
Push-Location "<path-to-repo>\openwebui-meta-wearables-companion"; $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"; $env:Path = "$env:JAVA_HOME\bin;$env:Path"; .\gradlew.bat installDebug; Pop-Location
```

Or on macOS / Linux:

```bash
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home" # macOS
# export JAVA_HOME="$HOME/android-studio/jbr" # Linux
export PATH="$JAVA_HOME/bin:$PATH"
(cd <path-to-repo>/openwebui-meta-wearables-companion && ./gradlew installDebug)
```

If multiple Android devices or emulators are connected, select a target from Android Studio or set
`ANDROID_SERIAL` before running `installDebug`.

## Notes

- The sample stores the Open WebUI endpoint, API key, selected model, system prompt, and prompt in
  app-private local preferences so they are available the next time you open the bridge.
- The app follows the Android system light/dark setting by default. Use **Theme** in settings to
  override it to light or dark mode.
- Each ask is attached to an Open WebUI chat session so follow-up questions can use the prior
  conversation. Use **New chat** in settings to start a fresh server-side conversation.
- Snapshot asks upload the JPEG to `/api/v1/files/` and attach the returned file metadata to the
  user message so the image appears in Open WebUI chat history.
- The selected Open WebUI model must support image input. Text-only models will not analyze the
  frame correctly.
- Response playback uses Android `TextToSpeech` routed toward the current Bluetooth communication
  device when available. The public DAT SDK does not expose Meta AI's built-in voice TTS as an app
  API.
- This app is a bridge sample; it does not intercept the built-in Meta AI assistant experience on
  the glasses.

## Resource asset map

- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`: Adaptive launcher icon definition used on
  Android 8.0+ devices. This is the resource Android 12+ also uses for the default system splash
  icon because the app does not define a separate `windowSplashScreenAnimatedIcon`.
- `app/src/main/res/drawable/ic_launcher_background.xml`: Background layer for the adaptive launcher
  icon.
- `app/src/main/res/drawable/ic_launcher_art_foreground.xml`: Current foreground wrapper for the
  adaptive launcher icon. It insets the actual artwork so the launcher and Android 12+ splash mask
  do not crop it too aggressively.
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher_art.jpg`: Duplicated launcher artwork used by the
  adaptive icon foreground wrapper.
- `app/src/main/res/drawable/ic_launcher_foreground.png`: Older launcher foreground artwork. It is
  currently not referenced by `ic_launcher.xml`.
- `app/src/main/res/mipmap-xxxhdpi/ic_launcher.jpg`: Legacy bitmap launcher icon fallback for
  non-adaptive icon resolution paths. Updating this affects older launcher/icon fallbacks, but it
  does not control the Android 12+ splash icon on current devices.
- `app/src/main/res/drawable/camera_access_icon.xml`: Hero icon shown on `HomeScreen` and
  `NonStreamScreen`.
- `app/src/main/res/drawable/hourglass_icon.xml`: Waiting-state icon shown while the app is waiting
  for an active device in `NonStreamScreen`.
- `app/src/main/res/drawable/video_icon.xml`, `tap_icon.xml`, `smart_glasses_icon.xml`: Tip icons
  shown in `NonStreamScreen`.
- `app/src/main/res/drawable/smart_glasses_icon.xml`, `sound_icon.xml`, `walking_icon.xml`: Tip
  icons shown in `HomeScreen`.
- `app/src/main/res/drawable/camera_icon.xml`, `timer_icon.xml`: Present in `res/drawable` but not
  currently referenced in the app code.

## License

This repository's original source code is licensed under the Apache License 2.0.

This project integrates with the Meta Wearables Device Access Toolkit. Use of the Wearables Device
Access Toolkit is subject to the Meta Wearables Developer Terms and Acceptable Use Policy:

- https://wearables.developer.meta.com/terms
- https://wearables.developer.meta.com/acceptable-use-policy

This repository does not relicense Meta's SDKs, documentation, services, trademarks, or other Meta
materials.
