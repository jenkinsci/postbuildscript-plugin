package org.jenkinsci.plugins.postbuildscript.model;

import org.kohsuke.stapler.DataBoundConstructor;

public class ScriptFile extends PostBuildItem {

    private final String filePath;

    @DataBoundConstructor
    public ScriptFile(String result, String filePath) {
        super(result);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

}
