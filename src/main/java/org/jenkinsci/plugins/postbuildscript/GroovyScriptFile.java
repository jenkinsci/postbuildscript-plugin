package org.jenkinsci.plugins.postbuildscript;

import hudson.Util;
import org.kohsuke.stapler.DataBoundConstructor;

/**
 * @author Gregory Boissinot
 */
public class GroovyScriptFile {

    private final String filePath;

    @DataBoundConstructor
    public GroovyScriptFile(String filePath) {
        this.filePath = Util.fixEmpty(filePath);
    }

    @SuppressWarnings("unused")
    public String getFilePath() {
        return filePath;
    }

}
