package org.jenkinsci.plugins.postbuildscript;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixRun;
import hudson.model.BuildListener;
import java.io.IOException;
import java.io.PrintStream;
import org.jenkinsci.plugins.postbuildscript.processor.Processor;
import org.jenkinsci.plugins.postbuildscript.processor.ProcessorFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ConfigurableMatrixAggregatorTest {

    @Mock
    private MatrixBuild build;

    @Mock
    private Launcher launcher;

    @Mock
    private BuildListener listener;

    @Mock
    private ProcessorFactory processorFactory;

    @Mock
    private Processor processor;

    @Mock
    private PrintStream logger;

    @Mock
    private MatrixRun run;

    @Mock
    private EnvVars envVars;

    private ConfigurableMatrixAggregator aggregator;
    private boolean processed;

    @Test
    public void runsProcessorWithEndOfMatrixBuildEnabled() throws IOException, InterruptedException {

        givenProcessor();
        givenAggregator();
        givenWillProcess();

        whenBuildEnd();

        thenProcesses();
        thenContinuesBuild();
    }

    @Test
    public void addsNewLineToLoggerAfterRun() throws Exception {

        givenAggregator();

        boolean canContinue = aggregator.endRun(run);

        assertThat(canContinue, is(true));
        verify(logger).println();
        verifyNoMoreInteractions(run);
    }

    private void thenProcesses() {
        verify(processor).process(true);
    }

    private void whenBuildEnd() {
        processed = aggregator.endBuild();
    }

    private void givenAggregator() throws IOException, InterruptedException {
        given(build.getEnvironment(listener)).willReturn(envVars);
        given(envVars.get("POSTBUILDSCRIPT_VERBOSE", "false")).willReturn("true");
        given(listener.getLogger()).willReturn(logger);
        aggregator = new ConfigurableMatrixAggregator(
                build, launcher, listener, processorFactory, MatrixPostBuildScript.class);
    }

    private void givenWillProcess() {
        given(processor.process(true)).willReturn(true);
    }

    private void givenProcessor() {
        given(processorFactory.createMatrixProcessor(build, launcher, listener)).willReturn(processor);
    }

    private void thenContinuesBuild() {
        assertThat(processed, is(true));
    }
}
