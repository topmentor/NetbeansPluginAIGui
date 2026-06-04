# SOX_AI_TOOL

SOX_AI_TOOL은 Apache NetBeans 안에서 AI 에이전트 CLI를 사용할 수 있게 해 주는 플러그인입니다. 현재 Claude Code와 Codex를 지원하며, 내장 터미널, NetBeans 연동 MCP 도구, 마크다운 미리보기, 파일 diff 검토 기능을 제공합니다.

플러그인의 패키지 표시 이름은 **AI Agent Bridge for NetBeans**이며, 제작사/개발자 표기는 **S.O.X**입니다.

## 주요 기능

- NetBeans 도구 창 안의 내장 터미널(JediTerm + PTY)에서 Claude Code 또는 Codex 실행
- 플러그인 옵션에서 사용할 에이전트 백엔드 선택 (Claude / Codex)
- Claude Code와 Codex 실행 파일 경로를 각각 설정
- 새 세션 시작 또는 이전 에이전트 세션 이어서 실행, 저장된 세션 관리
- Codex 전용 실행 모드 제공: Plan Mode, Ask on Command, Auto Mode, Full Auto, Bypass
- MCP SSE 서버를 통해 NetBeans 컨텍스트를 에이전트에 제공
- 에이전트가 열린 편집기, 진단 정보, 프로젝트, 파일 정보를 조회할 수 있는 MCP 도구 제공
- 채팅 입력창에서 파일을 선택해 `@path` 참조로 첨부
- NetBeans 내부에서 파일 diff를 열고 변경 사항 승인, 거절, 취소
- 마크다운 및 계획 파일을 렌더링된 형태로 미리보기 (찾기 바, 확대/축소 지원)
- OpenAI 호환 프록시: Anthropic Messages API 요청을 OpenAI Chat Completions 형식으로 변환해, OpenAI 호환 엔드포인트를 백엔드로 사용 (스트리밍/비스트리밍 모두 지원)
- 여러 Claude 프로필과 모델 별칭(model alias)을 정의하고 전환
- 프롬프트 즐겨찾기와 입력 히스토리 저장 및 재사용
- 툴바 또는 프로젝트 우클릭 메뉴에서 SOX_AI_TOOL 실행
- 전역 설정: `Tools > Options > SOX_AI_TOOL`
- 프로젝트별 설정: `Project Properties > SOX_AI_TOOL`

## 요구사항

- `RELEASE230`과 호환되는 Apache NetBeans
- JDK 17 이상
- Maven
- Windows에서 제공된 빌드 스크립트를 사용할 경우 PowerShell
- 로컬에 설치된 Claude Code CLI 또는 Codex CLI

이 플러그인은 로컬 CLI 도구를 실행합니다. Claude Code나 Codex 자체를 플러그인에 포함하지 않습니다.

## 빌드

저장소 루트에서 다음 명령을 실행합니다.

```powershell
powershell -ExecutionPolicy Bypass -File build-scripts\build.ps1 -Clean
```

이 스크립트는 내부적으로 다음 Maven 명령을 실행합니다.

```powershell
mvn clean verify
```

테스트를 건너뛰고 빌드하려면 다음 명령을 사용합니다.

```powershell
powershell -ExecutionPolicy Bypass -File build-scripts\build.ps1 -Clean -SkipTests
```

## 패키징

NetBeans 모듈 패키지인 `.nbm` 파일을 만들고 `dist` 폴더로 복사하려면 다음 명령을 실행합니다.

```powershell
powershell -ExecutionPolicy Bypass -File build-scripts\package.ps1 -Clean
```

생성된 `.nbm` 파일은 `target` 아래에 만들어지고, 패키지 스크립트 실행 시 `dist`로 복사됩니다.

현재 결과 파일 이름은 `pom.xml`의 `artifactId`와 `version`을 기준으로 정해집니다. 현재 `artifactId`는 `sox-ai-tool`, `version`은 `1.0`이므로 결과 파일은 다음과 같습니다.

