# tools/playwright

Agent-facing Playwright bootstrap for the **playwright-ui** skill. Lets an LLM
agent (Claude Code or Codex) detect a working Playwright/Chromium runtime,
install one automatically if it is missing, and drive a visible Chromium to
debug local web UIs.

## Files

| File | Purpose |
| --- | --- |
| `ensure-playwright.ps1` | Detect the NetBeans-bundled Playwright driver; if absent/incomplete, install `playwright` locally via npm and download Chromium. Prints a JSON config on STDOUT. |
| `playwright-healthcheck.mjs` | Open a URL, report HTTP status / title / browser version / console / page errors + screenshot, then close. |
| `helper.mjs` | `launchBrowser()` — reusable launcher for custom automation. If config/env is missing, it runs `ensure-playwright.ps1` automatically. |
| `.pw-runtime/` | Local npm install target (created only on the npm fallback path). Git-ignored. |
| `.artifacts/` | Screenshots and other run output. Git-ignored. |

## Policy: bundle-first → npm fallback

`ensure-playwright.ps1` prefers the Playwright Core / Chromium bundled with
NetBeans (verified: Core 1.57, Chromium revision 1200). Only when that runtime
is genuinely absent or incomplete does it install `playwright` into
`.pw-runtime/` and download Chromium. It never replaces an existing bundle with
a different version on its own.

`mode` in the output is `bundled`, `npm`, or `error`.

## Quick start (PowerShell)

```powershell
# 1. Optional: prepare the runtime explicitly (auto-installs if needed) and read the config.
$cfg = & powershell -ExecutionPolicy Bypass -File tools\playwright\ensure-playwright.ps1 | ConvertFrom-Json

# 2. Optional: pass the resolved config explicitly.
$env:PW_PACKAGE     = $cfg.playwrightPackage
$env:PW_BROWSER_EXE = $cfg.browserExecutable

# 3. Health check. If the env vars above are omitted, helper.mjs runs setup automatically.
& $cfg.node tools\playwright\playwright-healthcheck.mjs http://localhost:8080
```

For deeper automation, import `helper.mjs` and write a task-specific script; with
`PW_PACKAGE` / `PW_BROWSER_EXE` set it runs against whichever runtime was resolved.
If they are not set, `launchBrowser()` calls `ensure-playwright.ps1`, caches the
resolved config in `.pw-config.json`, and then launches Chromium.

See `docs/SKILL.md` (agent-neutral reference) and
`.claude/skills/playwright-ui/SKILL.md` (Claude Code skill) for the full workflow
and safety rules.
