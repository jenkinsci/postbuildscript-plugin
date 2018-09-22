package org.jenkinsci.plugins.postbuildscript.service;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.postbuildscript.logging.Logger;
import org.jenkinsci.plugins.postbuildscript.model.Script;

import java.util.List;

public class GroovyScriptExecutorFactory {

    private final AbstractBuild<?, ?> build;

    private final Logger logger;

    public GroovyScriptExecutorFactory(AbstractBuild<?, ?> build, Logger logger) {
        this.build = build;
        this.logger = logger;
    }

    public GroovyScriptExecutor create(Script script, List<String> arguments) {
        return new GroovyScriptExecutor(script, arguments, build, logger);
    }
}
