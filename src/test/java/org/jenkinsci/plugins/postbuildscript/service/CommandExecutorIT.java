package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.Functions;
import hudson.Launcher.LocalLauncher;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.postbuildscript.logging.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeFalse;

@RunWith(MockitoJUnitRunner.class)
public class CommandExecutorIT {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    @Mock
    private Logger logger;
    private File scriptFile;
    private CommandExecutor executor;

    @Test
    public void executesCommand() throws Exception {

        scriptFile = File.createTempFile(CommandExecutorIT.class.getName(), ".script");
        scriptFile.deleteOnExit();
        givenExecutor();

        int command = executor.executeCommand(new Command(scriptFile.getName() + " param1 param2"));

        assertThat(command, is(0));

    }

    @Test
    public void supportsShebangWithSpacesInFrontOfInterpreter() throws Exception {

        assumeFalse(Functions.isWindows());
        scriptFile = new File(getClass().getResource("/shebang_with_spaces.sh").toURI());
        givenExecutor();

        int command = executor.executeCommand(new Command(scriptFile.getName() + " param1 param2"));

        assertThat(command, is(0));

    }

    private void givenExecutor() {
        LocalLauncher launcher = jenkinsRule.createLocalLauncher();
        FilePath workspace = new FilePath(scriptFile.getParentFile());
        TaskListener listener = jenkinsRule.createTaskListener();
        executor = new CommandExecutor(logger, listener, workspace, launcher);
    }

}
