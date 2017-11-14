package org.jenkinsci.plugins.postbuildscript;

import hudson.Launcher;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
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
    private ConfigurableMatrixAggregator aggregator;

    private static void thenContinuesBuild(boolean processed) {
        assertThat(processed, is(true));
    }

    @Test
    public void doesNotProcessIfExecutedOnAxes() throws Exception {

        givenProcessor();
        givenAggregator(ExecuteOn.AXES);
        givenWillProcess();

        boolean processed = whenBuildEnd();

        thenContinuesBuild(processed);

    }

    @Test
    public void processesIfExecutionOnBoth() throws Exception {

        givenProcessor();
        givenAggregator(ExecuteOn.BOTH);
        givenWillProcess();

        boolean processed = whenBuildEnd();

        thenProcesses();
        thenContinuesBuild(processed);

    }

    @Test
    public void processesIfExecutionOnMatrix() throws Exception {

        givenProcessor();
        givenAggregator(ExecuteOn.MATRIX);
        givenWillProcess();

        boolean processed = whenBuildEnd();

        thenProcesses();
        thenContinuesBuild(processed);

    }

    private void thenProcesses() {
        verify(processor).process();
    }

    private boolean whenBuildEnd() throws InterruptedException, IOException {
        return aggregator.endBuild();
    }

    private void givenAggregator(ExecuteOn executeOn) {
        aggregator = new ConfigurableMatrixAggregator(
            build,
            launcher,
            listener,
            processorFactory,
            executeOn
        );
    }

    private void givenWillProcess() {
        given(processor.process()).willReturn(true);
    }

    private void givenProcessor() {
        given(processorFactory.create(build, launcher, listener)).willReturn(processor);
    }
}
