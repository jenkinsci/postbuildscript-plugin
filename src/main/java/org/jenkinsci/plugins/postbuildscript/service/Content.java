package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;

import java.io.IOException;

public class Content {

    private final Logger logger;

    private final FileCallable<String> callable;

    public Content(Logger logger, FileCallable<String> callable) {
        this.logger = logger;
        this.callable = callable;
    }

    public String resolve(FilePath filePath) throws PostBuildScriptException {
        try {
            logger.info(Messages.PostBuildScript_ResolvingEnvironmentVariables());
            return filePath.act(callable);
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException("Error to resolve environment variables", ioe);
        }
    }

}
