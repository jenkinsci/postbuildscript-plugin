package org.jenkinsci.plugins.postbuildscript.service;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptExecutionCallableTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();

    @Mock
    private Logger log;

    @Mock
    private TaskListener listener;

    @Mock
    private PrintStream printStream;

    @Test
    public void runsGroovyScriptWithVariablesAndBindings() throws Exception {

        FreeStyleProject freeStyleProject = jenkinsRule.createFreeStyleProject();
        FreeStyleBuild executable = freeStyleProject.createExecutable();
        given(log.getListener()).willReturn(listener);
        given(listener.getLogger()).willReturn(printStream);
        EnvVars.masterEnvVars.put("envVar1", "world");
        EnvVars.masterEnvVars.put("envVar2", "jenkins");

        GroovyScriptExecutionCallable callable = new GroovyScriptExecutionCallable(
            "log.info('hello $envVar1'); out.println(build.id)", executable, log);
        Boolean result = callable.call();

        assertThat(result, is(true));
        verify(log).info("hello world");
        verify(printStream).println(executable.getId());

    }
}
