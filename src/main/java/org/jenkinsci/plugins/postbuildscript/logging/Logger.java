package org.jenkinsci.plugins.postbuildscript.logging;

import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.helpers.MessageFormatter;

import javax.annotation.Nullable;
import java.io.PrintStream;

/**
 * @author Daniel Heid
 */
public class Logger extends MarkerIgnoringBase {

    private static final long serialVersionUID = 1083402096308867767L;

    private static final String ERROR_PREFIX = "ERROR"; //NON-NLS
    private static final String WARN_PREFIX = "WARN"; //NON-NLS
    private static final String INFO_PREFIX = "INFO"; //NON-NLS
    private static final String DEBUG_PREFIX = "DEBUG"; //NON-NLS
    private static final String TRACE_PREFIX = "TRACE"; //NON-NLS

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
    public void error(String msg) {
        printToLogger(ERROR_PREFIX, msg, null);
    }

    @Override
    public void error(String format, Object arg) {
        FormattingTuple formattingTuple = MessageFormatter.format(format, arg);
        printToLogger(ERROR_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }

    @Override
    public void error(String format, Object arg1, Object arg2) {
        FormattingTuple formattingTuple = MessageFormatter.format(format, arg1, arg2);
        printToLogger(ERROR_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }

    @Override
    public void error(String format, Object... arguments) {
        FormattingTuple formattingTuple = MessageFormatter.format(format, arguments);
        printToLogger(ERROR_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }

    @Override
    public void error(String msg, Throwable t) {
        printToLogger(ERROR_PREFIX, msg, t);
    }

    @Override
    public void warn(String msg) {
        printToLogger(WARN_PREFIX, msg, null);
    }

    @Override
    public void warn(String format, Object arg) {
        FormattingTuple formattingTuple = MessageFormatter.format(format, arg);
        printToLogger(WARN_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }

    @Override
    public void warn(String format, Object arg1, Object arg2) {
        FormattingTuple formattingTuple = MessageFormatter.format(format, arg1, arg2);
        printToLogger(WARN_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }

    @Override
    public void warn(String format, Object... arguments) {
        FormattingTuple formattingTuple = MessageFormatter.format(format, arguments);
        printToLogger(WARN_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }

    @Override
    public void warn(String msg, Throwable t) {
        printToLogger(WARN_PREFIX, msg, t);
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
    public void trace(String msg) {
        if (verbose) {
            printToLogger(TRACE_PREFIX, msg, null);
        }
    }

    @Override
    public void trace(String format, Object arg) {
        if (verbose) {
            FormattingTuple formattingTuple = MessageFormatter.format(format, arg);
            printToLogger(TRACE_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
        }
    }

    @Override
    public void trace(String format, Object arg1, Object arg2) {
        if (verbose) {
            FormattingTuple formattingTuple = MessageFormatter.format(format, arg1, arg2);
            printToLogger(TRACE_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
        }
    }

    @Override
    public void trace(String format, Object... arguments) {
        if (verbose) {
            FormattingTuple formattingTuple = MessageFormatter.format(format, arguments);
            printToLogger(TRACE_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
        }
    }

    @Override
    public void trace(String msg, Throwable t) {
        if (verbose) {
            printToLogger(TRACE_PREFIX, msg, t);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return verbose;
    }

    @Override
    public void debug(String msg) {
        if (verbose) {
            printToLogger(DEBUG_PREFIX, msg, null);
        }
    }

    @Override
    public void debug(String format, Object arg) {
        if (verbose) {
            FormattingTuple formattingTuple = MessageFormatter.format(format, arg);
            printToLogger(DEBUG_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
        }
    }

    @Override
    public void debug(String format, Object arg1, Object arg2) {
        if (verbose) {
            FormattingTuple formattingTuple = MessageFormatter.format(format, arg1, arg2);
            printToLogger(DEBUG_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
        }
    }

    @Override
    public void debug(String format, Object... arguments) {
        if (verbose) {
            FormattingTuple formattingTuple = MessageFormatter.format(format, arguments);
            printToLogger(DEBUG_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
        }
    }

    @Override
    public void debug(String msg, Throwable t) {
        if (verbose) {
            printToLogger(DEBUG_PREFIX, msg, t);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return true;
    }

    @Override
    public void info(String msg) {
        printToLogger(INFO_PREFIX, msg, null);
    }

    @Override
    public void info(String format, Object arg) {
        FormattingTuple formattingTuple = MessageFormatter.format(format, arg);
        printToLogger(INFO_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }

    @Override
    public void info(String format, Object arg1, Object arg2) {
        FormattingTuple formattingTuple = MessageFormatter.format(format, arg1, arg2);
        printToLogger(INFO_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }

    @Override
    public void info(String format, Object... arguments) {
        FormattingTuple formattingTuple = MessageFormatter.format(format, arguments);
        printToLogger(INFO_PREFIX, formattingTuple.getMessage(), formattingTuple.getThrowable());
    }

    @Override
    public void info(String msg, Throwable t) {
        printToLogger(INFO_PREFIX, msg, t);
    }

    @Override
    public boolean isWarnEnabled() {
        return true;
    }

    private void printToLogger(String prefix, @Nullable String message, @Nullable Throwable t) {
        PrintStream logger = listener.getLogger();
        logger.print("[PostBuildScript] - "); //NON-NLS
        logger.print('[');
        logger.print(prefix);
        logger.print("] ");
        logger.println(message);
        if (t != null) {
            t.printStackTrace(logger);
        }
    }

}
