package org.jenkinsci.plugins.postbuildscript;

import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.postbuildscript.model.Configuration;

public class ProcessorFactory {

    private final Configuration config;

    public ProcessorFactory(Configuration config) {
        this.config = config;
    }

    public Processor create(
        AbstractBuild<?, ?> build,
        Launcher launcher,
        BuildListener listener) {
        return new Processor(build, launcher, listener, config);
    }
}
