package io.github.nbplugins.claudecodegui.settings;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import org.openbeans.claude.netbeans.ClaudeCodeStatusService;
import org.openide.modules.Places;
import org.openide.util.Lookup;

/**
 * Options tab for detecting and validating the local Playwright runtime used by
 * the Playwright UI helper scripts.
 */
final class PlaywrightSettingsPanel extends JPanel {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final JCheckBox forceNpmCheck = new JCheckBox(
            "Use local npm runtime instead of NetBeans bundle");
    private final JTextField scriptDirField = textField();
    private final JTextField modeField = textField();
    private final JTextField nodeField = textField();
    private final JTextField playwrightField = textField();
    private final JTextField browserField = textField();
    private final JTextField versionField = textField();
    private final JTextField netBeansField = textField();
    private final JTextArea resultArea = new JTextArea(8, 80);
    private final JButton detectButton = new JButton("Search Playwright Runtime");
    private final JButton healthButton = new JButton("Run Playwright + NetBeans Check");

    private JsonNode lastConfig;

    PlaywrightSettingsPanel() {
        super(new BorderLayout());
        initUi();
    }

    void load() {
        forceNpmCheck.setSelected(ClaudeCodePreferences.isPlaywrightForceNpm());
        updateScriptLocation();
        updateNetBeansStatus();
    }

    void store() {
        ClaudeCodePreferences.setPlaywrightForceNpm(forceNpmCheck.isSelected());
    }

    private void initUi() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        int row = 0;

        forceNpmCheck.setToolTipText(
                "Sets SOX_PLAYWRIGHT_FORCE_NPM=1 for new Claude Code/Codex sessions");
        GridBagConstraints forceGbc = gbc(0, row, 3, true);
        form.add(forceNpmCheck, forceGbc);
        row++;

        form.add(new JLabel("Script location:"), gbc(0, row, 1, false));
        form.add(scriptDirField, fill(1, row));
        detectButton.addActionListener(e -> detectRuntime());
        form.add(detectButton, gbc(2, row, 1, false));
        row++;

        form.add(new JLabel("Runtime:"), gbc(0, row, 1, false));
        form.add(modeField, fill(1, row));
        row++;

        form.add(new JLabel("Node path:"), gbc(0, row, 1, false));
        form.add(nodeField, fill(1, row));
        row++;

        form.add(new JLabel("Playwright path:"), gbc(0, row, 1, false));
        form.add(playwrightField, fill(1, row));
        row++;

        form.add(new JLabel("Chromium path:"), gbc(0, row, 1, false));
        form.add(browserField, fill(1, row));
        row++;

        form.add(new JLabel("Version:"), gbc(0, row, 1, false));
        form.add(versionField, fill(1, row));
        row++;

        form.add(new JLabel("NetBeans connection:"), gbc(0, row, 1, false));
        form.add(netBeansField, fill(1, row));
        healthButton.addActionListener(e -> runHealthCheck());
        form.add(healthButton, gbc(2, row, 1, false));
        row++;

        resultArea.setEditable(false);
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(resultArea);
        GridBagConstraints resultGbc = gbc(0, row, 3, true);
        resultGbc.fill = GridBagConstraints.BOTH;
        resultGbc.weightx = 1.0;
        resultGbc.weighty = 1.0;
        form.add(scroll, resultGbc);

