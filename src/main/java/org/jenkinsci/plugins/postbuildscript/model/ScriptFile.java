package org.jenkinsci.plugins.postbuildscript.model;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Collection;

public class ScriptFile extends PostBuildItem {

    private final String filePath;

    private ScriptType scriptType;
    private boolean sandboxed;

    @DataBoundConstructor
    public ScriptFile(Collection<String> results, String filePath) {
        super(results);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public ScriptType getScriptType() {
        return scriptType;
    }

    @DataBoundSetter
    public void setScriptType(ScriptType scriptType) {
        this.scriptType = scriptType;
    }

    public boolean isSandboxed() {
        return sandboxed;
    }

    @DataBoundSetter
    public void setSandboxed(boolean sandboxed) {
        this.sandboxed = sandboxed;
    }
}
