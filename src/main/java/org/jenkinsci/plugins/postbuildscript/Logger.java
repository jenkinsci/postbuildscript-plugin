package org.jenkinsci.plugins.postbuildscript;

import hudson.model.TaskListener;

import java.io.Serializable;

/**
 * @author Gregory Boissinot
 */
public class Logger implements Serializable {

    private static final long serialVersionUID = 1083402096308867767L;
    private final TaskListener listener;

    public Logger(TaskListener listener) {
        this.listener = listener;
    }

    public TaskListener getListener() {
        return listener;
    }

    public void info(String message) {
        listener.getLogger().println("[PostBuildScript] - " + message);
    }

}
