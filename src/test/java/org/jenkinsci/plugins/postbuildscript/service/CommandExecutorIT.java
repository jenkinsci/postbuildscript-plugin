package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher.LocalLauncher;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.postbuildscript.logging.Logger;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
@WithJenkins
public class CommandExecutorIT {

    @Mock
    private Logger logger;
    private File scriptFile;
    private CommandExecutor executor;

    @Test
    public void executesCommand(JenkinsRule jenkinsRule) throws Exception {

        scriptFile = File.createTempFile(CommandExecutorIT.class.getName(), ".script");
        scriptFile.deleteOnExit();
        givenExecutor(jenkinsRule);

        int command = executor.executeCommand(new Command(scriptFile.getName() + " param1 param2"));

        assertThat(command, is(0));

    }

    @Test
    public void supportsShebangWithSpacesInFrontOfInterpreter(JenkinsRule jenkinsRule) throws Exception {

        Assumptions.assumeFalse(Functions.isWindows());
        scriptFile = new File(getClass().getResource("/shebang_with_spaces.sh").toURI());
        givenExecutor(jenkinsRule);

        int command = executor.executeCommand(new Command(scriptFile.getName() + " param1 param2"));

        assertThat(command, is(0));

    }

    private void givenExecutor(JenkinsRule jenkinsRule) {
        LocalLauncher launcher = jenkinsRule.createLocalLauncher();
        FilePath workspace = new FilePath(scriptFile.getParentFile());
        TaskListener listener = jenkinsRule.createTaskListener();
        executor = new CommandExecutor(logger, listener, workspace, launcher);
    }

}
