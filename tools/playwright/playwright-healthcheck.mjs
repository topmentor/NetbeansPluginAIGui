// One-shot Playwright health check for the "playwright-ui" agent skill.
//
// Launches Chromium (headed by default), navigates to a URL, and reports the
// HTTP status, page title, browser version, console output, page errors and a
// screenshot path as a single JSON object on STDOUT. Always closes the browser.
//
// Run it with the node resolved by ensure-playwright.ps1 and pass the resolved
// package/browser paths via env vars:
//
//   $env:PW_PACKAGE      = $cfg.playwrightPackage
//   $env:PW_BROWSER_EXE  = $cfg.browserExecutable
//   & $cfg.node tools\playwright\playwright-healthcheck.mjs http://localhost:8080 --headless
//
// Options:
//   <url> | --url <url>     Target URL (default: an offline data: page).
//   --headless              Run without a visible window (used for CI/verification).
//   --screenshot <path>     Where to save the screenshot (default: .artifacts/healthcheck.png).
//   --timeout <ms>          Navigation timeout (default: 30000).

import { mkdirSync } from 'node:fs';
import { dirname, resolve, join } from 'node:path';
import { fileURLToPath } from 'node:url';
import { launchBrowser } from './helper.mjs';

const argv = process.argv.slice(2);
let url = null;
let headless = false;
let screenshot = null;
let timeout = 30000;

for (let i = 0; i < argv.length; i++) {
  const a = argv[i];
  if (a === '--headless') headless = true;
  else if (a === '--screenshot') screenshot = argv[++i];
  else if (a === '--timeout') timeout = parseInt(argv[++i], 10);
  else if (a === '--url') url = argv[++i];
  else if (!a.startsWith('--')) url = a;
}
if (!url) url = 'data:text/html,<title>Playwright Healthcheck</title><h1>ok</h1>';

const scriptDir = dirname(fileURLToPath(import.meta.url));
if (!screenshot) screenshot = join(scriptDir, '.artifacts', 'healthcheck.png');
screenshot = resolve(screenshot);

const consoleMessages = [];
const pageErrors = [];
let browser;
let result = { ok: false, url };

try {
  const launched = await launchBrowser({ headless });
  browser = launched.browser;
  const { page } = launched;

  page.on('console', (m) => consoleMessages.push({ type: m.type(), text: m.text() }));
  page.on('pageerror', (e) => pageErrors.push(String(e)));

  const response = await page.goto(url, { waitUntil: 'load', timeout });
  const title = await page.title();
  const browserVersion = browser.version();

  mkdirSync(dirname(screenshot), { recursive: true });
  await page.screenshot({ path: screenshot, fullPage: false });

  result = {
    ok: true,
    url,
    httpStatus: response ? response.status() : null,
    title,
    browserVersion,
    headless,
    screenshot,
    consoleMessages,
    pageErrors,
  };
} catch (err) {
  result = {
    ok: false,
    url,
    error: String((err && err.stack) || err),
    consoleMessages,
    pageErrors,
  };
} finally {
  if (browser) {
    try { await browser.close(); } catch { /* ignore */ }
  }
}

process.stdout.write(JSON.stringify(result, null, 2) + '\n');
process.exit(result.ok ? 0 : 1);
