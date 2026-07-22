# SOX_AI_TOOL 사용자 매뉴얼

이 문서는 Apache NetBeans에서 SOX_AI_TOOL을 설치하고 Claude Code 또는 Codex CLI를 실행하는 방법을 설명합니다.

SOX_AI_TOOL은 NetBeans 안에서 AI 에이전트 CLI를 사용할 수 있게 해 주는 플러그인입니다. 플러그인 표시 이름은 **AI Agent Bridge for NetBeans**이며, 사용자 메뉴와 옵션에서는 **SOX_AI_TOOL** 이름을 사용합니다.

## 1. 준비 사항

설치 전에 다음 항목을 준비합니다.

- Apache NetBeans `RELEASE230` 호환 버전
- JDK 17 이상
- Claude Code CLI 또는 Codex CLI
- 플러그인 파일: `sox-ai-tool-<version>.nbm`

SOX_AI_TOOL은 Claude Code나 Codex를 포함하지 않습니다. 사용하려는 CLI는 별도로 설치되어 있어야 합니다.

CLI가 정상 설치되었는지 확인하려면 터미널에서 다음 명령 중 사용하는 도구를 실행합니다.

```powershell
claude --version
codex --version
```

명령이 인식되지 않으면 CLI 설치 경로를 NetBeans 옵션에서 직접 지정할 수 있습니다.

## 2. 플러그인 설치

1. NetBeans를 실행합니다.
2. 상단 메뉴에서 `Tools > Plugins`를 엽니다.
3. `Downloaded` 탭을 선택합니다.
4. `Add Plugins...`를 클릭합니다.
5. `sox-ai-tool-<version>.nbm` 파일을 선택합니다.
6. 설치 마법사의 안내에 따라 설치합니다.
7. NetBeans가 재시작을 요청하면 재시작합니다.

설치 후 `Tools > Options`에 `SOX_AI_TOOL` 항목이 보이면 설치가 완료된 것입니다. 툴바에도 `SOX_AI_TOOL` 실행 버튼이 표시됩니다.

## 3. 최초 설정

1. `Tools > Options > SOX_AI_TOOL`을 엽니다.
2. `General` 탭에서 `Agent CLI`를 선택합니다.
   - `Claude Code`: Claude Code CLI를 실행합니다.
   - `Codex`: Codex CLI를 실행합니다.
3. CLI 실행 파일 경로를 설정합니다.
   - CLI가 `PATH`에 있으면 비워 두어도 됩니다.
   - 직접 지정하려면 `Claude CLI path` 또는 `Codex CLI path`에서 `Browse...`를 눌러 실행 파일을 선택합니다.
4. 승인 없이 자동 진행하려면 `YOLO mode (auto accept for new agent sessions)`를 켭니다. Claude Code는 새 세션 시작 후 `Auto` 편집 모드로 전환하고, Codex는 새 세션을 `Auto Mode`로 시작합니다.
5. `MCP server port`를 확인합니다. 기본값을 그대로 사용해도 됩니다.
6. `Enable MCP integration`을 켜 둡니다. 이 옵션을 켜면 에이전트가 NetBeans의 열린 편집기, 진단 정보, 파일 열기, diff 열기 같은 IDE 도구를 사용할 수 있습니다.
7. 채팅창과 내장 터미널에서 한글이 깨지거나 네모로 보이지 않도록 `Terminal font`의 `Choose...`를 눌러 한글을 지원하는 고정폭 글꼴로 변경합니다. Windows에서는 `D2Coding`, `Consolas`, `Malgun Gothic` 등을 사용할 수 있습니다.
8. `OK` 또는 `Apply`를 눌러 저장합니다.

MCP 포트가 이미 사용 중이라는 알림이 나오면 `Tools > Options > SOX_AI_TOOL`에서 `MCP server port` 값을 다른 포트로 변경한 뒤 NetBeans를 재시작합니다.

## 4. 프로필 설정

여러 계정, API 키, 프록시, 모델 별칭을 나누어 쓰려면 `Profiles` 탭을 사용합니다.

