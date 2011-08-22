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

import javax.inject.Inject;
import java.io.*;

/**
 * @author Gregory Boissinot
 */
public class ScriptExecutor implements Serializable {

    @Inject
    protected FilePath executionNodeRootPath;

    @Inject
    protected PostBuildScriptLog log;

    @Inject
    private BuildListener listener;

    public int executeScriptAndGetExitCode(String scriptContent, Launcher launcher) throws PostBuildScriptException {

        if (scriptContent == null) {
            throw new NullPointerException("A scriptContent object must be set.");
        }
        return resolveContentAndExuteScript(scriptContent, launcher);
    }

    private int resolveContentAndExuteScript(String scriptContent, Launcher launcher) throws PostBuildScriptException {
        String scriptContentResolved = getResolvedContentWithEnvVars(scriptContent);
        return executeScript(scriptContentResolved, launcher);
    }


    public int executeScriptPathAndGetExitCode(String scriptFilePath, Launcher launcher) throws PostBuildScriptException {

        if (scriptFilePath == null) {
            throw new NullPointerException("The scriptFilePath object must be set.");
        }

        if (!existsScript(scriptFilePath)) {
            throw new PostBuildScriptException(String.format("The script file path '%s' doesn't exist.", scriptFilePath));
        }

        String scriptContent = getStringContent(scriptFilePath);
        return resolveContentAndExuteScript(scriptContent, launcher);
    }

    private String getResolvedContentWithEnvVars(final String scriptContent) throws PostBuildScriptException {
        String scriptContentResolved;
        try {
            log.info("Resolving environment variables for script the content.");
            scriptContentResolved =
                    executionNodeRootPath.act(new FilePath.FileCallable<String>() {
                        public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                            return Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);
                        }
                    });
        } catch (IOException ioe) {
            throw new PostBuildScriptException("Error to execute the script", ioe);
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException("Error to execute the script", ie);
        }
        return scriptContentResolved;
    }

    protected String getStringContent(final String filePath) throws PostBuildScriptException {

        assert filePath != null;

        try {
            return executionNodeRootPath.act(new FilePath.FileCallable<String>() {

                public String invoke(File f, VirtualChannel channel) throws IOException, InterruptedException {
                    StringBuffer content = new StringBuffer();
                    FileReader fileReader = new FileReader(filePath);
                    BufferedReader bufferedReader = new BufferedReader(fileReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        content.append(line);
                    }
                    return content.toString();
                }
            });
        } catch (IOException ioe) {
            throw new PostBuildScriptException(ioe);
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException(ie);
        }
    }

    private int executeScript(final String scriptContent, final Launcher launcher) throws PostBuildScriptException {

        assert scriptContent != null;
        log.info(String.format("Evaluating the script: \n %s", scriptContent));
        FilePath tmpFile;
        try {
            boolean isUnix = File.pathSeparatorChar == ':';
            final CommandInterpreter batchRunner;
            if (launcher.isUnix()) {
                batchRunner = new Shell(scriptContent);
            } else {
                batchRunner = new BatchFile(scriptContent);
            }
            tmpFile = batchRunner.createScriptFile(executionNodeRootPath);
            return launcher.launch().cmds(batchRunner.buildCommandLine(tmpFile)).stdout(listener).pwd(executionNodeRootPath).join();
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException(ie);
        } catch (IOException ioe) {
            throw new PostBuildScriptException(ioe);
        }
    }


    protected boolean existsScript(final String path) throws PostBuildScriptException {

        try {
            return executionNodeRootPath.act(new Callable<Boolean, PostBuildScriptException>() {
                public Boolean call() throws PostBuildScriptException {
                    File f = new File(path);
                    if (!f.exists()) {
                        log.info(String.format("Can't load the file '%s'. It doesn't exist.", f.getPath()));
                        return false;
                    }
                    return true;
                }
            });
        } catch (IOException ioe) {
            throw new PostBuildScriptException(ioe);
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException(ie);
        }
    }


    public Object evaluateGroovyScript(final String scriptContent) throws PostBuildScriptException {

        if (scriptContent == null) {
            throw new NullPointerException("The script content object must be set.");
        }
        try {
            return executionNodeRootPath.act(new Callable<Object, PostBuildScriptException>() {
                public Object call() throws PostBuildScriptException {
                    final String groovyExpressionResolved = Util.replaceMacro(scriptContent, EnvVars.masterEnvVars);
                    log.info(String.format("Evaluating the groovy script: \n %s", scriptContent));
                    GroovyShell shell = new GroovyShell();
                    return shell.evaluate(groovyExpressionResolved);
                }
            });
        } catch (IOException ioe) {
            throw new PostBuildScriptException(ioe);
        } catch (InterruptedException ie) {
            throw new PostBuildScriptException(ie);
        }
    }

    public Object evaluateGroovyScriptFilePath(final String scriptFilePath) throws PostBuildScriptException {
        if (scriptFilePath == null) {
            throw new NullPointerException("The scriptFilePath object must be set.");
        }

        if (!existsScript(scriptFilePath)) {
            return false;
        }

        String scriptContent = getStringContent(scriptFilePath);
        return evaluateGroovyScript(scriptContent);
    }
}
