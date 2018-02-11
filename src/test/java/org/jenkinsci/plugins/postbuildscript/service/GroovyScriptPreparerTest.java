package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.Messages;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
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
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private Script script;

    @Mock
    private ScriptFile scriptFile;

    private GroovyScriptPreparer groovyScriptPreparer;

    private File file;

    public GroovyScriptPreparerTest() {
    }

    @Before
    public void initPreparer() throws IOException, URISyntaxException {
        file = new File(getClass().getResource("/test_script").toURI());
        FilePath workspace = new FilePath(file.getParentFile());
        groovyScriptPreparer = new GroovyScriptPreparer(logger, workspace, executorFactory);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failsIfNoScriptContent() throws Exception {

        groovyScriptPreparer.evaluateScript(null);

    }

    @Test
    public void doesNotExecuteScriptIfWorkspaceIsNull() throws Exception {
        given(script.getContent()).willReturn(SCRIPT_CONTENT);
        GroovyScriptPreparer groovyScriptPreparer = new GroovyScriptPreparer(logger, null, executorFactory);

        boolean evaluated = groovyScriptPreparer.evaluateScript(script);

        assertThat(evaluated, is(false));

    }

    @Test
    public void createsExecutorAndActsOnWorkspace() throws Exception {
        given(script.getContent()).willReturn(SCRIPT_CONTENT);
        given(executorFactory.create(script, Collections.emptyList())).willReturn(executor);
        given(executor.call()).willReturn(true);

        boolean evaluated = groovyScriptPreparer.evaluateScript(script);

        assertThat(evaluated, is(true));
        verify(executor).call();

    }

    @Test
    public void logsExecutionFailAndReturnsFalse() throws Exception {
        given(script.getContent()).willReturn(SCRIPT_CONTENT);
        given(executorFactory.create(script, Collections.emptyList())).willReturn(executor);
        given(executor.call()).willThrow(new Exception(EXCEPTION_MESSAGE));

        boolean evaluated = groovyScriptPreparer.evaluateScript(script);

        assertThat(evaluated, is(false));
        verify(logger).info(startsWith(Messages.PostBuildScript_ProblemOccured("java.lang.Exception: " + EXCEPTION_MESSAGE)));

    }

    @Test
    public void evaluatesFile() throws Exception {

        given(executorFactory.create(any(Script.class), eq(Collections.emptyList()))).willReturn(executor);
        given(executor.call()).willReturn(true);

        boolean evaluated = groovyScriptPreparer.evaluateCommand(scriptFile, new Command(file.getName()));

        verify(executorFactory).create(any(Script.class), eq(Collections.emptyList()));
        verify(executor).call();
        assertThat(evaluated, is(true));

    }

    @Test
    public void doesNotExecuteScriptFileIfWorkspaceIsNull() throws Exception {

        GroovyScriptPreparer groovyScriptPreparer = new GroovyScriptPreparer(logger, null, executorFactory);

        boolean evaluated = groovyScriptPreparer.evaluateCommand(scriptFile, new Command(file.getName()));

        assertThat(evaluated, is(false));

    }

}
