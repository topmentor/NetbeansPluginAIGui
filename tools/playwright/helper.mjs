// Reusable Playwright launch helper for the "playwright-ui" agent skill.
//
// Load Playwright from a resolved package path (bundled NetBeans driver or a
// local npm install) and launch Chromium. Config comes from arguments,
// PW_PACKAGE / PW_BROWSER_EXE, or by automatically running ensure-playwright.ps1.
//
// Example (agent writes a one-off automation):
//   import { launchBrowser } from './helper.mjs';
//   const { browser, page } = await launchBrowser({ headless: false });
//   try { await page.goto(url); /* ... */ } finally { await browser.close(); }

import { createRequire } from 'node:module';
import { existsSync, readFileSync, writeFileSync } from 'node:fs';
import { dirname, join } from 'node:path';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';

const require = createRequire(import.meta.url);
const scriptDir = dirname(fileURLToPath(import.meta.url));
const configFile = join(scriptDir, '.pw-config.json');

function readCachedConfig() {
  if (forceNpm()) return null;
  if (!existsSync(configFile)) return null;
  try {
    const cfg = JSON.parse(readFileSync(configFile, 'utf8'));
    if (cfg && cfg.mode !== 'error' && cfg.node && cfg.playwrightPackage) return cfg;
  } catch {
    // Ignore corrupt cache and regenerate below.
  }
  return null;
}

function forceNpm() {
  return /^(1|true|yes)$/i.test(process.env.SOX_PLAYWRIGHT_FORCE_NPM || '');
}

function runEnsurePlaywright() {
  const script = join(scriptDir, 'ensure-playwright.ps1');
  const args = [
    '-ExecutionPolicy', 'Bypass',
    '-File', script,
    '-Quiet',
  ];
  if (forceNpm()) args.push('-ForceNpm');
  const result = spawnSync('powershell', args, {
    cwd: dirname(scriptDir),
    encoding: 'utf8',
    windowsHide: true,
  });
  if (result.status !== 0) {
    const detail = result.stdout || result.stderr || `exit ${result.status}`;
    throw new Error(`Playwright setup failed: ${detail.trim()}`);
  }
  const cfg = JSON.parse(result.stdout);
  if (!cfg || cfg.mode === 'error') {
    throw new Error(`Playwright setup failed: ${(cfg && cfg.error) || 'unknown error'}`);
  }
  writeFileSync(configFile, JSON.stringify(cfg, null, 2) + '\n');
  return cfg;
}

function resolveConfig({ packagePath, browserExecutable } = {}) {
  if (packagePath || process.env.PW_PACKAGE) {
    return {
      playwrightPackage: packagePath || process.env.PW_PACKAGE,
      browserExecutable: browserExecutable || process.env.PW_BROWSER_EXE,
    };
  }
  const cached = readCachedConfig();
  if (cached) return cached;
  return runEnsurePlaywright();
}

/** Require the Playwright module from an explicit package path or PW_PACKAGE. */
export function loadPlaywright(packagePath) {
  const cfg = resolveConfig({ packagePath });
  const pkg = cfg.playwrightPackage;
  if (!pkg) {
    throw new Error('Playwright package path missing. Auto setup did not return playwrightPackage.');
  }
  return require(pkg);
}

/**
 * Launch Chromium and open a fresh page.
 * @param {object} [opts]
 * @param {string} [opts.packagePath]      Path to the Playwright package (defaults to PW_PACKAGE).
 * @param {string} [opts.browserExecutable] chrome.exe path (defaults to PW_BROWSER_EXE).
 * @param {boolean} [opts.headless=false]  Headed by default so a human can watch/assist.
 * @param {string[]} [opts.args]           Extra Chromium args.
 * @returns {Promise<{chromium, browser, context, page}>}
 */
export async function launchBrowser({ packagePath, browserExecutable, headless = false, args } = {}) {
  const cfg = resolveConfig({ packagePath, browserExecutable });
  const { chromium } = loadPlaywright(cfg.playwrightPackage);

  const launchOpts = {
    headless,
    args: args || (headless ? [] : ['--start-maximized']),
  };
  const exe = cfg.browserExecutable || process.env.PW_BROWSER_EXE;
  if (exe) launchOpts.executablePath = exe;

  const browser = await chromium.launch(launchOpts);
  // Headed: null viewport follows the OS window. Headless: a stable size.
  const context = await browser.newContext({
    viewport: headless ? { width: 1280, height: 900 } : null,
  });
  const page = await context.newPage();
  return { chromium, browser, context, page };
}
