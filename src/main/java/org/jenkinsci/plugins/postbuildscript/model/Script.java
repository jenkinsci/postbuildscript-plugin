package org.jenkinsci.plugins.postbuildscript.model;

import hudson.Util;
import org.kohsuke.stapler.DataBoundConstructor;

import java.util.Collection;

public class Script extends PostBuildItem {

    private final String content;

    @DataBoundConstructor
    public Script(Collection<String> results, String content) {
        super(results);
        this.content = Util.fixEmpty(content);
    }

    public String getContent() {
        return content;
    }

    public Object readResolve() {
        super.readResolve();
        return this;
    }

}
