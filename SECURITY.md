# Security

Anki Spire Link communicates with AnkiConnect on the local machine at `http://127.0.0.1:8765`.

## What the Mod Uses

The mod uses AnkiConnect to:

- Ask Anki to open a configured deck review screen.
- Read the currently displayed review card ID.
- Read review-log entries for cards seen during the current review session.
- Map Anki ease values to in-game rewards.

## What the Mod Does Not Do

The mod does not intentionally:

- Upload Anki data to an external server.
- Read AnkiWeb credentials.
- Create, edit, or delete Anki notes.
- Render Anki card contents inside Slay the Spire.

## Reporting Issues

Please report security-sensitive issues privately if possible. If no private channel is available, open a GitHub issue with minimal reproduction details and avoid posting personal deck data, card contents, or logs containing private information.

## User Guidance

Keep AnkiConnect bound to localhost unless you understand the risks of exposing it to your network.
