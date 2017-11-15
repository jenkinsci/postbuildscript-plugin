package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;

import java.io.Serializable;

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

    public boolean evaluate(String scriptContent) {

        if (scriptContent == null) {
            throw new IllegalArgumentException("The script content object must be set.");
        }

        if (ensureWorkspaceNotNull(workspace)) {
            return false;
        }

        try {
            return workspace.act(groovyScriptExecutorFactory.create(scriptContent));
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

    public boolean evaluateFile(CharSequence command) throws PostBuildScriptException {

        if (ensureWorkspaceNotNull(workspace)) {
            return false;
        }

        FilePath filePath = new ScriptFilePath(workspace).resolve(command);
        LoadScriptContentCallable callable = new LoadScriptContentCallable();
        String scriptContent = new Content(logger, callable).resolve(filePath);
        return evaluate(scriptContent);
    }

}
