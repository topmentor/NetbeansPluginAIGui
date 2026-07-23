---
name: playwright-ui
description: 로컬 Playwright 실행환경(NetBeans 번들 드라이버 또는 로컬 npm 설치)을 자동으로 감지·설치한 뒤, 에이전트(Claude Code / Codex)가 로컬 웹 UI를 점검·조작할 수 있도록 화면이 보이는 Chromium을 실행한다. 사용자가 Playwright로 화면 열기, 버튼 클릭, UI 문제 재현, 동작 검증을 요청하거나 NetBeans Chromium Web Preview 환경을 사용해야 할 때 적용한다.
---

# Playwright UI

로컬 Playwright 실행환경을 자동으로 확인·준비한 뒤, 요청받은 UI 작업을 수행할 별도의 Chromium 브라우저를 화면에 표시하여 실행한다. 이 문서는 하네스 중립 참조 문서이며, 실제 로직은 저장소의 `tools/playwright/` 스크립트에 있다. Claude Code는 `.claude/skills/playwright-ui/SKILL.md`로도 이 스킬을 자동 인식한다.

## 구성 요소

- `tools/playwright/ensure-playwright.ps1` — 실행환경 감지 + 자동 설치. **번들 우선 → npm 폴백** 정책으로 동작하고, 결과 설정을 JSON으로 STDOUT에 출력한다(진행 로그는 STDERR).
- `tools/playwright/playwright-healthcheck.mjs` — URL을 열어 HTTP 상태·제목·브라우저 버전·콘솔/페이지 오류·스크린샷을 JSON으로 보고하고 브라우저를 닫는 실행 검사기.
- `tools/playwright/helper.mjs` — 작업별 자동화를 작성할 때 재사용하는 `launchBrowser()` 모듈.

## 1. 실행환경 준비 (감지 + 자동 설치)

PowerShell에서 실행한다.

```powershell
$cfg = & powershell -ExecutionPolicy Bypass -File tools\playwright\ensure-playwright.ps1 | ConvertFrom-Json
```

동작 순서:

1. NetBeans 번들 드라이버를 찾는다. 알려진 경로(`%LOCALAPPDATA%\NetBeans\Cache\28\chromium-web-preview\playwright-driver-1.57.0-win32_x64`)를 먼저 보고, 없으면 캐시에서 최신 `playwright-driver-*-win32_x64` 폴더를 검색한다. `node.exe`, `package/index.js`, `package/package.json`, `package/browsers.json`, 그리고 `browsers.json`의 chromium revision에 해당하는 `chrome.exe`가 모두 있으면 `mode:"bundled"` 설정을 반환한다. 이 프로젝트에서 검증된 환경은 Playwright Core 1.57, Chromium revision 1200이다.
2. 번들이 없거나 불완전하면 npm 폴백으로 전환한다. 시스템 `node`/`npm`을 확인하고 `tools/playwright/.pw-runtime`에 `playwright`를 설치한 뒤 `playwright install chromium`으로 Chromium을 내려받아 `mode:"npm"` 설정을 반환한다. 이미 설치돼 있으면 건너뛴다.
3. 준비에 실패하면 `mode:"error"`와 이유를 반환한다(예: Node.js 미설치). 이 경우 사용자에게 차이를 보고하고 임의로 소프트웨어를 바꾸지 않는다.

반환 필드: `mode`, `node`, `playwrightPackage`, `browserExecutable`, `coreVersion`, `chromiumRevision`.

> 자동 설치는 실행환경이 **실제로 없을 때만** 일어난다. 이미 존재하는 번들 버전을 사용자 요청 없이 다른 버전으로 다운로드·교체하지 않는다.

## 2. 실행 검사

본격 작업 전 반드시 한 번 실행 검사를 수행한다.

```powershell
$env:PW_PACKAGE     = $cfg.playwrightPackage
$env:PW_BROWSER_EXE = $cfg.browserExecutable
& $cfg.node tools\playwright\playwright-healthcheck.mjs http://localhost:8080      # 자동 검증 시 --headless 추가
```

결과 JSON(`ok`, `httpStatus`, `title`, `browserVersion`, `screenshot`, `consoleMessages`, `pageErrors`)을 확인한다. 스크린샷은 `tools/playwright/.artifacts/`에 저장된다.

## 3. 브라우저 실행 및 UI 검증

세부 조작은 `helper.mjs`의 `launchBrowser()`로 작성한다. `PW_PACKAGE`, `PW_BROWSER_EXE`가 설정돼 있으면 번들/npm 어느 런타임이든 동일하게 동작한다.

```javascript
import { launchBrowser } from './tools/playwright/helper.mjs';
const { browser, context, page } = await launchBrowser({ headless: false });
try {
  await page.goto(targetUrl, { waitUntil: 'load' });
  // 역할·레이블·안정적인 ID·카드 제목 기준으로 요소를 찾아 조작한다.
} finally {
  await browser.close();
}
```

- 로그인이나 사용자 확인이 필요하면 화면이 보이는 브라우저를 사용한다. NetBeans가 `remote-debugging-pipe`로 실행한 기존 프로세스에 연결하지 말고 별도의 화면 표시 브라우저를 실행한다.
- 비밀번호는 사용자가 열린 브라우저에 직접 입력하게 한다. 비밀번호를 요청·열람·출력·저장하지 않는다.
- Console 오류, `pageerror`, 관련 요청/응답, 최종 DOM 상태, 스크린샷을 수집한다.
- 버튼 클릭이나 HTTP 200만으로 성공 처리하지 말고 애플리케이션의 실제 완료 상태까지 기다린다.
- 로그인 대기나 장시간 작업 중에는 현재 상태를 사용자에게 알린다.

## 안전 규칙

- 삭제·비우기·재생성·제출·게시 등 중요한 데이터를 변경하는 UI 동작은 사용자의 명시적 승인을 받은 뒤 수행한다.
- 파괴적 작업 전에는 정확한 대상을 알리고, 완료 후 저장된 결과를 독립적으로 검증한다.
- 성공하거나 실패하면 `finally`에서 브라우저를 종료한다.
- 사용자의 일반 브라우저 프로필에서 쿠키를 복사하거나 재사용하지 않는다.

## 결과 보고

`mode`(bundled/npm), Playwright Core 버전, Chromium revision과 실행 버전, 대상 URL, 수행 동작, 요청·응답 근거, Console/Page 오류, 완료 결과, 스크린샷 경로, 데이터 변경 여부를 보고한다.
