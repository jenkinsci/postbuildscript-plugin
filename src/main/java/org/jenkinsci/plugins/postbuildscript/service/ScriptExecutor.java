package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.tasks.BatchFile;
import hudson.tasks.CommandInterpreter;
import hudson.tasks.Shell;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Gregory Boissinot
 */
public class ScriptExecutor implements Serializable {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");
    private static final long serialVersionUID = 6304738377691375266L;

    private final Logger logger;

    private final BuildListener listener;

    public ScriptExecutor(Logger logger, BuildListener listener) {
        this.logger = logger;
        this.listener = listener;
    }

    private static FilePath getFilePath(FilePath workspace, String givenPath) throws PostBuildScriptException {

        try {
            return workspace.act(new LoadFileCallable(givenPath, workspace));
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException("Error to resolve script path", ioe);
        }
    }

    private static FilePath resolveScriptPath(
        FilePath workspace,
        CharSequence command
    ) throws PostBuildScriptException {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null");
        }

        String scriptFilePath = WHITESPACE_PATTERN.split(command)[0];

        FilePath filePath = getFilePath(workspace, scriptFilePath);
        if (filePath == null) {
            throw new PostBuildScriptException(Messages.PostBuildScript_ScriptFilePathDoesNotExist(scriptFilePath));
        }
        return filePath;
    }

    public int executeScriptPathAndGetExitCode(
        FilePath workspace,
        CharSequence command,
        Launcher launcher
    ) throws PostBuildScriptException {

        FilePath filePath = resolveScriptPath(workspace, command);
        String[] splittedCommand = WHITESPACE_PATTERN.split(command);
        String[] parameters = new String[splittedCommand.length - 1];
        if (splittedCommand.length > 1) {
            System.arraycopy(splittedCommand, 1, parameters, 0, parameters.length);
        }
        return executeScript(workspace, filePath, launcher, parameters);

    }

    private String getResolvedContentWithEnvVars(FilePath filePath) throws PostBuildScriptException {
        String resolvedScript;
        try {
            logger.info(Messages.PostBuildScript_ResolvingEnvironmentVariables());
            resolvedScript =
                filePath.act(new LoadScriptContentCallable());
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException("Error to resolve environment variables", ioe);
        }
        return resolvedScript;
    }

    private int executeScript(
        FilePath workspace,
        FilePath script,
        Launcher launcher,
        String[] parameters
    ) throws PostBuildScriptException {

        if (script == null) {
            throw new IllegalArgumentException("script must not be null");
        }
        if (launcher == null) {
            throw new IllegalArgumentException("launcher must not be null");
        }

        String scriptContent = getResolvedContentWithEnvVars(script);
        logger.info(Messages.PostBuildScript_ExecutingScript(script, Arrays.toString(parameters)));
        try {
            CommandInterpreter batchRunner;
            if (launcher.isUnix()) {
                batchRunner = new Shell(scriptContent);
            } else {
                batchRunner = new BatchFile(scriptContent);
            }
            FilePath tmpFile = batchRunner.createScriptFile(workspace);
            List<String> args = new ArrayList<>(Arrays.asList(batchRunner.buildCommandLine(tmpFile)));
            args.addAll(Arrays.asList(parameters));
            return launcher.launch().cmds(args).stdout(listener).pwd(workspace).join();
        } catch (InterruptedException | IOException ie) {
            throw new PostBuildScriptException("Error to execute script", ie);
        }
    }

    public boolean performGroovyScript(AbstractBuild<?, ?> build, String scriptContent) {

        if (scriptContent == null) {
            throw new IllegalArgumentException("The script content object must be set.");
        }

        FilePath workspace = build.getWorkspace();
        if (ensureWorkspaceNotNull(workspace)) {
            return false;
        }

        try {
            return workspace.act(new GroovyScriptExecutionCallable(scriptContent, build, logger));
        } catch (Throwable throwable) {
            logger.info(Messages.PostBuildScript_ProblemOccured(throwable.getMessage()));
            return false;
        }
    }

    private boolean ensureWorkspaceNotNull(FilePath workspace) {
        if (workspace == null) {
            logger.info(Messages.PostBuildScript_WorkspaceEmpty());
            return true;
        }
        return false;
    }

    public boolean performGroovyScriptFile(
        AbstractBuild<?, ?> build,
        CharSequence scriptFilePath
    ) throws PostBuildScriptException {

        FilePath workspace = build.getWorkspace();
        if (ensureWorkspaceNotNull(workspace)) {
            return false;
        }

        FilePath filePath = resolveScriptPath(workspace, scriptFilePath);

        String scriptContent = getResolvedContentWithEnvVars(filePath);
        return performGroovyScript(build, scriptContent);
    }

}
