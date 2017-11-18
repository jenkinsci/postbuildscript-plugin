package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.Launcher.LocalLauncher;
import hudson.model.TaskListener;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.jvnet.hudson.test.JenkinsRule;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class CommandExecutorTest {

    @Rule
    public JenkinsRule jenkinsRule = new JenkinsRule();
    @Mock
    private Logger logger;

    @Test
    public void executesCommand() throws Exception {

        LocalLauncher launcher = jenkinsRule.createLocalLauncher();
        File tempFile = File.createTempFile(CommandExecutorTest.class.getName(), ".script");
        tempFile.deleteOnExit();
        FilePath workspace = new FilePath(tempFile.getParentFile());
        TaskListener listener = jenkinsRule.createTaskListener();
        CommandExecutor executor = new CommandExecutor(logger, listener, workspace, launcher);

        int command = executor.executeCommand(new Command(tempFile.getName() + " param1 param2"));

        assertThat(command, is(0));

    }
}
