package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.PostBuildScriptException;

import java.io.IOException;
import java.util.regex.Pattern;

public class ScriptFilePath {

    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    private final FilePath workspace;

    public ScriptFilePath(FilePath workspace) {
        this.workspace = workspace;
    }

    public FilePath resolve(CharSequence command) throws PostBuildScriptException {

        String scriptFilePath = WHITESPACE_PATTERN.split(command)[0];

        FilePath filePath = getFilePath(scriptFilePath);
        if (filePath == null) {
            throw new PostBuildScriptException(
                Messages.PostBuildScript_ScriptFilePathDoesNotExist(scriptFilePath));
        }

        return filePath;

    }

    private FilePath getFilePath(String givenPath) throws PostBuildScriptException {

        try {
            return workspace.act(new LoadFileCallable(givenPath, workspace));
        } catch (IOException | InterruptedException ioe) {
            throw new PostBuildScriptException("Error to resolve script path", ioe);
        }

    }


}
