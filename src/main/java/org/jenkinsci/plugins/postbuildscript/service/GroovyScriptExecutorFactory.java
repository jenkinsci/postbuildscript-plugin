package org.jenkinsci.plugins.postbuildscript.service;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.postbuildscript.Logger;

public class GroovyScriptExecutorFactory {

    private final AbstractBuild<?, ?> build;

    private final Logger logger;

    public GroovyScriptExecutorFactory(AbstractBuild<?, ?> build, Logger logger) {
        this.build = build;
        this.logger = logger;
    }

    public GroovyScriptExecutor create(String scriptContent) {
        return new GroovyScriptExecutor(scriptContent, build, logger);
    }
}
