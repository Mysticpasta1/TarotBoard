# Welcome to TarotBoard!

## Default Keybinds

            Cards / Chips:
                Drag ......................... Move
                Double-click (left) ......... Flip
                Shift+Click (left) .......... Rotate -1°
                Ctrl+Click (left) ........... Rotate -90°
                Shift+Click (right) ......... Rotate +1°
                Ctrl+Click (right) .......... Rotate +90°
                Double-click (right) ........ Reset rotation

            Dice:
                Double-click ................ Roll
                Drag ........................ Move
            
            General:
                Tab ......................... Show player list (multiplayer)
                F ........................... Multi-flip chips while hovering
                Drag onto ✖ zone .......... Delete item

All keybinds are configurable from the Settings menu.

## Building

Requires JDK 26+ and Gradle (wrapper included).

    git clone https://github.com/<your>/TarotBoard
    cd TarotBoard
    ./gradlew build

Run the desktop application:

    ./gradlew run

Build a fat JAR:

    ./gradlew shadowJar
    # Output at build/libs/TarotBoard-3.0.0-all.jar

Build a native installer/package:

    ./gradlew jpackage

Run the headless dedicated server:

    ./gradlew runServer                                # defaults to port 5555, password "admin"
    ./gradlew runServer --args="--port 7777"            # custom port
    ./gradlew runServer --args="--password mypass"      # custom operator password
    ./gradlew runServer --args="--port 7777 --password mypass"

The server also accepts a bare port number as the first positional argument (e.g. `--args="7777"`).

## Android Build

Android deployment requires GraalVM and the Android SDK:

    ./gradlew build -PandroidBuild=true

## Contributing

### Code Style

- No inline or block comments in source code. All documentation is in Javadoc on public APIs.
- Use `record` types for simple data carriers (e.g., `GuiStyle`, `Styles`, `SuitStyle`).
- Use `wither` methods (`withX(value)`) for immutable record mutation.
- UI colour and font values come from theme configuration (`ThemeConfiguration.GuiConfig`), never hardcoded.

### Theme System

All visual theming is driven by JSON files under `src/main/resources/com/mystic/tarotboard/assets/configs/`:

| File          | Purpose                                                                                 |
|---------------|-----------------------------------------------------------------------------------------|
| `themes.json` | Theme registry — each entry declares image paths and references to gui/suit/cursor keys |
| `gui.json`    | UI font sizes, colours, and layout values (keyed, referenced by `guiKey`)               |
| `suits.json`  | Suit group colour hex values (keyed, referenced by `suitStyleKey`)                      |
| `cursor.json` | Remote cursor appearance (size, dot, label) (keyed, referenced by `cursorKey`)          |
| `css.json`    | Help page stylesheets (keyed, referenced by `helpCssLight`/`helpCssDark` in gui config) |

Image assets live in `src/main/resources/com/mystic/tarotboard/assets/`.

### Module System

The project uses Java modules (`module-info.java`). For dependencies without module-info (flexmark JARs), the Gradle plugin `org.gradlex.extra-java-module-info` declares automatic modules in `build.gradle`.

### Testing

No test suite is currently configured. Contributions adding tests are welcome.

## Examples

The `Example/` directory at the project root mirrors the runtime asset structure:

```
Example/
├── assets/             ← placeholder PNG images (card fronts, backs, chips, background)
└── configs/
    ├── themes.json     ← example theme registry entry with all fields documented
    ├── gui.json        ← all UI configuration fields with inline docs
    ├── suits.json      ← suit group colour definitions
    ├── cursor.json     ← remote cursor settings
    └── css.json        ← help page light/dark stylesheets
```

Every JSON key is annotated with `_comment_` / `_section_` / `_about_` prefixed keys that serve as inline documentation. These documentation keys are safely ignored by Gson during parsing.

Use these files as a reference when creating custom themes or contributing configuration changes.

## Attributions

Background Image -
https://www.freepik.com/free-photo/perfect-green-grass_902965.htm#fromView=search&page=1&position=4&uuid=d990dedb-0fea-400d-8b32-55c7cc5d4d3d

Card Back Image -
https://in.pinterest.com/pin/388857749087799765/

Card Front Image -
https://pngtree.com/freebackground/royal-golden-blue-mandala-art-background-with-border-invitation-card-wedding-islamic-arabic-geometric-pattern-diwali_1596347.html

Poker Chip Images -
Mysticpasta1 and Waterpicker using GIMP

Code and Ideas -
Mysticpasta1
