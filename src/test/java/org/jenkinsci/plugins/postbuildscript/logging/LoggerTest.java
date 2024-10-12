package org.jenkinsci.plugins.postbuildscript.logging;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;

import hudson.EnvVars;
import hudson.model.AbstractBuild;
import hudson.model.TaskListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoggerTest {

    private static final String NEW_LINE = System.getProperty("line.separator");

    private static final String MESSAGE = "message";

    @Mock
    private TaskListener taskListener;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();

    @Mock
    private AbstractBuild<?, ?> build;

    private Logger logger;

    @Mock
    private EnvVars envVars;

    @BeforeEach
    public void letTaskListenerReturnStream() throws IOException, InterruptedException {
        given(build.getEnvironment(taskListener)).willReturn(envVars);
        given(envVars.get("POSTBUILDSCRIPT_VERBOSE", "false")).willReturn("true");

        logger = new Logger(taskListener, build);
    }

    @Test
    public void prefixesInfoMessages() throws IOException {

        givenPrintStream();

        logger.info(MESSAGE);

        out.flush();
        assertThat(out.toString(), is("[PostBuildScript] - [INFO] message" + NEW_LINE));
    }

    @Test
    public void prefixesErrorMessages() throws IOException {

        givenPrintStream();

        logger.error(MESSAGE);

        out.flush();
        assertThat(out.toString(), is("[PostBuildScript] - [ERROR] message" + NEW_LINE));
    }

    @Test
    public void addsThrowable() throws IOException {

        givenPrintStream();

        logger.error(MESSAGE, new RuntimeException("Test exception message"));

        out.flush();
        assertThat(
                out.toString(),
                startsWith("[PostBuildScript] - [ERROR] message" + NEW_LINE
                        + "java.lang.RuntimeException: Test exception message"));
    }

    @Test
    public void fullyQualifiedCallerNameIsClassName() {

        String fullyQualifiedCallerName = logger.getFullyQualifiedCallerName();

        assertThat(fullyQualifiedCallerName, is("org.jenkinsci.plugins.postbuildscript.logging.Logger"));
    }

    private void givenPrintStream() {
        given(taskListener.getLogger()).willReturn(new PrintStream(out, false, StandardCharsets.UTF_8));
    }
}
