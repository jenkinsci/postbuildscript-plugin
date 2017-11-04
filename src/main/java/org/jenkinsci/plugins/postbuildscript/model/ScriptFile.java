package org.jenkinsci.plugins.postbuildscript.model;

import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;

public class ScriptFile extends PostBuildItem {

    private final String filePath;

    @DataBoundConstructor
    public ScriptFile(Collection<String> results, String filePath) {
        super(results);
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

}
