# Changelog

## 0.1.0 - 2026-05-05

Initial public release candidate.

- Add the `Anki Link` run relic.
- Ask at run start whether to enable Anki Link for the current run.
- Intercept non-status, non-curse card plays and wait for a real Anki review.
- Connect to Anki through AnkiConnect at `http://127.0.0.1:8765`.
- Support optional deck-name configuration through BaseMod settings.
- Support macOS focus switching, plus manual split-screen mode.
- Apply rating-based card multipliers and rewards:
  - Again: x1, max HP +1.
  - Hard: x1, gain 1 Energy and 3 Block.
  - Good: x2, draw 1, temporary Strength +1.
  - Easy: x4, gain 1 Energy, draw 1, temporary Strength +1 and temporary Dexterity +1.
- Add low-probability random extra rewards, including relics, max HP, healing, and gold.
- Add English, Simplified Chinese, and Traditional Chinese localization.
- Add unit tests for AnkiConnect parsing, review sessions, run-choice state, card stat math, and reward policy.
