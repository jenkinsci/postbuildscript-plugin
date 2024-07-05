package org.jenkinsci.plugins.postbuildscript.service;

import hudson.EnvVars;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.postbuildscript.logging.Logger;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.PrintStream;
import java.util.Collections;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@WithJenkins
public class GroovyScriptExecutorIT {

    @Mock
    private Logger log;

    @Mock
    private TaskListener listener;

    @Mock
    private PrintStream printStream;

    @Mock
    private Script script;

    @Test
    public void runsGroovyScriptWithVariablesAndBindings(JenkinsRule jenkinsRule) throws Exception {
        given(script.getContent()).willReturn("log.info('hello $envVar1'); out.println(build.id)");

        FreeStyleProject freeStyleProject = jenkinsRule.createFreeStyleProject();
        FreeStyleBuild executable = freeStyleProject.createExecutable();
        given(log.getListener()).willReturn(listener);
        given(listener.getLogger()).willReturn(printStream);
        EnvVars.masterEnvVars.put("envVar1", "world");
        EnvVars.masterEnvVars.put("envVar2", "jenkins");

        GroovyScriptExecutor callable = new GroovyScriptExecutor(
            script, Collections.emptyList(), executable, log);
        callable.execute();

        verify(log).info("hello world");
        verify(printStream).println(executable.getId());

    }
}