1. `Tools > Options > SOX_AI_TOOL > Profiles`를 엽니다.
2. 기본 프로필을 수정하거나 `Add`로 새 프로필을 만듭니다.
3. 인증 방식을 선택합니다.
   - Claude 관리 인증: Claude CLI가 자체 로그인 상태를 사용합니다.
   - Anthropic 구독 토큰: Claude Code OAuth 토큰을 사용합니다.
4. 사내망이나 프록시 환경이면 `Proxy` 설정을 지정합니다.
5. 추가 환경 변수가 필요하면 `Extra env` 항목에 추가합니다.
6. 모델 별칭이 필요하면 `Model Aliases`에서 `sonnet`, `opus`, `haiku`, `custom` 등에 연결할 모델 ID를 지정합니다.
7. `Apply` 또는 `OK`를 눌러 저장합니다.

프로젝트마다 다른 프로필을 쓰려면 프로젝트를 우클릭하고 `Properties > SOX_AI_TOOL`에서 프로필을 선택합니다.

## 5. 세션 시작

세션은 두 가지 방법으로 시작할 수 있습니다.

- 툴바의 `SOX_AI_TOOL` 버튼 클릭
- 프로젝트 우클릭 후 `Open with SOX_AI_TOOL` 선택

세션 시작 화면에서 다음 항목을 설정합니다.

- `Agent`: 실행할 백엔드입니다. `Claude Code` 또는 `Codex`를 선택합니다.
- 작업 디렉터리: 에이전트가 실행될 프로젝트 또는 폴더입니다.
- `Profile`: 사용할 인증/환경 프로필입니다.
- `Extra args`: CLI에 추가로 전달할 인자입니다.
- 세션 모드: 새 세션 또는 이전 세션 재개 방식을 선택합니다.

설정을 마친 뒤 `Open`을 누르면 NetBeans 도구 창 안에 내장 터미널이 열리고 선택한 CLI가 실행됩니다.

## 6. 세션 모드

Claude Code에서 사용할 수 있는 세션 모드는 다음과 같습니다.

- `New session`: 새 세션을 시작합니다.
- `Continue last`: 가장 최근 세션을 이어서 실행합니다.
- `Resume specific`: 저장된 세션 목록에서 특정 세션을 선택해 이어서 실행합니다.

Codex에서 사용할 수 있는 세션 모드는 다음과 같습니다.

- `New Codex session`: 새 Codex 세션을 시작합니다.
- `Resume last Codex session`: `codex resume --last` 방식으로 최근 세션을 이어서 실행합니다.
- `Choose Codex session...`: Codex의 세션 선택 흐름을 엽니다.

실행 중인 세션을 닫거나 다른 세션으로 전환하려면 프롬프트 입력창의 우클릭 메뉴에서 `Start New Session` 또는 `Switch to Session...`을 사용할 수 있습니다.

## 7. Codex 실행 모드

Codex를 선택하면 `Codex mode`를 고를 수 있습니다.

- `YOLO mode (auto accept for new agent sessions)`: 설정 화면에서 켜면 새 Codex 세션 기본값을 `Auto Mode`로 저장합니다. 끄면 `Auto Mode`였던 기본값을 `Ask on Command`로 되돌립니다.
- `Plan Mode`: 읽기 전용 샌드박스에서 실행하며 필요할 때 승인 요청을 합니다.
- `Ask on Command`: 작업공간 쓰기 권한을 주되 필요할 때 승인 요청을 합니다.
- `Auto Mode`: 작업공간 쓰기 권한으로 실행하고 승인 요청 없이 진행합니다.
- `Full Auto`: 전체 파일 접근 권한으로 실행하고 승인 요청 없이 진행합니다.
- `Bypass Approvals + Sandbox`: Codex의 승인과 샌드박스를 우회하는 플래그를 사용합니다.

파일 변경 가능성이 있는 작업은 처음에는 `Plan Mode` 또는 `Ask on Command`로 시작하는 것이 안전합니다.

