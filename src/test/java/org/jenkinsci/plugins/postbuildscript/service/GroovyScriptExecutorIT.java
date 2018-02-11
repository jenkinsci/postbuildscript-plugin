package org.jenkinsci.plugins.postbuildscript.service;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.PrintStream;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptExecutorIT {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Mock
    private Logger log;

    @Mock
    private TaskListener listener;

    @Mock
    private PrintStream printStream;

    @Mock
    private Script script;

    @Test
    public void runsGroovyScriptWithVariablesAndBindings() throws Exception {
        given(script.getContent()).willReturn("log.info('hello $envVar1'); out.println(build.id)");

        FreeStyleProject freeStyleProject = jenkinsRule.createFreeStyleProject();
        FreeStyleBuild executable = freeStyleProject.createExecutable();
        given(log.getListener()).willReturn(listener);
        given(listener.getLogger()).willReturn(printStream);
        EnvVars.masterEnvVars.put("envVar1", "world");
        EnvVars.masterEnvVars.put("envVar2", "jenkins");

        GroovyScriptExecutor callable = new GroovyScriptExecutor(
            script, Collections.emptyList(), executable, log);
        Boolean result = callable.call();

        assertThat(result, is(true));
        verify(log).info("hello world");
        verify(printStream).println(executable.getId());

    }
}
