package org.jenkinsci.plugins.postbuildscript.model;

import hudson.Util;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

import java.util.Collection;

public class Script extends PostBuildItem {

    private final String content;

    private boolean sandboxed;

    @DataBoundConstructor
    public Script(Collection<String> results, String content) {
        super(results);
        this.content = Util.fixEmpty(content);
    }

    public String getContent() {
        return content;
    }

    @DataBoundSetter
    public void setSandboxed(boolean sandboxed) {
        this.sandboxed = sandboxed;
    }

    public boolean isSandboxed() {
        return sandboxed;
    }

}
