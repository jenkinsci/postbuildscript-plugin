package org.jenkinsci.plugins.postbuildscript;

import java.io.IOException;

import hudson.Launcher;
import hudson.matrix.MatrixBuild;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.postbuildscript.processor.Processor;
import org.jenkinsci.plugins.postbuildscript.processor.ProcessorFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

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
    private boolean processed;

    @Test
    public void runsProcessorWithEndOfMatrixBuildEnabled() {

        givenProcessor();
        givenAggregator();
        givenWillProcess();

        whenBuildEnd();

        thenProcesses();
        thenContinuesBuild();

    }

    private void thenProcesses() {
        verify(processor).process(true);
    }

    private void whenBuildEnd() {
        processed = aggregator.endBuild();
    }

    private void givenAggregator() {
        aggregator = new ConfigurableMatrixAggregator(
            build,
            launcher,
            listener,
            processorFactory
        );
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
