package org.jenkinsci.plugins.postbuildscript;

/**
 * @author Gregory Boissinot
 */
public class PostBuildScriptException extends Exception {

    private static final long serialVersionUID = 8656827646635596263L;

    public PostBuildScriptException(String message) {
        super(message);
    }

    public PostBuildScriptException(String message, Throwable cause) {
        super(message, cause);
    }

    public PostBuildScriptException(Throwable cause) {
        super(cause);
    }
}
