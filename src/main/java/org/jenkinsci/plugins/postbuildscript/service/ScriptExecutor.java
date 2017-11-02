package org.jenkinsci.plugins.postbuildscript.service;

import groovy.lang.Binding;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import jenkins.SlaveToMasterFileCallable;
import jenkins.security.SlaveToMasterCallable;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptLog;
import org.jenkinsci.plugins.scriptsecurity.sandbox.groovy.SecureGroovyScript;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class ScriptExecutor implements Serializable {

    private PostBuildScriptLog log;

    private BuildListener listener;

    public ScriptExecutor(PostBuildScriptLog log, BuildListener listener) {
        this.log = log;
        this.listener = listener;
    }

    public int executeScriptPathAndGetExitCode(FilePath workspace, String command, Launcher launcher) throws PostBuildScriptException {

        FilePath filePath = resolveScriptPath(workspace, command);
        String[] splittedCommand = command.split("\\s+");
        String[] parameters;
        parameters = new String[splittedCommand.length - 1];
        if (splittedCommand.length > 1) {
            System.arraycopy(splittedCommand, 1, parameters, 0, parameters.length);
        }
        return executeScript(workspace, filePath, launcher, parameters);

    }

    private String getResolvedContentWithEnvVars(FilePath filePath) throws PostBuildScriptException {
        String scriptContentResolved;
        try {
            log.info("Resolving environment variables for the script content.");
            scriptContentResolved =
                    filePath.act(new SlaveToMasterFileCallable<String>() {
                        @Override
                        public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                            String scriptContent = Util.loadFile(f);
                            return Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);
                        }
                    });
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException("Error to resolve environment variables", ioe);
        }
        return scriptContentResolved;
    }

    private int executeScript(FilePath workspace, FilePath script, final Launcher launcher, String[] parameters) throws PostBuildScriptException {

        assert script != null;
        assert launcher != null;

        String scriptContent = getResolvedContentWithEnvVars(script);
        log.info(String.format("Executing the script %s with parameters %s", script, Arrays.toString(parameters)));
        FilePath tmpFile;
        try {
            final CommandInterpreter batchRunner;
            if (launcher.isUnix()) {
                batchRunner = new Shell(scriptContent);
            } else {
                batchRunner = new BatchFile(scriptContent);
            }
            tmpFile = batchRunner.createScriptFile(workspace);
            List<String> args = new ArrayList<>(Arrays.asList(batchRunner.buildCommandLine(tmpFile)));
            args.addAll(Arrays.asList(parameters));
            return launcher.launch().cmds(args).stdout(listener).pwd(workspace).join();
        } catch (InterruptedException | IOException ie) {
            throw new PostBuildScriptException("Error to execute script", ie);
        }
    }


    private FilePath getFilePath(final FilePath workspace, final String givenPath) throws PostBuildScriptException {

        try {
            return workspace.act(new SlaveToMasterFileCallable<FilePath>() {
                @Override
                public FilePath invoke(File ws, VirtualChannel channel) throws IOException, InterruptedException {
                    File givenFile = new File(givenPath);
                    if (givenFile.exists()) {
                        return new FilePath(channel, givenFile.getPath());
                    }

                    FilePath filePath = new FilePath(workspace, givenPath);
                    if (filePath.exists()) {
                        return filePath;
                    }
                    return null;
                }
            });
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException("Error to resolve script path", ioe);
        }
    }


    public boolean performGroovyScript(final FilePath workspace, final String scriptContent) {

        if (scriptContent == null) {
            throw new NullPointerException("The script content object must be set.");
        }
        try {
            return workspace.act(new SlaveToMasterCallable<Boolean, Throwable>() {
                @Override
                public Boolean call() throws Throwable {
                    final String groovyExpressionResolved = Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);
                    log.info(String.format("Evaluating the groovy script: \n %s", scriptContent));
                    Binding binding = new Binding();
                    binding.setVariable("workspace", new File(workspace.getRemote()));
                    binding.setVariable("log", log);
                    binding.setVariable("out", log.getListener().getLogger());
                    ClassLoader classLoader = getClass().getClassLoader();
                    SecureGroovyScript script = new SecureGroovyScript(groovyExpressionResolved, false, null);
                    script.configuringWithNonKeyItem();
                    script.evaluate(classLoader, binding);
                    return true;
                }
            });
        } catch (Throwable e) {
            listener.getLogger().print("Problems occurs: " + e.getMessage());
            return false;
        }
    }


    public boolean performGroovyScriptFile(FilePath workspace, final String scriptFilePath) throws PostBuildScriptException {
        FilePath filePath = resolveScriptPath(workspace, scriptFilePath);

        String scriptContent = getResolvedContentWithEnvVars(filePath);
        return performGroovyScript(workspace, scriptContent);
    }

    private FilePath resolveScriptPath(FilePath workspace, String commandString) throws PostBuildScriptException {
        if (commandString == null) {
            throw new NullPointerException("The commandString object must be set.");
        }

        String scriptFilePath = commandString.split("\\s+")[0];

        FilePath filePath = getFilePath(workspace, scriptFilePath);
        if (filePath == null) {
            throw new PostBuildScriptException(String.format("The script file path '%s' doesn't exist.", scriptFilePath));
        }
        return filePath;
    }
}
