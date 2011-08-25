package org.jenkinsci.plugins.postbuildscript;

/**
 * @author Gregory Boissinot
 */
public class PostBuildScriptException extends Exception {

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
