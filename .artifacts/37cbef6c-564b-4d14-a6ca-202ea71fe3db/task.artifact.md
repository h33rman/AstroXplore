# Tasks: Modern Splash Screen Migration

- [x] **Phase 1: Dependencies**
    - [x] Add `androidx.core:splashscreen` to Version Catalog
    - [x] Add dependency to `app/build.gradle.kts`
- [x] **Phase 2: Assets & Theming**
    - [x] Import user-provided SVG as Vector Drawable
    - [x] Create Splash Screen theme in `themes.xml`
    - [x] Update `AndroidManifest.xml` launcher theme
- [x] **Phase 3: Integration**
    - [x] Implement `installSplashScreen()` in `MainActivity`
    - [x] Configure `setKeepOnScreenCondition` for session loading
- [x] **Phase 4: Cleanup**
    - [x] Delete `SplashScreen.kt`
    - [x] Remove Splash route from `AppNavGraph.kt`
    - [x] Clean up redundant `MainViewModel` transitions
