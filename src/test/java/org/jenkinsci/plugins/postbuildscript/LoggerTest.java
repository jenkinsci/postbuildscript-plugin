package org.jenkinsci.plugins.postbuildscript;

import hudson.model.TaskListener;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.PrintStream;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LoggerTest {

    private static final String MESSAGE = "message";

    @Mock
    private TaskListener taskListener;

    @Mock
    private PrintStream printStream;

    @InjectMocks
    private Logger logger;

    @Before
    public void letTaskListenerReturnStream() {
        given(taskListener.getLogger()).willReturn(printStream);
    }

    @Test
    public void prefixesInfoMessages() {

        logger.info(MESSAGE);

        verify(printStream).println(Messages.PostBuildScript_LogPrefix(MESSAGE));

    }

    @Test
    public void prefixesErrorMessages() {

        logger.error(MESSAGE);

        verify(printStream).println(Messages.PostBuildScript_LogPrefix(
            Messages.PostBuildScript_ErrorPrefix(MESSAGE)));

    }

}
