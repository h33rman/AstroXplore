# Implementation Plan - Bidirectional Synchronization & Offline-First Profiles

This plan focuses on making the User Profile, Library (Saved Papers), and Journal Clubs robustly offline-first by implementing local caching and bidirectional synchronization.

## User Review Required

> [!IMPORTANT]
> **Database Version Upgrade**: The Room database schema has been upgraded to version 6. A destructive migration is configured for development, meaning local data will be reset on first launch after this update.

## Proposed Changes

### Core: Offline-First Foundations

#### [NEW] [ProfileEntity.kt](file:///D:/Apps_Softwares/AstroXplore/app/src/main/java/com/example/astroxplore/core/database/entity/ProfileEntity.kt) / [ProfileDao.kt](file:///D:/Apps_Softwares/AstroXplore/app/src/main/java/com/example/astroxplore/core/database/dao/ProfileDao.kt)
- Adds local storage for User Profiles including names, affiliation, and ORCID.
- Supports `isSynced` flag to track changes made while offline.

#### [MODIFY] [AppDatabase.kt](file:///D:/Apps_Softwares/AstroXplore/app/src/main/java/com/example/astroxplore/core/database/AppDatabase.kt)
- Bump version to 6.
- Register `ProfileEntity` and `ProfileDao`.

---

### Features: Synchronization Logic

#### [MODIFY] [ProfileRepository.kt](file:///D:/Apps_Softwares/AstroXplore/app/src/main/java/com/example/astroxplore/features/profile/data/ProfileRepository.kt)
- Implements `syncProfile` to push offline changes to Supabase.
- Uses `ProfileDao` for instant local feedback even when network is unstable.

#### [MODIFY] [LibraryRepository.kt](file:///D:/Apps_Softwares/AstroXplore/app/src/main/java/com/example/astroxplore/features/library/data/LibraryRepository.kt)
- Enhances `syncLibrary` to fetch all remote saved papers and update local Room cache.
- Tracks `isSynced` for saved papers.

#### [MODIFY] [MainViewModel.kt](file:///D:/Apps_Softwares/AstroXplore/app/src/main/java/com/example/astroxplore/MainViewModel.kt)
- Global sync orchestration: Triggers sync on app launch and when connectivity returns.

## Verification Plan

### Automated Tests
- `ProfileRepositoryTest`: Verify that `updateProfile` saves locally and attempts a remote sync.
- `LibraryRepositoryTest`: Verify that `syncLibrary` merges remote data into local storage.

### Manual Verification
1. **Offline Profile Edit**: Disable internet, update profile name, then enable internet and verify it reflects in Supabase.
2. **Device Sync**: Log in on a second device and verify that Library and Profile data are pulled immediately.
3. **Connectivity Transitions**: Verify that sync is triggered automatically when toggling Airplane Mode.
