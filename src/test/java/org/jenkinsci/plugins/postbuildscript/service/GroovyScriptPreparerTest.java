package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
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

        groovyScriptPreparer.evaluateScript(null);

    }

    @Test
    public void doesNotExecuteScriptIfWorkspaceIsNull() throws Exception {

        GroovyScriptPreparer groovyScriptPreparer = new GroovyScriptPreparer(logger, null, executorFactory);

        boolean evaluated = groovyScriptPreparer.evaluateScript(SCRIPT_CONTENT);

        assertThat(evaluated, is(false));

    }

    @Test
    public void createsExecutorAndActsOnWorkspace() throws Exception {

        given(executorFactory.create(SCRIPT_CONTENT, Collections.emptyList())).willReturn(executor);
        given(executor.call()).willReturn(true);

        boolean evaluated = groovyScriptPreparer.evaluateScript(SCRIPT_CONTENT);

        assertThat(evaluated, is(true));
        verify(executor).call();

    }

    @Test
    public void logsExecutionFailAndReturnsFalse() throws Exception {

        given(executorFactory.create(SCRIPT_CONTENT, Collections.emptyList())).willReturn(executor);
        given(executor.call()).willThrow(new Exception(EXCEPTION_MESSAGE));

        boolean evaluated = groovyScriptPreparer.evaluateScript(SCRIPT_CONTENT);

        assertThat(evaluated, is(false));
        verify(logger).info(startsWith(Messages.PostBuildScript_ProblemOccured("java.lang.Exception: " + EXCEPTION_MESSAGE)));

    }

    @Test
    public void evaluatesFile() throws Exception {

        given(executorFactory.create(startsWith("Hello world"), eq(Collections.emptyList()))).willReturn(executor);
        given(executor.call()).willReturn(true);

        boolean evaluated = groovyScriptPreparer.evaluateCommand(new Command(scriptFile.getName()));

        verify(executorFactory).create(startsWith("Hello world"), eq(Collections.emptyList()));
        verify(executor).call();
        assertThat(evaluated, is(true));

    }

    @Test
    public void doesNotExecuteScriptFileIfWorkspaceIsNull() throws Exception {

        GroovyScriptPreparer groovyScriptPreparer = new GroovyScriptPreparer(logger, null, executorFactory);

        boolean evaluated = groovyScriptPreparer.evaluateCommand(new Command(scriptFile.getName()));

        assertThat(evaluated, is(false));

    }

}