```text
sox-ai-tool-<version>.nbm   예) sox-ai-tool-1.0.nbm
```

## 설치

1. 빌드 또는 패키징을 실행해 `.nbm` 파일을 생성합니다.
2. NetBeans를 엽니다.
3. `Tools > Plugins > Downloaded`로 이동합니다.
4. `Add Plugins...`를 클릭합니다.
5. 생성된 `.nbm` 파일을 선택합니다.
6. 설치를 진행하고, 필요하면 NetBeans를 재시작합니다.

## 기본 사용법

설치 후 다음 순서로 설정합니다.

1. `Tools > Options > SOX_AI_TOOL`을 엽니다.
2. 사용할 에이전트 백엔드를 선택합니다: Claude 또는 Codex.
3. CLI가 `PATH`에 없다면 실행 파일 경로를 지정합니다.
4. MCP, 터미널, diff, 마크다운, 세션 관련 옵션을 필요에 맞게 설정합니다.
5. SOX_AI_TOOL 툴바 버튼 또는 프로젝트 우클릭 메뉴에서 세션을 엽니다.

에이전트 백엔드에 따라 GUI에 표시되는 모드가 달라집니다. Claude를 선택하면 Claude Code의 편집/권한 모드가, Codex를 선택하면 Codex 전용 샌드박스·승인 모드가 표시됩니다.

## 채팅 입력과 파일 참조

채팅 입력창에서는 파일이나 폴더를 `@path` 형태로 참조할 수 있습니다.

- `Attach` 버튼: 파일 선택 창에서 하나 이상의 파일을 골라 `@path` 참조로 입력창에 삽입
- 파일 드래그 앤 드롭 또는 클립보드 이미지 붙여넣기: 입력창에 파일 참조로 삽입
- `@` 입력: 작업 디렉터리 기준 파일/폴더 자동완성 팝업 표시
- 자동완성 팝업에서 `Up`/`Down`: 항목 이동
- 자동완성 팝업에서 `Enter` 또는 `Tab`: 선택한 폴더 안으로 이동하거나 파일 참조 확정
- 자동완성 팝업에서 `Space`: 선택한 항목을 그대로 `@path ` 참조로 확정하고 팝업 닫기
- 자동완성 팝업에서 `Esc`: 팝업 닫기

## Claude Code 실행 모드

Claude Code 세션에서는 다음 편집/권한 모드를 사용하며, 터미널(TUI) 안에서 `Shift+Tab`으로 순환합니다. 플러그인은 현재 화면 상태에서 활성 모드를 감지해 GUI에 반영합니다. 모드는 권한이 약한 순서에서 강한 순서로 나열됩니다.

- `Plan`: 계획만 제안하고 파일 변경을 자동 허용하지 않음 (읽기 전용)
- `Default`: 모든 파일 변경 전에 diff 승인 창을 표시 (ask-on-edit)
- `Accept Edits`: 세션 작업 디렉터리 안의 파일 변경은 자동 허용, 그 밖의 파일은 diff 승인 창 표시
- `Auto`: 분류기가 안전한 도구 호출과 파일 편집을 자동 승인하고 위험한 작업(대량 삭제, 데이터 유출 등)만 차단 (Max/Team/Enterprise/API 플랜, Claude Code 2.1.83+)
- `Bypass Permissions`: 위치와 무관하게 모든 파일 변경을 자동 허용 (Claude를 `--dangerously-skip-permissions`로 실행했을 때 활성화)

각 모드는 `model/EditMode.java`에 정의되어 있고, 화면 텍스트로부터의 모드 감지는 `process/ScreenContentDetector.java`가 담당합니다.

또한 세션 시작 방식(`model/SessionMode.java`)을 선택할 수 있습니다.

- `New session`: 새 세션 시작
- `Continue last`: 가장 최근 세션 이어서 실행 (`--continue`)
- `Resume specific`: 저장된 세션 목록에서 특정 세션 선택 후 재개 (`--resume <id>`)

## Codex 실행 모드

현재 Codex 프리셋은 다음과 같습니다.

