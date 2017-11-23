package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public class GroovyScriptPreparer implements Serializable {

    private static final long serialVersionUID = 6304738377691375266L;

    private final Logger logger;

    private final transient GroovyScriptExecutorFactory groovyScriptExecutorFactory;

    private final FilePath workspace;

    public GroovyScriptPreparer(
        Logger logger,
        FilePath workspace,
        GroovyScriptExecutorFactory groovyScriptExecutorFactory
    ) {
        this.logger = logger;
        this.workspace = workspace;
        this.groovyScriptExecutorFactory = groovyScriptExecutorFactory;
    }

    public boolean evaluateScript(String scriptContent) {
        return evaluateScript(scriptContent, Collections.emptyList());
    }

    public boolean evaluateScript(String scriptContent, List<String> arguments) {

        if (scriptContent == null) {
            throw new IllegalArgumentException("The script content object must be set.");
        }

        if (ensureWorkspaceNotNull(workspace)) {
            return false;
        }

        try {
            return workspace.act(groovyScriptExecutorFactory.create(scriptContent, arguments));
        } catch (Exception throwable) {
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

    public boolean evaluateCommand(Command command) throws PostBuildScriptException {

        if (ensureWorkspaceNotNull(workspace)) {
            return false;
        }

        FilePath filePath = new ScriptFilePath(workspace).resolve(command.getScriptPath());
        LoadScriptContentCallable callable = new LoadScriptContentCallable();
        String scriptContent = new Content(callable).resolve(filePath);
        return evaluateScript(scriptContent, command.getParameters());
    }

}
