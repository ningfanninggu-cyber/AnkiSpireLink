# Contributing

Thanks for wanting to improve Anki Spire Link.

This is a Slay the Spire mod that intentionally keeps Anki in the real desktop app. Changes should preserve that design unless there is a very strong reason to change it.

## Local Setup

You need:

- Slay the Spire
- ModTheSpire
- BaseMod
- Anki desktop
- AnkiConnect
- Java 8-compatible build output
- Maven

The project uses system-scoped dependencies for Slay the Spire, ModTheSpire, and BaseMod. By default it expects a Steam install path. If your Steam library is elsewhere, set `STEAMAPPS` before building:

```bash
STEAMAPPS="/path/to/steamapps" mvn package
```

On macOS with the local bundled toolchain used during development:

```bash
JAVA_HOME="../SpireMod_CET46InSpire/.tools/java/jdk-25.0.3+9/Contents/Home" \
PATH="../SpireMod_CET46InSpire/.tools/java/jdk-25.0.3+9/Contents/Home/bin:../SpireMod_CET46InSpire/.tools/maven/bin:$PATH" \
mvn -s "../SpireMod_CET46InSpire/.tools/maven-settings-aliyun.xml" package
```

## Verification

Before opening a pull request, run:

```bash
mvn test
```

For gameplay changes, also test manually in ModTheSpire:

1. Launch with BaseMod and Anki Spire Link.
2. Start a run and choose to enable Anki Link.
3. Play a non-status card.
4. Complete one real Anki review through AnkiConnect.
5. Confirm the card resolves and the reward message appears.

If the change touches mod compatibility, also test with the relevant mod combination.

## Design Notes

- Do not render Anki card fronts, backs, or answer buttons in-game.
- Keep AnkiConnect as the source of truth for real review results.
- Do not add auto-skip behavior for long questions.
- Preserve manual split-screen play.
- Keep rewards noticeable but not run-breaking.
- Avoid new dependencies unless they clearly reduce risk or complexity.

## Release Notes

When a user-facing behavior changes, update:

- `README.md`
- `使用说明.md`
- `CHANGELOG.md`
- Workshop description, if relevant
