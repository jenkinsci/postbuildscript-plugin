package org.jenkinsci.plugins.postbuildscript.model;

import hudson.Util;
import org.kohsuke.stapler.DataBoundConstructor;

public class Script extends PostBuildItem {

    private final String content;

    @DataBoundConstructor
    public Script(String result, String content) {
        super(result);
        this.content = Util.fixEmpty(content);
    }

    public String getContent() {
        return content;
    }
}