- `Plan Mode`: 읽기 전용 샌드박스, 필요 시 승인 요청
- `Ask on Command`: 작업공간 쓰기 샌드박스, 필요 시 승인 요청
- `Auto Mode`: 작업공간 쓰기 샌드박스, 승인 요청 없음
- `Full Auto`: 전체 접근 샌드박스, 승인 요청 없음
- `Bypass`: Codex의 승인 및 샌드박스 우회 플래그 사용

각 모드는 `CodexRunMode.java`에서 Codex CLI 플래그로 매핑됩니다.

## 프로젝트 구조

```text
build-scripts/       빌드 및 패키징 스크립트
src/main/java/       NetBeans 플러그인 Java 소스
src/main/resources/  NetBeans layer, bundle, 아이콘, 서비스 리소스
pom.xml              Maven/NBM 빌드 설정
AGENTS.md            이후 AI 에이전트 작업을 위한 프로젝트 사전정보
```

자바 소스의 메인 패키지는 `io.github.nbplugins.claudecodegui`이며, `org.openbeans.claude.netbeans`에 레거시 호환 패키지가 남아 있습니다.

주요 구현 위치:

- `settings/AgentBackend.java`: Claude와 Codex 백엔드 선택
- `settings/CodexRunMode.java`: Codex 샌드박스 및 승인 모드 프리셋
- `settings/ClaudeCodePreferences.java`: 플러그인 설정 저장소
- `settings/ClaudeProfile*.java`, `settings/ModelAlias*.java`, `settings/ProxyConfiguration.java`: 프로필, 모델 별칭, 프록시 설정
- `process/ClaudeProcess.java`: CLI 프로세스 실행 및 인자 구성
- `controller/ClaudeSessionController.java`: 세션 수명주기 제어
- `ui/ClaudeSessionTab.java`: 메인 세션 창
- `ui/ClaudeSessionSelectorPanel.java`: 세션 시작 UI
- `ui/SessionModePanel.java`: 백엔드별 세션 모드 라벨
- `ui/markdown/`, `ui/MarkdownPreviewTab.java`, `ui/MarkdownDiffPanel.java`: 마크다운 렌더링 및 diff 보기
- `mcp/`: MCP SSE 서버 및 NetBeans 도구 브리지 (`mcp/tools/` 아래 개별 도구)
- `openaiproxy/`: Anthropic ↔ OpenAI API 변환 프록시 서블릿
- `model/`: 세션, 프롬프트 히스토리/즐겨찾기 등 데이터 모델

## 검증 빌드

현재 이 저장소의 단위 테스트 코드는 제거되어 있습니다. 빌드 검증은 Maven/NBM 패키징이 정상적으로 완료되는지 확인하는 방식으로 진행합니다.

전체 검증 빌드:

```powershell
mvn verify
```

Windows 빌드 스크립트:

```powershell
powershell -ExecutionPolicy Bypass -File build-scripts\build.ps1 -Clean
```

## 유지보수 참고

이 프로젝트는 처음에 Claude Code 전용 플러그인으로 시작했기 때문에 내부 클래스명과 패키지명에 `Claude`가 많이 남아 있습니다. 패키지, 클래스, 설정 키, NetBeans layer 등록, 마이그레이션 코드가 서로 연결되어 있으므로 단순 문자열 치환으로 이름을 바꾸지 않는 것이 좋습니다.

사용자에게 보이는 이름은 `SOX_AI_TOOL` 또는 에이전트 중립적인 표현을 사용합니다. Maven 및 플러그인 표시 메타데이터는 `AI Agent Bridge for NetBeans`, 제작사/개발자 표기는 `S.O.X`를 사용합니다.

플러그인 아이콘 리소스 위치:

```text
src/main/resources/io/github/nbplugins/claudecodegui/icons/
```

현재 아이콘은 Maven 빌드 중 SVG에서 생성하지 않고, 정적 PNG/ICO 리소스로 관리합니다.

## 라이선스

Apache License, Version 2.0.
