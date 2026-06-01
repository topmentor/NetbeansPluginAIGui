package io.github.nbplugins.claudecodegui.settings;

import java.util.ArrayList;
import java.util.List;

/** Preset approval/sandbox policies for Codex CLI sessions. */
public enum CodexRunMode {
    PLAN("Plan Mode", "read-only", "on-request", false),
    ASK_ON_COMMAND("Ask on Command", "workspace-write", "on-request", false),
    AUTO("Auto Mode", "workspace-write", "never", false),
    FULL_AUTO("Full Auto", "danger-full-access", "never", false),
    BYPASS("Bypass Approvals + Sandbox", null, null, true);

    private final String label;
    private final String sandbox;
    private final String approval;
    private final boolean bypassApprovalsAndSandbox;

    CodexRunMode(String label, String sandbox, String approval,
                 boolean bypassApprovalsAndSandbox) {
        this.label = label;
        this.sandbox = sandbox;
        this.approval = approval;
        this.bypassApprovalsAndSandbox = bypassApprovalsAndSandbox;
    }

    public List<String> cliArgs() {
        List<String> args = new ArrayList<>();
        if (bypassApprovalsAndSandbox) {
            args.add("--dangerously-bypass-approvals-and-sandbox");
            return args;
        }
        if (sandbox != null) {
            args.add("--sandbox");
            args.add(sandbox);
        }
        if (approval != null) {
            args.add("--ask-for-approval");
            args.add(approval);
        }
        return args;
    }

    public static CodexRunMode fromName(String name) {
        if (name != null) {
            for (CodexRunMode mode : values()) {
                if (mode.name().equals(name)) return mode;
            }
        }
        return ASK_ON_COMMAND;
    }

    @Override
    public String toString() {
        return label;
    }
}
