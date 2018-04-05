package org.jenkinsci.plugins.postbuildscript.service;

import groovy.lang.Binding;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Util;
import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class GroovyScriptExecutor {

    private static final long serialVersionUID = 3874477459736242748L;
    private final List<String> arguments;
    private final transient AbstractBuild<?, ?> build;
    private final Logger log;
    private final SecureGroovyScript secureGroovyScript;

    public GroovyScriptExecutor(Script script, List<String> arguments, AbstractBuild<?, ?> build, Logger log) {
        this.arguments = new ArrayList<>(arguments);
        this.build = build;
        this.log = log;
        String enrichedScript = Util.replaceMacro(script.getContent(), EnvVars.masterEnvVars);
        secureGroovyScript = new SecureGroovyScript(enrichedScript, script.isSandboxed(), null);
        secureGroovyScript.configuringWithNonKeyItem();
    }

    public void execute() throws Exception {

        Binding binding = new Binding();
        if (build != null) {
            FilePath workspace = build.getWorkspace();
            if (workspace != null && workspace.getRemote() != null) {
                binding.setVariable("workspace", new File(workspace.getRemote())); //NON-NLS
            }
            binding.setVariable("build", build); //NON-NLS
        }

        binding.setVariable("log", log); //NON-NLS
        binding.setVariable("out", log.getListener().getLogger()); //NON-NLS
        binding.setVariable("args", arguments); //NON-NLS

        ClassLoader classLoader = getClass().getClassLoader();
        secureGroovyScript.evaluate(classLoader, binding);
    }
}
