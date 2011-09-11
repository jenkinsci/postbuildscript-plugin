package org.jenkinsci.plugins.postbuildscript;

import hudson.Util;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class GenericScript {

    private final String filePath;

    private transient String content;

    @DataBoundConstructor
    public GenericScript(String filePath) {
        this.filePath = Util.fixEmpty(filePath);
    }

    @SuppressWarnings("unused")
    public String getFilePath() {
        return filePath;
    }

    @SuppressWarnings("unused")
    @Deprecated
    public String getContent() {
        return content;
    }
}
