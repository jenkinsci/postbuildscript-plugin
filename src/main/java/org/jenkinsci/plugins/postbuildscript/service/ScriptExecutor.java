package org.jenkinsci.plugins.postbuildscript.service;

import groovy.lang.GroovyShell;
import hudson.EnvVars;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.Callable;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptLog;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class ScriptExecutor implements Serializable {

    protected PostBuildScriptLog log;

    private BuildListener listener;

    public ScriptExecutor(PostBuildScriptLog log, BuildListener listener) {
        this.log = log;
        this.listener = listener;
    }

    public int executeScriptPathAndGetExitCode(FilePath workspace, String scriptFilePath, Launcher launcher) throws PostBuildScriptException {

        if (scriptFilePath == null) {
            throw new NullPointerException("The scriptFilePath object must be set.");
        }

        FilePath filePath = getFilePath(workspace, scriptFilePath);
        if (filePath == null) {
            throw new PostBuildScriptException(String.format("The script file path '%s' doesn't exist.", scriptFilePath));
        }

        return executeScript(workspace, filePath, launcher);
    }

    private String getResolvedContentWithEnvVars(FilePath filePath) throws PostBuildScriptException {
        String scriptContentResolved;
        try {
            log.info("Resolving environment variables for the script content.");
            scriptContentResolved =
                    filePath.act(new FilePath.FileCallable<String>() {
                        public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                            String scriptContent = Util.loadFile(f);
                            return Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);
                        }
                    });
        } catch (IOException ioe) {
            throw new PostBuildScriptException("Error to resolve environment variables", ioe);
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException("Error to resolve environment variables", ie);
        }
        return scriptContentResolved;
    }

    private int executeScript(FilePath workspace, FilePath script, final Launcher launcher) throws PostBuildScriptException {

        assert script != null;
        assert launcher != null;

        String scriptContent = getResolvedContentWithEnvVars(script);
        log.info(String.format("Evaluating the script: \n %s", scriptContent));
        FilePath tmpFile;
        try {
            final CommandInterpreter batchRunner;
            if (launcher.isUnix()) {
                batchRunner = new Shell(scriptContent);
            } else {
                batchRunner = new BatchFile(scriptContent);
            }
            tmpFile = batchRunner.createScriptFile(workspace);
            return launcher.launch().cmds(batchRunner.buildCommandLine(tmpFile)).stdout(listener).pwd(workspace).join();
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException("Error to execute script", ie);
        } catch (IOException ioe) {
            throw new PostBuildScriptException("Error to execute script", ioe);
        }
    }


    private FilePath getFilePath(final FilePath workspace, final String givenPath) throws PostBuildScriptException {

        try {
            return workspace.act(new FilePath.FileCallable<FilePath>() {
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
        } catch (IOException ioe) {
            throw new PostBuildScriptException("Error to resolve script path", ioe);
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException("Error to resolve script path", ie);
        }
    }


    public boolean performGroovyScript(FilePath workspace, final String scriptContent) {

        if (scriptContent == null) {
            throw new NullPointerException("The script content object must be set.");
        }
        try {
            return workspace.act(new Callable<Boolean, Throwable>() {
                public Boolean call() throws Throwable {
                    final String groovyExpressionResolved = Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);
                    log.info(String.format("Evaluating the groovy script: \n %s", scriptContent));
                    GroovyShell shell = new GroovyShell();
                    shell.evaluate(groovyExpressionResolved);
                    return true;
                }
            });
        } catch (IOException ioe) {
            listener.getLogger().print("Problems occurs: " + ioe.getMessage());
            return false;
        } catch (InterruptedException ie) {
            listener.getLogger().print("Problems occurs: " + ie.getMessage());
            return false;
        } catch (Throwable e) {
            listener.getLogger().print("Problems occurs: " + e.getMessage());
            return false;
        }
    }


    public boolean performGroovyScriptFile(FilePath workspace, final String scriptFilePath) throws PostBuildScriptException {
        if (scriptFilePath == null) {
            throw new NullPointerException("The scriptFilePath object must be set.");
        }

        FilePath filePath = getFilePath(workspace, scriptFilePath);
        if (filePath == null) {
            throw new PostBuildScriptException(String.format("The script file path '%s' doesn't exist.", scriptFilePath));
        }

        String scriptContent = getResolvedContentWithEnvVars(filePath);
        return performGroovyScript(workspace, scriptContent);
    }
}
