package io.github.nbplugins.claudecodegui.settings;

/** CLI backend used for new terminal sessions. */
public enum AgentBackend {
    CLAUDE("Claude Code"),
    CODEX("Codex");

    private final String label;

    AgentBackend(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static AgentBackend fromName(String name) {
        if (name != null) {
            for (AgentBackend backend : values()) {
                if (backend.name().equals(name)) {
                    return backend;
                }
            }
        }
        return CLAUDE;
    }

    @Override
    public String toString() {
        return label;
    }
}
