# DevBay Launcher

An Android launcher built for developers, by a developer. App-drawer-centric home screen with quick access to the tools you actually use every day — system toggles, widgets, gestures, and a companion logcat/crash viewer, all without leaving your home screen.

<div align="center">
    <a href="https://app.netlify.com/projects/devbay-launcher/deploys"><img src="https://api.netlify.com/api/v1/badges/0058dcd7-e63f-439a-9f1f-7ecf51fae4ae/deploy-status"></a>
</div>
## Features

**Core**
- App-drawer-style home screen with fuzzy search
- Sections: DEBUG (debuggable apps), PINNED, TOOLS, OTHER
- App folders (create, rename, delete, drag apps in/out)
- Icon pack support (standard `appfilter.xml` format — Delta Icons, Whicons, Kaaip, etc.)
- Custom gestures (swipe left/right, double-tap) mapped to any action or app
- Recent apps quick-switch strip
- Notification badges on app icons

**System tools**
- Quick toggle chips (Developer Options, Kill Activities, Slow Animations, Big Fonts, Wireless ADB) via Shizuku
- Home-screen widget support (`AppWidgetHost`)
- One-tap launcher switcher for testing other launchers
- Deep-links to specific Android Settings pages (Developer Options, Battery, Storage, Wi-Fi, and more)
- Shizuku wireless-debugging pairing helper

**Privacy & security**
- Hidden app vault — password, fingerprint, or face unlock
- Type your vault password directly into the search bar to unlock
- Encrypted storage (Android Keystore-backed) for vault credentials and clipboard history

**Productivity**
- Clipboard history manager
- GitHub repository monitor — background sync, notifications for new issues/PRs
- Logcat and crash log viewer — choose between a built-in Shizuku-based viewer or [LogFox](https://github.com/F0x1d/LogFox) as a companion app

**Customization**
- Light / Dark / Follow System theme
- Custom lock screen overlay (non-root; system authentication still handles the actual unlock)

## Requirements

- Android 8.0 (API 26) or newer
- [Shizuku](https://shizuku.rikka.app/) installed and running (wireless debugging or root) for quick-toggle chips and Shizuku-based logcat/crash viewing
- [LogFox](https://github.com/F0x1d/LogFox) installed (optional) if you prefer it over the built-in log viewer

## Tech stack

- Kotlin, single-activity-per-feature architecture (no fragments)
- View Binding
- AndroidX (Activity, AppCompat, RecyclerView, ConstraintLayout, WorkManager, Biometric, Security-Crypto)
- Kotlin Coroutines + Flow
- Shizuku API for elevated shell access
- `AppWidgetHost` for embedded home-screen widgets
- Gradle Kotlin DSL, git-tag-driven versioning (no manual version bumps in `build.gradle.kts`)

## Project structure

app/src/main/java/com/devbay/launcher/
├── activity/       All Activity classes
├── app/            Core app model, repository, search, sectioning
├── cache/          Package-change broadcast receiver
├── clipboard/       Clipboard history
├── folder/         App folders
├── gesture/        Gesture mapping
├── github/         GitHub repo monitor
├── icon/           Icon pack support
├── launcher/       Home-screen adapter and list model
├── lock/           Lock screen overlay
├── logcat/         Logcat/crash viewer launcher
├── notification/   Notification badges, GitHub notifications
├── quicktoggle/    Shizuku quick-toggle chips
├── recent/         Recent apps strip
├── settings/       System Settings deep-links
├── shizuku/        Shizuku command execution
├── theme/          Light/Dark/System theme
├── vault/          Hidden app vault
└── widget/         Home-screen widget hosting

## Building

The project is built entirely through GitHub Actions — there is no requirement to build locally.

1. **`ci.yml`** — runs lint, unit tests, and a compile check on every push/PR
2. **`build.yml`** — triggered after CI passes; builds debug and release APKs (plus the LogFox companion APK from its submodule)
3. **`checker.yml`** — verifies the built APKs are valid, non-corrupt archives
4. **`release.yml`** — on a version tag, signs the release APK and publishes it to GitHub Releases

To build locally instead:

```bash
git clone --recurse-submodules https://github.com/Zoder-Studio/DevBay-Launcher.git
cd DevBay-Launcher
./gradlew assembleDebug
```

If you already cloned without submodules:

```bash
git submodule update --init --recursive
```

## Versioning

Run the **Version Tag** workflow from the Actions tab (patch / minor / major).
It reads the latest git tag, computes the next version, and pushes a new tag — no manual
edits to build.gradle.kts are needed. versionName and versionCode are derived automatically
from git tags and commit count at build time.

## License

DevBay Launcher is licensed under the **GNU General Public License v3.0 (or later)** — see [LICENSE](./LICENSE).
This project was originally MIT-licensed, but includes [LogFox](https://github.com/F0x1d/LogFox) (GPL-3.0) as a git submodule for its companion logcat/crash viewer. Because GPL-3.0 is copyleft, the combined work is distributed under GPL-3.0 in its entirety. See [NOTICE.md](./NOTICE.md) for full third-party attribution.

## Acknowledgments

[LogFox](https://github.com/F0x1d/LogFox) by F0x1d — logcat/crash reading companion app
[Shizuku](https://github.com/RikkaApps/Shizuku) by RikkaApps — elevated shell access without root