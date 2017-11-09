package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class GroovyScriptExecutor implements Serializable {

    private static final long serialVersionUID = 6304738377691375266L;

    private final Logger logger;

    public GroovyScriptExecutor(Logger logger) {
        this.logger = logger;
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
        CharSequence command
    ) throws PostBuildScriptException {

        FilePath workspace = build.getWorkspace();
        if (ensureWorkspaceNotNull(workspace)) {
            return false;
        }

        FilePath filePath = new ScriptFilePath(workspace).resolve(command);
        LoadScriptContentCallable callable = new LoadScriptContentCallable();
        String scriptContent = new Content(logger, callable).resolve(filePath);
        return performGroovyScript(build, scriptContent);
    }

}
