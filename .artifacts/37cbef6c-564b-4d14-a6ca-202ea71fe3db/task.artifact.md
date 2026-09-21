# Tasks: Bidirectional Synchronization

- [x] **Phase 1: Database Infrastructure**
    - [x] Create `ProfileEntity` and `ProfileDao`
    - [x] Update `AppDatabase` to version 6
    - [x] Provide `ProfileDao` in `DatabaseModule`
    - [x] Add `isSynced` to `SavedPaperEntity`
- [x] **Phase 2: Repository Sync Logic**
    - [x] Update `ProfileRepository` with `syncProfile` and local caching
    - [x] Enhance `LibraryRepository` with robust `syncLibrary`
    - [x] Verify `GroupRepository` sync integration
- [x] **Phase 3: Global Orchestration**
    - [x] Implement `syncAll` in `MainViewModel`
    - [x] Trigger sync on app launch and connectivity restoration
- [ ] **Phase 4: UI Verification**
    - [ ] Run build and verify data persistence across app restarts
    - [ ] Test offline-to-online sync transition