## 8. Claude Code 편집 모드

Claude Code 세션에서는 상단의 모드 콤보 또는 Claude Code TUI의 `Shift+Tab` 흐름으로 편집/권한 모드를 전환할 수 있습니다.

- `Plan`: 계획 중심 모드입니다. 파일 변경을 자동 허용하지 않습니다.
- `Default`: 파일 변경 전에 diff 승인 창을 표시합니다.
- `Accept Edits`: 세션 작업 디렉터리 안의 변경을 자동 허용합니다.
- `Auto`: Claude Code가 안전하다고 판단한 작업을 자동 승인합니다.
- `Bypass Permissions`: 권한 확인을 우회합니다.

표시되는 모드는 설치된 Claude Code 버전과 실행 방식에 따라 달라질 수 있습니다.

`YOLO mode (auto accept for new agent sessions)`를 켜면 Claude Code 새 세션이 준비된 뒤 플러그인이 `Auto` 모드로 한 번 전환합니다. `Auto` 모드를 지원하지 않는 Claude Code 버전이나 플랜에서는 가능한 대체 모드로 전환될 수 있습니다.

## 9. 프롬프트 입력

세션 하단 입력창에 요청을 작성한 뒤 `Send`를 누릅니다.

한글 프롬프트를 입력할 때 글자가 깨지거나 일부 문자가 네모로 보이면 `Tools > Options > SOX_AI_TOOL > General > Terminal font`에서 한글 지원 글꼴로 변경합니다. 채팅창과 터미널 출력은 이 폰트 설정의 영향을 받습니다.

주요 버튼은 다음과 같습니다.

- `Send`: 입력한 프롬프트를 에이전트에 보냅니다.
- `Cancel`: 현재 입력 또는 진행 중인 요청을 취소합니다.
- `Attach`: 파일을 선택해 `@path` 참조로 입력창에 추가합니다.
- `History`: 이전 프롬프트를 다시 불러옵니다.
- `Favorites`: 자주 쓰는 프롬프트를 저장하고 재사용합니다.

입력창에서 `@`를 입력하면 작업 디렉터리 기준 파일/폴더 자동완성이 표시됩니다.

- `Up` / `Down`: 항목 이동
- `Enter` / `Tab`: 선택한 폴더로 이동하거나 파일 참조 확정
- `Space`: 선택한 항목을 `@path ` 형태로 확정
- `Esc`: 자동완성 닫기

파일을 드래그 앤 드롭하거나 클립보드의 이미지를 붙여넣어 파일 참조로 추가할 수도 있습니다.

## 10. Diff 승인과 파일 변경 검토

에이전트가 파일을 수정하려고 하면 설정과 실행 모드에 따라 NetBeans 안에 diff 화면이 열립니다.

diff 화면에서는 변경 전후를 확인한 뒤 다음 작업을 선택합니다.

- 승인: 변경을 적용합니다.
- 거절: 변경을 적용하지 않습니다. 필요하면 이유를 에이전트에 전달합니다.
- 취소: 변경을 거절하고 현재 에이전트 작업을 중단합니다.

`Tools > Options > SOX_AI_TOOL > General`에서 file diff를 별도 탭으로 열지, 세션 안에 표시할지 설정할 수 있습니다. 별도 탭으로 열 경우 dock 위치도 선택할 수 있습니다.

## 11. 마크다운 미리보기

SOX_AI_TOOL은 마크다운 파일과 계획 파일을 NetBeans 안에서 렌더링해 보여 줄 수 있습니다.

주요 기능은 다음과 같습니다.

- 마크다운 렌더링 미리보기
- 찾기 바
- 확대/축소
- diff 화면 안의 마크다운 미리보기
- 계획 파일 자동 미리보기

관련 옵션은 `Tools > Options > SOX_AI_TOOL > General`에서 설정합니다.

