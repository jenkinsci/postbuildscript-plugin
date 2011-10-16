package org.jenkinsci.plugins.postbuildscript;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class GenericScript {

    private final String filePath;

    private transient String content;

    @DataBoundConstructor
    public GenericScript(String filePath) {
        this.filePath = filePath;
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
