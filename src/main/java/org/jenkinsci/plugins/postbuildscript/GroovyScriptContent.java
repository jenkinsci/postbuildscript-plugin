package org.jenkinsci.plugins.postbuildscript;

import hudson.Util;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class GroovyScriptContent {

    private final String content;

    @DataBoundConstructor
    public GroovyScriptContent(String content) {
        this.content = Util.fixEmpty(content);
    }

    @SuppressWarnings("unused")
    public String getContent() {
        return content;
    }
}
