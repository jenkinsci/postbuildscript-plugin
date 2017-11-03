package org.jenkinsci.plugins.postbuildscript;

import hudson.Util;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class GroovyScriptFile extends PostBuildItem {

    private final String filePath;

    @DataBoundConstructor
    public GroovyScriptFile(String filePath, String result) {
        super(result);
        this.filePath = Util.fixEmpty(filePath);
    }

    @SuppressWarnings("unused")
    public String getFilePath() {
        return filePath;
    }

}
