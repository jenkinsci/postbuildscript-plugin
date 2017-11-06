package org.jenkinsci.plugins.postbuildscript.service;

import groovy.lang.Binding;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import jenkins.security.SlaveToMasterCallable;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;

import java.io.File;

public class GroovyScriptExecutionCallable extends SlaveToMasterCallable<Boolean, Throwable> {
    private static final long serialVersionUID = 3874477459736242748L;
    private final String scriptContent;
    private final AbstractBuild<?, ?> build;
    private final Logger log;

    public GroovyScriptExecutionCallable(String scriptContent, AbstractBuild<?, ?> build, Logger log) {
        this.scriptContent = scriptContent;
        this.build = build;
        this.log = log;
    }

    @Override
    public Boolean call() throws Exception {

        String script = Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);

        Binding binding = new Binding();
        FilePath workspace = build.getWorkspace();
        if (workspace != null) {
            binding.setVariable("workspace", new File(workspace.getRemote())); //NON-NLS
        }
        binding.setVariable("log", log);
        binding.setVariable("out", log.getListener().getLogger()); //NON-NLS
        binding.setVariable("build", build); //NON-NLS

        ClassLoader classLoader = getClass().getClassLoader();

        SecureGroovyScript groovyScript = new SecureGroovyScript(script, false, null);
        groovyScript.configuringWithNonKeyItem();
        groovyScript.evaluate(classLoader, binding);

        return true;
    }
}
