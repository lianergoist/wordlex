# Project Plan

A Wordle-like app where users can guess 5-letter words. The app includes a hint system that tracks possible words based on guesses. Users can request hints to place correct letters or view a list of potential words. Data (word list) should be loaded into a database on the first run.

## Project Brief

# Project Brief: Wordle Plus

A modern take on the classic word-guessing game, featuring an intelligent hint system and a robust local dictionary for seamless offline play.

## Features
* **5-Letter Word Gameplay:** Classic Wordle mechanics with color-coded visual feedback for correct, present, and absent letters.
* **Intelligent Hint System:** Tracks potential words based on current progress, allowing users to reveal correct letters or view a list of remaining possibilities.
* **Local Word Database:** Automatic first-run population of a local dictionary to ensure high performance and offline accessibility.
* **Persistent Game State:** Automatically saves and resumes game progress using state-driven logic to prevent data loss.

## High-Level Tech Stack
* **Kotlin:** Leveraging modern language features for robust and maintainable code.
* **Jetpack Compose:** Utilizing Material 3 to create a vibrant, energetic, and responsive user interface.
* **Jetpack Navigation 3:** Implementing a state-driven navigation architecture for seamless transitions and state management.
* **Compose Material Adaptive:** Ensuring a high-quality experience across all device form factors (phones, tablets, and foldables).
* **Room Database:** Managing the 5-letter word dictionary and game persistence.
* **Kotlin Coroutines & Flow:** Handling asynchronous word processing and reactive UI state updates.

## Implementation Steps
**Total Duration:** 24m 14s

### Task_1_DataLayer_Room: Set up the Room database for word storage and game state persistence. Implement the logic to populate the local dictionary with 5-letter words on the first run.
- **Status:** COMPLETED
- **Updates:** Room database with Word and GameState entities created. Logic for populating words from a raw resource (words.txt) implemented. Repository pattern set up with WordRepository and GameStateRepository. MainActivity updated for initialization check. Adaptive icon and edge-to-edge display initialized. Project SDK updated to 37.
- **Acceptance Criteria:**
  - Room database and entities (Word, GameState) are created
  - Logic for populating words from a local resource is implemented
  - Database initialization is verified on app launch
- **Duration:** 18m 44s

### Task_2_GameEngine_ViewModel: Develop the core Wordle game logic, including guess evaluation (correct, present, absent) and the intelligent hint system. Create a ViewModel to manage the game state using Flow.
- **Status:** COMPLETED
- **Updates:** WordleGameEngine implemented with guess evaluation and possible words filtering logic. GameViewModel developed to manage game state, handle inputs, and persist progress. Hint system (letter reveal and possible words list) integrated into ViewModel. Unit tests for game engine passed.
- **Acceptance Criteria:**
  - Game engine correctly evaluates 5-letter guesses
  - Hint system identifies possible words based on current board state
  - ViewModel exposes game state and handles user actions (input, hint request)
- **Duration:** 2m 14s

### Task_3_Compose_UI: Build the game UI using Jetpack Compose and Material 3. Implement the word grid, on-screen keyboard, and hint overlays. Apply a vibrant, energetic color scheme.
- **Status:** COMPLETED
- **Updates:** Built the game UI using Jetpack Compose and Material 3. Implemented WordleGrid, WordleKeyboard, and HintDialog components. Applied a vibrant Material 3 color scheme with light/dark mode and dynamic color support. Integrated adaptive layout using WindowSizeClass for phone and tablet optimization. MainActivity updated to host the game UI. Edge-to-edge display and adaptive icon finalized.
- **Acceptance Criteria:**
  - Word grid and keyboard are functional and visually reflect game state
  - Hint system UI (revealing letters/words) is integrated
  - Material 3 theme with vibrant colors and Edge-to-Edge display is implemented
- **Duration:** 3m 16s

### Task_4_Navigation_Icon_Verification: Integrate Jetpack Navigation 3 and Compose Material Adaptive. Create an adaptive app icon and perform a final run to verify stability and requirements.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Jetpack Navigation 3 manages app flow
  - UI is adaptive across different form factors
  - Adaptive app icon is created and assigned
  - App builds successfully, passes existing tests, and runs without crashing
- **StartTime:** 2026-07-19 23:09:20 CEST

