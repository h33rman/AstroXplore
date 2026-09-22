# Implementation Plan - Modern Android Splash Screen API

This plan migrates the app from a custom Compose-based splash screen to the official **Android 12+ Splash Screen API** using `androidx.core:splashscreen`. This provides a smoother, native launch experience and reduces perceived app startup time.

## User Review Required

> [!IMPORTANT]
> **Asset Requirements**: To complete this migration, I need the following from your Illustrator design:
> 1. **Foreground Icon (SVG)**: A 108dp x 108dp vector. Ensure the main logo is centered within a **72dp diameter safe zone**.
> 2. **Background Color**: The Hex code (e.g., `#0F172A`) you want for the splash background.

## Proposed Changes

### Core: Infrastructure

#### [MODIFY] [libs.versions.toml](file:///D:/Apps_Softwares/AstroXplore/gradle/libs.versions.toml)
- Add `androidx-core-splashscreen = "1.0.1"` (or latest).

#### [MODIFY] [build.gradle.kts (app)](file:///D:/Apps_Softwares/AstroXplore/app/build.gradle.kts)
- Include the splashscreen library dependency.

---

### UI & Theming

#### [MODIFY] [themes.xml](file:///D:/Apps_Softwares/AstroXplore/app/src/main/res/values/themes.xml)
- Define `Theme.AstroXplore.Starting`:
    - Set `windowSplashScreenBackground`.
    - Set `windowSplashScreenAnimatedIcon`.
    - Set `postSplashScreenTheme` to `Theme.AstroXplore`.

#### [MODIFY] [AndroidManifest.xml](file:///D:/Apps_Softwares/AstroXplore/app/src/main/AndroidManifest.xml)
- Update `MainActivity` to use `android:theme="@style/Theme.AstroXplore.Starting"`.

---

### Logic & Cleanup

#### [MODIFY] [MainActivity.kt](file:///D:/Apps_Softwares/AstroXplore/app/src/main/java/com/example/astroxplore/MainActivity.kt)
- Call `installSplashScreen()` before `super.onCreate()`.
- Use `setKeepOnScreenCondition` to wait for Supabase/Hilt initialization before transitioning to the main UI.

#### [DELETE] [SplashScreen.kt](file:///D:/Apps_Softwares/AstroXplore/app/src/main/java/com/example/astroxplore/features/splash/ui/SplashScreen.kt)
- Remove the legacy custom splash screen and its navigation logic in `AppNavGraph`.

## Verification Plan

### Manual Verification
1. **Cold Start**: Verify the native icon appears immediately when tapping the app icon.
2. **Transition**: Ensure a seamless transition from the native splash to either the Login or Feed screen.
3. **OS Compatibility**: Test on Android 11 (legacy) and Android 12+ (modern) to ensure consistent behavior.
