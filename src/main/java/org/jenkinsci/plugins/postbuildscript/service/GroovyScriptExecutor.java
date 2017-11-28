package org.jenkinsci.plugins.postbuildscript.service;

import groovy.lang.Binding;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import jenkins.security.MasterToSlaveCallable;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroovyScriptExecutor extends MasterToSlaveCallable<Boolean, Exception> {

    private static final long serialVersionUID = 3874477459736242748L;
    private final String scriptContent;
    private final List<String> arguments;
    private final transient AbstractBuild<?, ?> build;
    private final Logger log;

    public GroovyScriptExecutor(String scriptContent, List<String> arguments, AbstractBuild<?, ?> build, Logger log) {
        this.scriptContent = scriptContent;
        this.arguments = new ArrayList<>(arguments);
        this.build = build;
        this.log = log;
    }

    @Override
    public Boolean call() throws Exception {

        String script = Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);

        Binding binding = new Binding();
        if (build != null) {
            FilePath workspace = build.getWorkspace();
            if (workspace != null && workspace.getRemote() != null) {
                binding.setVariable("workspace", new File(workspace.getRemote())); //NON-NLS
            }
            binding.setVariable("build", build); //NON-NLS
        }

        binding.setVariable("log", log);
        binding.setVariable("out", log.getListener().getLogger()); //NON-NLS
        binding.setVariable("args", arguments);


        ClassLoader classLoader = getClass().getClassLoader();

        SecureGroovyScript groovyScript = new SecureGroovyScript(script, false, null);
        groovyScript.configuringWithNonKeyItem();
        groovyScript.evaluate(classLoader, binding);

        return true;
    }
}
