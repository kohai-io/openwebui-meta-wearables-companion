# Open WebUI Integration

This companion app bridges Meta Wearables DAT camera access into an Open WebUI chat workflow.
Use this file as the agent-readable map, and use `docs/openwebui/openapi.snapshot.json` as the
machine-readable API contract.

## Source of Truth

- Live Swagger UI is dev-only: `${OWUI_BASE_URL}/docs`.
- Live OpenAPI JSON is dev-only: `${OWUI_BASE_URL}/openapi.json`.
- Open WebUI must be running with `ENV=dev` for those docs to be available.
- Normal coding work should rely on the committed snapshot at `docs/openwebui/openapi.snapshot.json`.
- Refresh the snapshot only when a dev Open WebUI instance is available.

## Environment

Local scripts should read these names from the process environment or a local `.env` file:

| Name | Purpose |
| --- | --- |
| `OWUI_BASE_URL` | Root Open WebUI server URL, without `/api` or `/api/v1`. |
| `OWUI_API_KEY` | Bearer token from Open WebUI Settings > Account. |
| `OWUI_MODEL` | Optional default model id for examples and smoke tests. |
| `GITHUB_TOKEN` | GitHub token with `read:packages` for DAT SDK dependencies. |
| `ANDROID_SERIAL` | Optional target device/emulator selector. |

See `.env.example` for a template. Do not commit `.env` or real API keys.

Gradle also reads `OWUI_BASE_URL`, `OWUI_API_KEY`, and `OWUI_MODEL` from the process environment,
`.env`, or `local.properties` and exposes them as development defaults through `BuildConfig`.
The app stores user-edited values in private preferences after first launch.

## Kotlin Client Boundary

The Android app's Open WebUI boundary is:

`app/src/main/java/com/meta/wearable/dat/externalsampleapps/openwebuibridge/openwebui/OpenWebUiClient.kt`

Keep Open WebUI HTTP details inside this package. UI and stream code should call typed functions
such as `listModels`, `askText`, and `askAboutImage` rather than building endpoint strings.

Current implementation style:

- `suspend` functions run network work on `Dispatchers.IO`.
- Results are returned as sealed result types, not thrown across the ViewModel boundary.
- Requests use `Authorization: Bearer <OWUI_API_KEY>`.
- The client accepts flexible base URL inputs but normalizes them back to the server root.

## Endpoints Used By The App

| Feature | Method | Endpoint | Notes |
| --- | --- | --- | --- |
| List models | `GET` | `/api/models` | Returns available model ids. |
| List chats | `GET` | `/api/v1/chats/` | Returns prior chat ids, titles, and timestamps. |
| Create chat | `POST` | `/api/v1/chats/new` | Creates the Open WebUI chat history container. |
| Fetch chat | `GET` | `/api/v1/chats/{chatId}` | Used to preserve current message branch. |
| Update chat | `POST` | `/api/v1/chats/{chatId}` | Writes user and assistant messages into history. |
| Upload image | `POST` | `/api/v1/files/` | Multipart JPEG upload for visible chat history attachments. |
| File content | `GET` | `/api/v1/files/{fileId}/content` | Embedded in Markdown for uploaded images. |
| Chat completion | `POST` | `/api/v1/chat/completions` | Sends text or vision-style chat messages. |
| Completion event | `POST` | `/api/chat/completed` | Notifies Open WebUI that the assistant response completed. |

## Request Patterns

Text ask:

```json
{
  "model": "${OWUI_MODEL}",
  "stream": true,
  "messages": [
    { "role": "system", "content": "You are helping through Meta glasses." },
    { "role": "user", "content": "What should I do next?" }
  ],
  "chat_id": "<chat-id>",
  "id": "<assistant-message-id>",
  "session_id": "<session-id>"
}
```

Vision ask:

```json
{
  "role": "user",
  "content": [
    { "type": "text", "text": "What am I looking at?" },
    {
      "type": "image_url",
      "image_url": { "url": "data:image/jpeg;base64,<jpeg-bytes>" }
    }
  ]
}
```

The app also uploads the JPEG to `/api/v1/files/` and writes Open WebUI file metadata into the chat
history so the image appears in the Open WebUI conversation UI.

## Maintenance Rules

- Prefer extending `OpenWebUiClient` over adding HTTP calls in ViewModels or Composables.
- Keep endpoint names and payload assumptions in this file whenever app behavior changes.
- Compare changes against `docs/openwebui/openapi.snapshot.json`.
- If live docs are unavailable, do not assume `/docs` exists; ask the user to run Open WebUI with
  `ENV=dev` or work from the committed snapshot.
- If a generated client is introduced later, keep it wrapped behind the existing app-facing
  `OpenWebUiClient` style API so generated churn does not leak into UI code.
