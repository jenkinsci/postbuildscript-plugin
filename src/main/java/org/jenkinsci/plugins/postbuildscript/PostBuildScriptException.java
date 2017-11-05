package org.jenkinsci.plugins.postbuildscript;

/**
 * @author Gregory Boissinot
 */
public class PostBuildScriptException extends Exception {

    private static final long serialVersionUID = 8656827646635596263L;

    public PostBuildScriptException(String s) {
        super(s);
    }

    public PostBuildScriptException(String s, Throwable throwable) {
        super(s, throwable);
    }

    public PostBuildScriptException(Throwable throwable) {
        super(throwable);
    }
}
