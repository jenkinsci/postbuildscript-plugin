package org.jenkinsci.plugins.postbuildscript.logging;

import org.slf4j.Marker;
import org.slf4j.event.Level;
import org.slf4j.helpers.LegacyAbstractLogger;
import org.slf4j.helpers.MessageFormatter;

import java.io.PrintStream;

import hudson.model.AbstractBuild;
import hudson.model.TaskListener;

/**
 * @author Daniel Heid
 */
public class Logger extends LegacyAbstractLogger {

    private static final long serialVersionUID = 1083402096308867767L;

    private static final String FULLY_QUALIFIED_CALLER_NAME = Logger.class.getName();

    private final TaskListener listener;
    private final boolean verbose;

    public Logger(TaskListener listener, AbstractBuild<?, ?> build) {
        this.listener = listener;
        try {
            verbose = Boolean.parseBoolean(
                build.getEnvironment(listener).get("POSTBUILDSCRIPT_VERBOSE", "false") //NON-NLS
            );
        } catch (Exception e) {
            throw new LoggerInitializationException(e);
        }
    }

    public TaskListener getListener() {
        return listener;
    }

    @Override
    protected String getFullyQualifiedCallerName() {
        return FULLY_QUALIFIED_CALLER_NAME;
    }

    @Override
    protected void handleNormalizedLoggingCall(Level level, Marker marker, String messagePattern, Object[] arguments, Throwable throwable) {
        PrintStream logger = listener.getLogger();
        logger.print("[PostBuildScript] - "); //NON-NLS
        logger.print('[');
        logger.print(level.name());
        logger.print("] ");
        logger.println(MessageFormatter.basicArrayFormat(messagePattern, arguments));
        if (throwable != null) {
            throwable.printStackTrace(logger);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return true;
    }

    @Override
    public boolean isTraceEnabled() {
        return verbose;
    }

    @Override
    public boolean isDebugEnabled() {
        return verbose;
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

}