        add(form, BorderLayout.CENTER);
    }

    private void detectRuntime() {
        setBusy(true);
        resultArea.setText("Searching Playwright runtime...");
        new SwingWorker<JsonNode, Void>() {
            @Override
            protected JsonNode doInBackground() throws Exception {
                Path scriptDir = resolvePlaywrightScriptDir();
                JsonNode cfg = runEnsure(scriptDir);
                if (cfg instanceof ObjectNode object) {
                    object.put("scriptLocation", scriptDir.toAbsolutePath().toString());
                }
                return cfg;
            }

            @Override
            protected void done() {
                try {
                    lastConfig = get();
                    applyConfig(lastConfig);
                    resultArea.setText(MAPPER.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(lastConfig));
                } catch (Exception ex) {
                    showError("Playwright runtime search failed", ex);
                } finally {
                    updateNetBeansStatus();
                    setBusy(false);
                }
            }
        }.execute();
    }

    private void runHealthCheck() {
        setBusy(true);
        resultArea.setText("Running Playwright health check...");
        new SwingWorker<JsonNode, Void>() {
            @Override
            protected JsonNode doInBackground() throws Exception {
                Path scriptDir = resolvePlaywrightScriptDir();
                JsonNode cfg = lastConfig != null ? lastConfig : runEnsure(scriptDir);
                return runHealth(scriptDir, cfg);
            }

            @Override
            protected void done() {
                try {
                    JsonNode health = get();
                    resultArea.setText(MAPPER.writerWithDefaultPrettyPrinter()
                            .writeValueAsString(health));
                } catch (Exception ex) {
                    showError("Playwright health check failed", ex);
                } finally {
                    updateNetBeansStatus();
                    setBusy(false);
                }
            }
        }.execute();
    }

    private JsonNode runEnsure(Path scriptDir) throws IOException, InterruptedException {
        List<String> cmd = new ArrayList<>(List.of(
                "powershell", "-ExecutionPolicy", "Bypass",
                "-File", scriptDir.resolve("ensure-playwright.ps1").toString(),
                "-Quiet"));
        if (forceNpmCheck.isSelected()) {
            cmd.add("-ForceNpm");
        }
        ProcessResult result = run(cmd, scriptDir.getParent(), Map.of(
                "SOX_PLAYWRIGHT_FORCE_NPM", forceNpmCheck.isSelected() ? "1" : ""));
        JsonNode json = MAPPER.readTree(result.stdout());
        if (result.exitCode() != 0 || "error".equals(json.path("mode").asText())) {
            throw new IOException(json.path("error").asText(result.stderr()));
        }
        return json;
    }

    private JsonNode runHealth(Path scriptDir, JsonNode cfg) throws IOException, InterruptedException {
        String node = cfg.path("node").asText("node");
        List<String> cmd = List.of(node,
                scriptDir.resolve("playwright-healthcheck.mjs").toString(),
                "--headless", "--timeout", "10000");
        ProcessResult result = run(cmd, scriptDir.getParent(), Map.of(
                "PW_PACKAGE", cfg.path("playwrightPackage").asText(""),
                "PW_BROWSER_EXE", cfg.path("browserExecutable").asText(""),
                "SOX_PLAYWRIGHT_FORCE_NPM", forceNpmCheck.isSelected() ? "1" : ""));
        JsonNode json = MAPPER.readTree(result.stdout());
        if (result.exitCode() != 0 || !json.path("ok").asBoolean(false)) {
            throw new IOException(json.path("error").asText(result.stderr()));
        }
        return json;
    }

    private static ProcessResult run(List<String> cmd, Path dir, Map<String, String> env)
            throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (dir != null) {
            pb.directory(dir.toFile());
        }
        pb.environment().putAll(env);
        Process process = pb.start();
        boolean done = process.waitFor(Duration.ofSeconds(90).toMillis(),
                java.util.concurrent.TimeUnit.MILLISECONDS);
        if (!done) {
            process.destroyForcibly();
            throw new IOException("Process timed out: " + String.join(" ", cmd));
        }
        String stdout = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        String stderr = new String(process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8);
        return new ProcessResult(process.exitValue(), stdout, stderr);
    }

    private void applyConfig(JsonNode cfg) {
        modeField.setText(switch (cfg.path("mode").asText("")) {
            case "bundled" -> "NetBeans bundled driver";
            case "npm" -> "Local npm install";
            default -> cfg.path("mode").asText("(unknown)");
        });
        nodeField.setText(cfg.path("node").asText(""));
        playwrightField.setText(cfg.path("playwrightPackage").asText(""));
        browserField.setText(cfg.path("browserExecutable").asText(""));
        String scriptLocation = cfg.path("scriptLocation").asText("");
        if (!scriptLocation.isBlank()) {
            scriptDirField.setText(scriptLocation);
        }
        String version = cfg.path("coreVersion").asText("");
        String rev = cfg.path("chromiumRevision").asText("");
        versionField.setText(rev.isBlank() || "null".equals(rev)
                ? version
                : version + " / Chromium revision " + rev);
    }

    private void updateScriptLocation() {
        Path scriptDir = findSourcePlaywrightScriptDir();
        scriptDirField.setText(scriptDir != null
                ? scriptDir.toAbsolutePath().toString()
                : "Bundled in plugin; extracted to NetBeans cache when searched");
    }

    private void updateNetBeansStatus() {
        ClaudeCodeStatusService status = Lookup.getDefault().lookup(ClaudeCodeStatusService.class);
        if (status == null) {
            netBeansField.setText("Unavailable: status service not registered");
        } else if (status.isServerRunning()) {
            netBeansField.setText("Connected: MCP server running on port " + status.getServerPort());
        } else {
            netBeansField.setText("Not connected: MCP server is not running");
        }
    }

    private static Path resolvePlaywrightScriptDir() throws IOException {
        Path sourceDir = findSourcePlaywrightScriptDir();
        if (sourceDir != null) {
            return sourceDir;
        }
        return extractBundledScripts();
    }

    private static Path findSourcePlaywrightScriptDir() {
        Path current = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath();
        for (Path p = current; p != null; p = p.getParent()) {
            Path candidate = p.resolve("tools").resolve("playwright");
            if (Files.isRegularFile(candidate.resolve("ensure-playwright.ps1"))
                    && Files.isRegularFile(candidate.resolve("playwright-healthcheck.mjs"))) {
                return candidate;
            }
        }
        return null;
    }

    private static Path extractBundledScripts() throws IOException {
        Path target = Places.getCacheSubdirectory("sox-ai-tool/playwright").toPath();
        Files.createDirectories(target);
        for (String name : List.of("ensure-playwright.ps1", "helper.mjs", "playwright-healthcheck.mjs")) {
            String resource = "/tools/playwright/" + name;
            try (InputStream in = PlaywrightSettingsPanel.class.getResourceAsStream(resource)) {
                if (in == null) {
                    throw new IOException("Bundled Playwright script not found: " + resource);
                }
                Files.write(target.resolve(name), in.readAllBytes());
            }
        }
        return target;
    }

    private void showError(String title, Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        resultArea.setText(title + ":\n" + cause.getMessage());
    }

    private void setBusy(boolean busy) {
        detectButton.setEnabled(!busy);
        healthButton.setEnabled(!busy);
        forceNpmCheck.setEnabled(!busy);
    }

    private static JTextField textField() {
        JTextField field = new JTextField(60);
        field.setEditable(false);
        return field;
    }

    private static GridBagConstraints gbc(int x, int y, int width, boolean fullWidth) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = x;
        c.gridy = y;
        c.gridwidth = width;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(6, x == 0 ? 8 : 4, 4, 8);
        if (fullWidth) {
            c.fill = GridBagConstraints.HORIZONTAL;
            c.weightx = 1.0;
        }
        return c;
    }

    private static GridBagConstraints fill(int x, int y) {
        GridBagConstraints c = gbc(x, y, 1, true);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1.0;
        return c;
    }

    private record ProcessResult(int exitCode, String stdout, String stderr) {}
}
