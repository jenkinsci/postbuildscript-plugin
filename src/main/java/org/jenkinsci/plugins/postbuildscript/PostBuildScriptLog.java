package org.jenkinsci.plugins.postbuildscript;

import hudson.model.TaskListener;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class PostBuildScriptLog implements Serializable {

    private TaskListener listener;

    public PostBuildScriptLog(TaskListener listener) {
        this.listener = listener;
    }

    public void info(String message) {
        listener.getLogger().println("[PostBuildScript] - " + message);
    }

    public void error(String message) {
        listener.getLogger().println("[PostBuildScript] - [ERROR] - " + message);
    }
}
