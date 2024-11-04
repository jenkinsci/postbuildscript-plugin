package org.jenkinsci.plugins.postbuildscript.service;

import hudson.model.AbstractBuild;
import hudson.model.Descriptor.FormException;

import java.util.List;
import org.jenkinsci.plugins.postbuildscript.logging.Logger;
import org.jenkinsci.plugins.postbuildscript.model.Script;

public class GroovyScriptExecutorFactory {

    private final AbstractBuild<?, ?> build;

    private final Logger logger;

    public GroovyScriptExecutorFactory(AbstractBuild<?, ?> build, Logger logger) {
        this.build = build;
        this.logger = logger;
    }

    public GroovyScriptExecutor create(Script script, List<String> arguments) throws FormException {
        return new GroovyScriptExecutor(script, arguments, build, logger);
    }
}
