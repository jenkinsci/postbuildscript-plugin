package org.jenkinsci.plugins.postbuildscript.service;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import hudson.EnvVars;
import hudson.FilePath;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptPreparerTest {

    private static final String SCRIPT_CONTENT = "scriptContent";
    private static final String EXCEPTION_MESSAGE = "exceptionMessage";

    @Mock
    private Logger logger;

    @Mock
    private GroovyScriptExecutorFactory executorFactory;

    @Mock
    private GroovyScriptExecutor executor;

    private GroovyScriptPreparer groovyScriptPreparer;

    private File scriptFile;

    @Before
    public void initPreparer() throws IOException, URISyntaxException {
        scriptFile = new File(getClass().getResource("/test_script").toURI());
        FilePath workspace = new FilePath(scriptFile.getParentFile());
        groovyScriptPreparer = new GroovyScriptPreparer(logger, workspace, executorFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfNoScriptContent() throws Exception {

        groovyScriptPreparer.evaluate(null);

    }

    @Test
    public void doesNotExecuteScriptIfWorkspaceIsNull() throws Exception {

        GroovyScriptPreparer groovyScriptPreparer = new GroovyScriptPreparer(logger, null, executorFactory);

        boolean evaluated = groovyScriptPreparer.evaluate(SCRIPT_CONTENT);

        assertThat(evaluated, is(false));

    }

    @Test
    public void createsExecutorAndActsOnWorkspace() throws Exception {

        given(executorFactory.create(SCRIPT_CONTENT)).willReturn(executor);
        given(executor.call()).willReturn(true);

        boolean evaluated = groovyScriptPreparer.evaluate(SCRIPT_CONTENT);

        assertThat(evaluated, is(true));
        verify(executor).call();

    }

    @Test
    public void logsExecutionFailAndReturnsFalse() throws Exception {

        given(executorFactory.create(SCRIPT_CONTENT)).willReturn(executor);
        given(executor.call()).willThrow(new Exception(EXCEPTION_MESSAGE));

        boolean evaluated = groovyScriptPreparer.evaluate(SCRIPT_CONTENT);

        assertThat(evaluated, is(false));
        verify(logger).info(Messages.PostBuildScript_ProblemOccured(EXCEPTION_MESSAGE));

    }

    @Test
    public void evaluatesFile() throws Exception {

        EnvVars.masterEnvVars.put("name", "world");
        given(executorFactory.create(startsWith("Hello world"))).willReturn(executor);
        given(executor.call()).willReturn(true);

        boolean evaluated = groovyScriptPreparer.evaluateFile(scriptFile.getName());

        verify(executorFactory).create(startsWith("Hello world"));
        verify(executor).call();
        assertThat(evaluated, is(true));

    }

    @Test
    public void doesNotExecuteScriptFileIfWorkspaceIsNull() throws Exception {

        GroovyScriptPreparer groovyScriptPreparer = new GroovyScriptPreparer(logger, null, executorFactory);

        boolean evaluated = groovyScriptPreparer.evaluateFile(scriptFile.getName());

        assertThat(evaluated, is(false));

    }

}
