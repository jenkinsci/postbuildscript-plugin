package org.jenkinsci.plugins.postbuildscript;

import hudson.Util;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class GroovyScriptContent extends PostBuildItem {

    private final String content;

    @DataBoundConstructor
    public GroovyScriptContent(String content, String result) {
        super(result);
        this.content = Util.fixEmpty(content);
    }

    @SuppressWarnings("unused")
    public String getContent() {
        return content;
    }
}
