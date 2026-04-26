---
description: Work with Open WebUI API integration in the companion app
---

# Open WebUI Integration

Use this skill when changing the Open WebUI bridge, adding API calls, or refreshing API context.

## Context Files

- Curated map: `docs/openwebui/index.md`
- OpenAPI snapshot: `docs/openwebui/openapi.snapshot.json`
- Kotlin client boundary:
  `app/src/main/java/com/meta/wearable/dat/externalsampleapps/openwebuibridge/openwebui/OpenWebUiClient.kt`
- Env template: `.env.example`

## Workflow

1. Read `docs/openwebui/index.md` first.
2. Check the committed OpenAPI snapshot for endpoint details.
3. Keep HTTP calls inside the `openwebui` package.
4. Expose typed `suspend` functions and sealed result types to the rest of the app.
5. Use `OWUI_BASE_URL` in scripts and examples. Do not hard-code private hosts.
6. Update the curated index when adding, removing, or changing an endpoint.

## Refreshing The Snapshot

Live Open WebUI docs are not public. They are available only when Open WebUI is configured with
`ENV=dev`.

```powershell
$env:OWUI_BASE_URL = "https://your-dev-openwebui.example"
.\scripts\refresh-openwebui-openapi.ps1
```

Do not require this refresh for normal coding work. Use the committed snapshot when the live docs
are unavailable.
