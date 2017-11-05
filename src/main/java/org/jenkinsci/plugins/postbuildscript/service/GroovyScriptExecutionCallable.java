package org.jenkinsci.plugins.postbuildscript.service;

import groovy.lang.Binding;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import jenkins.security.SlaveToMasterCallable;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;

import java.io.File;

public class GroovyScriptExecutionCallable extends SlaveToMasterCallable<Boolean, Throwable> {
    private static final long serialVersionUID = 3874477459736242748L;
    private final String scriptContent;
    private final FilePath workspace;
    private final Logger log;

    public GroovyScriptExecutionCallable(String scriptContent, FilePath workspace, Logger log) {
        this.scriptContent = scriptContent;
        this.workspace = workspace;
        this.log = log;
    }

    @Override
    public Boolean call() throws Exception {

        String script = Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);


        Binding binding = new Binding();
        binding.setVariable("workspace", new File(workspace.getRemote()));
        binding.setVariable("log", log);
        binding.setVariable("out", log.getListener().getLogger());

        ClassLoader classLoader = getClass().getClassLoader();

        SecureGroovyScript groovyScript = new SecureGroovyScript(script, false, null);
        groovyScript.configuringWithNonKeyItem();
        groovyScript.evaluate(classLoader, binding);

        return true;
    }
}
