# Anki Spire Link

Anki Spire Link is a Slay the Spire mod that connects card plays to real Anki reviews through AnkiConnect.

Before a normal card is played, the game waits for you to answer one review card in the real Anki desktop app. Your Anki rating then decides the in-game reward for that card.

Detailed Chinese player guide: [使用说明.md](使用说明.md)

## What This Mod Is

This mod is for players who want Slay the Spire to gently, but firmly, make them study.

It does not embed Anki inside Slay the Spire. It does not render Anki card fronts, backs, or answer buttons in-game. Instead, it uses your real Anki desktop app and reads real review results through AnkiConnect.

That means:

- Your reviews are recorded by Anki normally.
- Your existing decks, card templates, media, and add-ons still work.
- Long math, language, image, audio, or medical cards can be answered in Anki itself.
- You can use split-screen play: Anki on one side, Slay the Spire on the other.

## Features

- Adds the `Anki Link` relic.
- At the start of each run, asks whether to enable Anki Link for that run.
- Intercepts non-status, non-curse card plays.
- Waits for one real Anki review before the card resolves.
- Supports a configured Anki deck name, or manual deck selection.
- Supports macOS app focus switching.
- Supports manual split-screen mode on all platforms.
- Shows an in-game message explaining the reward you received.
- Includes English, Simplified Chinese, and Traditional Chinese localization.

## Requirements

- Slay the Spire
- ModTheSpire
- BaseMod
- Anki desktop running locally
- AnkiConnect installed and listening on `http://127.0.0.1:8765`

AnkiConnect add-on code: `2055492159`

## Gameplay Flow

1. Start a run.
2. Choose `Use this run` / `本局使用`.
3. Play a non-status, non-curse card.
4. The game opens an Anki review waiting screen.
5. Answer one real card in Anki.
6. Click Again, Hard, Good, or Easy in Anki.
7. The game detects the rating through AnkiConnect.
8. The original card resolves with the matching reward.

There is no short auto-skip timer. If Anki has an active review card, the game waits until a real Anki answer is recorded.

## Rewards

| Anki rating | Card multiplier | Fixed reward |
| --- | ---: | --- |
| Again | x1 | Max HP +1 |
| Hard | x1 | Gain 1 Energy and 3 Block |
| Good | x2 | Draw 1 card and gain 1 temporary Strength |
| Easy | x4 | Gain 1 Energy, draw 1 card, and gain 1 temporary Strength and 1 temporary Dexterity |

The multiplier applies to the played card's damage, block, positive magic number, and multi-target damage. Card stats are restored after the play resolves.

Each rating can also roll one small random extra reward:

| Anki rating | Random relic | Max HP +3 to +5 | Small heal | Small gold |
| --- | ---: | ---: | ---: | ---: |
| Again | 1% | 4% | 8% | 12% |
| Hard | 3% | 6% | 10% | 14% |
| Good | 8% | 8% | 12% | 15% |
| Easy | 15% | 10% | 10% | 10% |

The relic chance is intentionally low. It is meant to feel like a pleasant surprise, not a reliable farming engine.

## Focus Switching

On macOS, the mod can focus Anki when a review starts and best-effort focus Slay the Spire after the review is detected.

You can disable this in the mod settings with `Switch focus to Anki`. When disabled, the mod only shows the waiting screen and polls AnkiConnect in the background. This is the recommended mode for manual split-screen play.

On Windows and Linux, focus switching is manual. Keep Anki open and use your normal window layout or Alt+Tab.

## Installation

For normal play, install through Steam Workshop when available.

For manual installation, place `AnkiSpireLink.jar` in the Slay the Spire `mods` folder and launch through ModTheSpire with BaseMod enabled.

Do not load both a local jar and a Steam Workshop copy at the same time.

## Building

This project uses Maven and system-scoped dependencies for Slay the Spire, ModTheSpire, and BaseMod.

From this directory:

```bash
mvn package
```

If your Steam library is not in the default location, set `STEAMAPPS`:

```bash
STEAMAPPS="/path/to/steamapps" mvn package
```

On macOS, the default Steam profile expects:

```text
~/Library/Application Support/Steam/steamapps
```

On Windows, the default Steam path is usually under:

```text
C:/Program Files (x86)/Steam/steamapps
```

That location may require an elevated terminal for the package step to copy the jar into the game `mods` folder.

The local development environment used for this repository can build with:

```bash
JAVA_HOME="../SpireMod_CET46InSpire/.tools/java/jdk-25.0.3+9/Contents/Home" \
PATH="../SpireMod_CET46InSpire/.tools/java/jdk-25.0.3+9/Contents/Home/bin:../SpireMod_CET46InSpire/.tools/maven/bin:$PATH" \
mvn -s "../SpireMod_CET46InSpire/.tools/maven-settings-aliyun.xml" package
```

## Testing

Run the unit tests:

```bash
mvn test
```

Manual smoke test:

1. Launch ModTheSpire with BaseMod and Anki Spire Link.
2. Open Anki and confirm AnkiConnect responds at `http://127.0.0.1:8765`.
3. Start a run and choose to enable Anki Link.
4. Play a non-status card.
5. Complete one Anki review card.
6. Confirm the original card resolves and an in-game reward message appears.

## Current Limitations

- The AnkiConnect endpoint is fixed to `127.0.0.1:8765`.
- The deck setting is an exact Anki deck name. Leave it empty to choose the deck manually in Anki.
- Automatic application focus switching is macOS-only.
- Mobile Anki clients and AnkiWeb are not supported because they do not expose the local AnkiConnect API used by this mod.

## Project Structure

```text
src/main/java/com/ankispirelink
  actions/    Small custom game actions.
  anki/       AnkiConnect client and external review session logic.
  game/       Reward policy, run-choice state, and card-stat helpers.
  patches/    ModTheSpire patches for card play interception.
  relics/     Anki Link relic implementation.
  screens/    Custom start-choice and review-wait screens.
  ui/         BaseMod settings panel.

src/main/resources
  ModTheSpire.json
  AnkiSpireLinkResources/

src/test/java
  Unit tests for Anki, reward, card-stat, and run-choice logic.
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md).

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE).

This project is not affiliated with Mega Crit, Anki, AnkiConnect, ModTheSpire, or BaseMod.
