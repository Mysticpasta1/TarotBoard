# Changelog - TarotBoard

## [3.0.0] - 2026-05-12

### Added

- **Scenes refactored into dedicated classes:**
  - `StartScene` — main menu with New Game, Load Game, Multiplayer, Help, Settings buttons; update-check label at bottom
  - `GameScene` — in-game board with proportional scaling, right control panel, discard zone, draggable cards/chips/dice, cursor overlay, multiplayer controls
  - `MultiplayerScene` — hub scene with Host Game / Join Game / Back buttons
  - `HostGameScene` — host controls (name, color, port, op password, cursor selector, network status)
  - `JoinGameScene` — join controls (name, color, IP/port, operator access, cursor selector, network status)
  - `SettingsScene` — settings window with theme selector, key bindings, font/color configuration
  - `HelpScene` — help/credits window with theme-aware stylesheet

- **New game item classes:** `Cards.java`, `Chips.java`, `Dice.java` (under `com.mystic.tarotboard.items`)
- **Networking layer:** `GameServer`, `GameClient`, `HeadlessServer`, `NetworkMessage`, `UpdateManager`
- **Configuration system:** `GuiStyle`, `KeyBindConfig`, `RemoteCursor`, `SuitStyle` (enhanced), `ThemeManager`, `ThemeConfiguration`
- **Utility classes:** `Styles.java` (unified CSS styling), `UIUtils.java` (drag/background/scale helpers), `SaveData.java`, `PlatformPaths.java`, `CardDataHelper.java`
- **Bundled resources:** `colorpicker.css`, logo, `configs/*.json` (gui, themes, suits, cursor, css), updated card/chip/background images
- **Example directory:** Full set of example config files with documented JSON schema (`_comment_`/`_section_` keys) and theme assets
- **CI/CD:** GitHub Actions build workflow
- **Javadocs:** All public methods across every class now documented

### Changed

- **TarotBoard.java** — major rewrite:
  - `main()` handles `--version`/`--help` flags and calls `launch()`; `init()` re-enters `main()` via `launched` boolean guard
  - `checkForUpdates(Label)` replaces popup alerts — runs in background thread, shows clickable download label only when update available
  - Update indicator positioned at `Pos.BOTTOM_CENTER` in start content StackPane
  - Scene management stores `GameScene`, `StartScene`, `MultiplayerScene`, `HostGameScene`, `JoinGameScene` references
  - `switchToMultiplayer()`, `switchToHostGame()`, `switchToJoinGame()` methods added
  - `hostGame()` and `joinGame()` made public
  - All methods now have Javadocs
- **Multiplayer scene readability:** Inline labels use `Styles.mpLabel()` (bold, dropshadow, configurable text color + background); `mpTitle()` has `-fx-text-fill: white`; `mpLabelBg` config field added
- **ColorPicker text styling:** Replaced temp-file CSS with bundled `assets/colorpicker.css` resource loaded via `getClass().getResource(...).toExternalForm()`
- **Mouse drag fix:** `UIUtils.makeDraggable()` uses `parent.sceneToLocal()` for cursor-speed drag, fixing 1:1 tracking with scaled game content
- **Multiplayer controls:** `disconnectButton`, `opPwInGame`, `requestOpInGame` are now fields (not locals); hidden by default, toggled via `setMultiplayerControlsVisible(boolean)`
- **Disconnect button** now calls `leaveGame()` + `switchToStart()` to return to main menu
- **Cursor image** applies locally via `ImageCursor` with center hotspot for proper PNG transparency support
- **`gameContent.toFront()` removed** from MOUSE_CLICKED filter, `spawnDie()`, and `spawnChip()` — was pushing game content above UI controls
- **`applyBackgroundImage(Pane)`** signature simplified (removed unused Scene parameter)
- **Build:** All `Main-Class` references changed from `com.mystic.tarotboard.Launcher` to `com.mystic.tarotboard.TarotBoard`
- **Module-info:** Added `exports com.mystic.tarotboard.items`

### Removed

- **`Launcher.java`** — deleted; `TarotBoard.main()` calls `launch()` directly, no wrapper needed in modular JDK 26
- **Dead code removed:**
  - `settingsRoot()`, `settingsPropLabel()`, `settingsColorField()`, `settingsSpinner()` from `Styles.java`
  - `helpBg`, `settingsBg`, `settingsSectionColor` from `ThemeConfiguration` (and from `gui.json` config)
  - `showUpdateAvailableDialog()` from `TarotBoard.java`
  - `getPlayerListOverlay()` from `GameScene.java`
- **Old `Card.java`** (under `gameitems`) — replaced by `items/Cards.java`
- **Old `Chip.java` / `Die.java`** (under `gameitems`) — replaced by `items/Chips.java` / `items/Dice.java`
- **Old `HelpWindow.java`** — replaced by `scenes/HelpScene.java`
- **Old bundled `themes.json`** — replaced by `assets/configs/themes.json`
- **Old `css/help.css`** — replaced by `assets/configs/css.json` with theme-aware stylesheets
- **`primaryStage` parameter** from `MultiplayerScene` constructor
- **Unused imports** (`java.nio.file.*`) from `GameScene` and `MultiplayerScene`
- **Old `examples/ExampleTheme.json`** — replaced by `Example/` directory with full config set

### Fixed

- Mouse drag now follows cursor at 1:1 speed under scaled `gameContent` (scene-to-local coordinate conversion)
- In-game buttons no longer blocked by `gameContent.toFront()` pushing game layer above control panel
- `ColorPicker` text is now white/bold via bundled CSS instead of temp-file workaround
- Multiplayer controls hidden in singleplayer, only shown when entering multiplayer

### Configuration Schema

All JSON configs now support `_comment_` and `_section_`-prefixed keys for inline documentation, safely ignored by Gson.

- `gui.json` — font sizes, colors, padding for all UI elements
- `themes.json` — theme registry with image paths, suit/gui/cursor key references
- `suits.json` — suit group color definitions
- `cursor.json` — remote cursor color/opacity/size settings
- `css.json` — theme-aware stylesheet references for Help scene
