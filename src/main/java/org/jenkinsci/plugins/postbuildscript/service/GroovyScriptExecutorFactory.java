package org.jenkinsci.plugins.postbuildscript.service;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.postbuildscript.Logger;

import java.util.List;

public class GroovyScriptExecutorFactory {

    private final AbstractBuild<?, ?> build;

    private final Logger logger;

    public GroovyScriptExecutorFactory(AbstractBuild<?, ?> build, Logger logger) {
        this.build = build;
        this.logger = logger;
    }

    public GroovyScriptExecutor create(String scriptContent, List<String> arguments) {
        return new GroovyScriptExecutor(scriptContent, arguments, build, logger);
    }
}
