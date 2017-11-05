package org.jenkinsci.plugins.postbuildscript.service;

import hudson.EnvVars;
import hudson.Util;
import hudson.remoting.VirtualChannel;
import jenkins.SlaveToMasterFileCallable;

import java.io.File;
import java.io.IOException;

public class LoadScriptContentCallable extends SlaveToMasterFileCallable<String> {
    private static final long serialVersionUID = -8106062333861360484L;

    @Override
    public String invoke(File f, VirtualChannel channel) throws IOException {
        String scriptContent = Util.loadFile(f);
        return Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);
    }
}
