# ChefMate Android

ChefMate is an Android cooking companion built with Kotlin and Jetpack Compose. The app combines recipe discovery, shopping flows, pantry-aware planning, and `Bepes`, a server-driven cooking assistant that helps users organize and cook one or more dishes in the same session.

This repository contains the Android client only.

## What It Does

- Browse and search recipes
- View trending recipes and personalized recommendations
- Save, create, edit, and manage recipes
- Build shopping lists from recipes and track shopping history
- Manage pantry ingredients and diet notes
- Sign in, register, and edit profile information
- Chat with `Bepes` to plan and cook meals with session-aware guidance

## App Areas

- `Auth`: sign in, sign up, profile editing
- `Home`: discovery, recommendations, and entry points into cooking flows
- `Recipes`: list, search, detail, create, and personal recipe storage
- `Shopping`: generate ingredient lists and review shopping history
- `Pantry & Diet Notes`: manage available ingredients and cooking preferences
- `Bepes Chat`: server-driven cooking sessions with recipe focus, meal context, and chat history

## Tech Stack

- Kotlin
- Jetpack Compose
- MVVM architecture
- Room
- OkHttp
- Gson
- Coil
- DataStore
- Android Security Crypto

## Local Setup

### Prerequisites

- Android Studio
- JDK 11
- Android SDK with API level `35`
- A running ChefMate backend environment

### Configure `local.properties`

Copy the sample config and fill in your local values:

```bash
cp local.properties.example local.properties
```

Required keys:

```properties
CHEFMATE_API_BASE_URL=https://your-api-host.example.com
CHEFMATE_CHAT_API_KEY=replace-with-chat-api-key
```

The Android build reads these values in `app/build.gradle.kts` and fails early if either key is missing or empty.

Optional local signing keys can also be added in `local.properties`:

```properties
KEY_ALIAS=projectkey
KEYSTORE_PASSWORD=
KEY_PASSWORD=
```

## Build And Run

Install a debug build on a connected device or emulator:

```bash
./gradlew :app:installDebug
```

Launch the app with `adb`:

```bash
adb shell am start -n com.watb.chefmate/.ui.main.MainActivity
```

If you want to build without installing:

```bash
./gradlew :app:assembleDebug
```

## Architecture Notes

This project is an Android client that talks to a separate backend. The app uses a Compose-based MVVM structure with local persistence for app data and secure storage for sensitive session data.

`Bepes` is the AI-assisted cooking flow in the app. The current client uses a server-driven chat/session model, where the backend owns meal-session state, message history, focus recipe, and cooking progress while the Android client renders and interacts with that state.

## Repository Layout

- `app/`: Android application source
- `gradle/`: Gradle version catalog and wrapper configuration
- `local.properties.example`: sample local configuration for required API keys

## Ecosystem

- Server: [ChefMate_Server](https://github.com/PhongDayNai/ChefMate_Server)
- Admin web: [ChefMate_Admin_Web](https://github.com/PhongDayNai/ChefMate_Admin_Web)

## Open Source Status

This repository is being cleaned up and documented for public/open-source use. If you find a bug, want to suggest an improvement, or want to contribute a fix, feel free to open an issue or a pull request.

Some project conventions and community files such as a formal license or contribution guide may still be added separately.
