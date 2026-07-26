# Tabi AI

A modern, production-ready AI voice assistant for Android, built in Java with a clean MVVM
architecture. Tabi listens, talks back, remembers your conversations, and can control your
phone — opening apps, searching Google/YouTube, toggling the flashlight, launching the camera,
and reporting the time, date, and weather — all on top of a beautiful custom Material 3 UI with
full dark mode support.

## Features

- 🎙️ **Voice Recognition** — on-device speech-to-text via Android's `SpeechRecognizer`
- 🔊 **Text to Speech** — Tabi speaks every reply aloud
- 🤖 **OpenAI (ChatGPT) integration** — general conversation is answered by the Chat Completions API
- 🔁 **Continuous voice conversation** — after Tabi finishes speaking, the mic automatically re-opens
- 🎨 **Beautiful Material 3 UI** — custom gradient header, chat bubbles, animated mic button
- 🌗 **Dark Mode** — full day/night theme, togglable in Settings
- 📱 **Open installed apps** by voice ("open Spotify")
- 🔍 **Google Search** by voice ("search for the weather in Tokyo")
- ▶️ **YouTube Search** by voice ("play lofi beats on youtube")
- 📷 **Camera** launch by voice ("open camera")
- 🔦 **Flashlight** toggle by voice ("turn on the flashlight")
- 🕒 **Time & Date** ("what's the time", "what's today's date")
- ☀️ **Weather** via OpenWeatherMap, using the device's last known location
- 💾 **Chat history** persisted locally with **Room**
- 📜 **RecyclerView** chat UI with DiffUtil for efficient updates
- 🏗️ **MVVM** architecture (View → ViewModel → Repository → Room/Retrofit)
- 🌐 **Retrofit + OkHttp + Gson** for all networking
- 🔐 **Runtime permissions** handled gracefully (mic, camera, location)

## Project structure

```
app/src/main/java/com/tabi/ai/
├── MainActivity.java              # Main conversation screen
├── SettingsActivity.java          # API keys + theme settings
├── TabiApplication.java           # App entry point, DB + theme init
├── assistant/
│   └── CommandProcessor.java      # Routes input to local device actions vs. OpenAI
├── data/
│   ├── local/                     # Room: entity, DAO, database
│   ├── remote/                    # Retrofit: OpenAI + OpenWeatherMap services & models
│   └── repository/                # ChatRepository, WeatherRepository
├── ui/
│   ├── adapter/ChatAdapter.java   # RecyclerView adapter (user/AI bubbles)
│   └── main/MainViewModel.java    # MVVM ViewModel
└── utils/                         # Speech, TTS, app launcher, flashlight, permissions, etc.
```

## Setup

1. **Open in Android Studio** (Hedgehog or newer recommended) as an existing project.
2. **Add your API keys.** Tabi will work without hard-coding keys — you can enter them at
   runtime in the in-app **Settings** screen (gear icon, top-right). Alternatively, bake them
   into the build by adding to your **project-level** `gradle.properties` (not committed to
   version control):

   ```properties
   OPENAI_API_KEY=sk-your-key-here
   OPENWEATHER_API_KEY=your-openweathermap-key-here
   ```

   - Get an OpenAI key at https://platform.openai.com/api-keys
   - Get a free OpenWeatherMap key at https://openweathermap.org/api

3. **Sync Gradle** and run on a device or emulator (API 24+). A real device is strongly
   recommended for testing microphone/speech features.
4. On first launch, grant the microphone, camera, and location permissions when prompted.

## How it works

Every message you speak or type first passes through `CommandProcessor`, which pattern-matches
simple device commands (time, date, weather, flashlight, camera, open app, Google/YouTube search).
If nothing matches, the message is forwarded to `ChatRepository`, which calls the OpenAI Chat
Completions API (model configurable in `Constants.OPENAI_CHAT_MODEL`) with the last 10 messages
of context plus a system prompt tuned for spoken responses. Every exchange — from either path —
is saved to a local Room database and rendered live in the RecyclerView via LiveData.

Tap the mic once to start a single voice turn, or let Tabi keep listening automatically after
each reply for a hands-free, continuous conversation. Tap the mic again (or say nothing) to stop.

## Building an APK

**Easiest path — Android Studio:**
1. Open the `TabiAI` folder as a project in Android Studio (Hedgehog/2023.1+ or newer).
2. Let it sync Gradle (Studio will offer to generate the Gradle wrapper jar automatically if
   asked — accept it).
3. `Build > Build Bundle(s) / APK(s) > Build APK(s)` for a debug APK, or select the `release`
   build variant first for a release build.
4. The APK appears under `app/build/outputs/apk/debug/` or `.../apk/release/` — Studio also
   shows a "locate" link in the notification when the build finishes.

**Command line** (once you have the Android SDK and a JDK installed locally):
```bash
./gradlew assembleDebug     # unsigned/debug-signed APK
./gradlew assembleRelease   # release APK (unsigned unless you add signing, see below)
```
On Windows use `gradlew.bat` instead of `./gradlew`.

### Building a signed release APK

1. Generate a keystore once (keep it safe — losing it means you can never update the app under
   the same signature again):
   ```bash
   keytool -genkeypair -v -keystore my-release-key.jks -keyalg RSA -keysize 2048 \
     -validity 10000 -alias tabi-key
   ```
2. Copy `app/keystore.properties.example` to `app/keystore.properties` and fill in the real
   store/key passwords and the path to the `.jks` file you just created.
3. `app/keystore.properties` is already git-ignored — never commit it or the `.jks` file.
4. Run `./gradlew assembleRelease` (or build the `release` variant in Android Studio). Gradle
   picks up `app/keystore.properties` automatically and produces a signed
   `app-release.apk`. If `keystore.properties` is absent, the release build still succeeds but
   produces an **unsigned** APK.

## Notes for production use

- Never ship a production APK with a hard-coded API key in `gradle.properties`/`BuildConfig` —
  route requests through your own backend to keep the key secret. The direct-from-client OpenAI
  call here is for demo/personal-use simplicity.
- The flashlight and camera features require a physical device with those sensors.
- `QUERY_ALL_PACKAGES` is used to enumerate installed apps for "open <app>" voice commands; be
  aware this permission is subject to Play Store policy review for published apps.
