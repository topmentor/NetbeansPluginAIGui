// Reusable Playwright launch helper for the "playwright-ui" agent skill.
//
// Load Playwright from a resolved package path (bundled NetBeans driver or a
// local npm install) and launch Chromium. Config comes from arguments or the
// PW_PACKAGE / PW_BROWSER_EXE environment variables that ensure-playwright.ps1
// feeds in, so the same code works for both the bundled and npm runtimes.
//
// Example (agent writes a one-off automation):
//   import { launchBrowser } from './helper.mjs';
//   const { browser, page } = await launchBrowser({ headless: false });
//   try { await page.goto(url); /* ... */ } finally { await browser.close(); }

import { createRequire } from 'node:module';

const require = createRequire(import.meta.url);

/** Require the Playwright module from an explicit package path or PW_PACKAGE. */
export function loadPlaywright(packagePath) {
  const pkg = packagePath || process.env.PW_PACKAGE;
  if (!pkg) {
    throw new Error('Playwright package path missing. Pass packagePath or set PW_PACKAGE.');
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
  const { chromium } = loadPlaywright(packagePath);

  const launchOpts = {
    headless,
    args: args || (headless ? [] : ['--start-maximized']),
  };
  const exe = browserExecutable || process.env.PW_BROWSER_EXE;
  if (exe) launchOpts.executablePath = exe;

  const browser = await chromium.launch(launchOpts);
  // Headed: null viewport follows the OS window. Headless: a stable size.
  const context = await browser.newContext({
    viewport: headless ? { width: 1280, height: 900 } : null,
  });
  const page = await context.newPage();
  return { chromium, browser, context, page };
}