- `Markdown preview in diff`: diff 안에서 마크다운 미리보기를 표시할 조건
- `Auto-open plan preview for plans/*.md`: 계획 파일 자동 미리보기
- `Markdown preview dock position`: 마크다운 미리보기 탭 위치

## 12. 프로젝트별 설정

프로젝트마다 다른 프로필을 사용하려면 다음 순서로 설정합니다.

1. NetBeans `Projects` 창에서 프로젝트를 우클릭합니다.
2. `Properties`를 엽니다.
3. `SOX_AI_TOOL` 항목을 선택합니다.
4. `Profile`에서 사용할 프로필을 선택합니다.
5. 저장합니다.

이후 해당 프로젝트에서 `Open with SOX_AI_TOOL`을 실행하면 선택한 프로필이 기본값으로 사용됩니다.

## 13. 권장 사용 흐름

처음 사용하는 프로젝트에서는 다음 흐름을 권장합니다.

1. `Tools > Options > SOX_AI_TOOL`에서 사용할 CLI와 경로를 설정합니다.
2. 프로젝트 우클릭 후 `Open with SOX_AI_TOOL`을 선택합니다.
3. 첫 실행은 Claude Code의 `Plan` 또는 Codex의 `Plan Mode`로 시작합니다.
4. 에이전트에게 작업 범위와 변경할 파일을 먼저 설명하게 합니다.
5. 변경이 필요하면 diff를 확인하고 승인합니다.
6. 작업이 끝나면 NetBeans에서 빌드 또는 테스트를 실행해 결과를 검증합니다.

## 14. 문제 해결

### SOX_AI_TOOL 메뉴나 옵션이 보이지 않음

- 플러그인 설치가 완료되었는지 `Tools > Plugins > Installed`에서 확인합니다.
- NetBeans를 재시작합니다.
- 설치한 `.nbm` 파일이 현재 NetBeans 버전과 호환되는지 확인합니다.

### CLI 실행 실패

- `Tools > Options > SOX_AI_TOOL > General`에서 CLI 경로를 확인합니다.
- 터미널에서 `claude --version` 또는 `codex --version`이 실행되는지 확인합니다.
- CLI가 `PATH`에 없으면 실행 파일 경로를 직접 지정합니다.

### MCP 포트가 사용 중이라고 나옴

- `Tools > Options > SOX_AI_TOOL > General`에서 `MCP server port`를 다른 값으로 변경합니다.
- NetBeans를 재시작합니다.

### 에이전트가 NetBeans 파일이나 진단 정보를 못 읽음

- `Enable MCP integration`이 켜져 있는지 확인합니다.
- MCP 포트 충돌 알림이 있었는지 확인합니다.
- 세션을 닫고 새로 시작합니다.

### 파일 변경이 자동으로 적용되어 불안함

- Claude Code는 `Plan` 또는 `Default` 모드를 사용합니다.
- Codex는 `Plan Mode` 또는 `Ask on Command`를 사용합니다.
- `Full Auto`, `Bypass Approvals + Sandbox`, `Bypass Permissions`는 신뢰할 수 있는 작업에서만 사용합니다.

### 화면 글꼴이 깨지거나 작게 보임

- `Tools > Options > SOX_AI_TOOL > General`에서 `Terminal font`의 `Choose...`를 누릅니다.
- 한글 채팅 입력과 터미널 출력을 위해 한글을 지원하는 고정폭 글꼴과 적절한 크기를 선택합니다.
- Windows에서는 `D2Coding`, `Consolas`, `Malgun Gothic` 등을 사용할 수 있습니다.

## 15. 제거

1. NetBeans에서 `Tools > Plugins`를 엽니다.
2. `Installed` 탭에서 `AI Agent Bridge for NetBeans` 또는 `SOX_AI_TOOL` 관련 항목을 찾습니다.
3. `Uninstall`을 선택합니다.
4. NetBeans를 재시작합니다.

플러그인을 제거해도 Claude Code CLI, Codex CLI, 각 CLI의 로컬 설정과 세션 파일은 별도로 삭제되지 않습니다.
