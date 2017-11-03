package org.jenkinsci.plugins.postbuildscript;

import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class GenericScript extends PostBuildItem {

    private final String filePath;

    @DataBoundConstructor
    public GenericScript(String filePath, String result) {
        super(result);
        this.filePath = filePath;
    }

    @SuppressWarnings("unused")
    public String getFilePath() {
        return filePath;
    }

}
