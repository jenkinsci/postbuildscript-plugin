package org.jenkinsci.plugins.postbuildscript;

import hudson.Functions;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.model.Result;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;
import static org.junit.Assume.assumeFalse;

public class PostBuildScriptTest {

    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();
    private File outFile;
    private Collection<ScriptFile> scriptFiles;
    private PostBuildScript postBuildScript;
    private FreeStyleBuild build;

    @Before
    public void setUp() throws Exception {
        outFile = File.createTempFile(getClass().getName(), "out");
        outFile.deleteOnExit();
    }

    @Test
    public void executesShellScriptFile() throws Exception {
        assumeFalse(Functions.isWindows());

        givenScriptFiles("/script.sh");
        postBuildScript = new PostBuildScript(
            scriptFiles,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );

        whenBuilt();

        thenSuccessfulBuild();
        thenWroteHelloWorldToFile();

    }

    @Test
    public void executesGroovyScriptFile() throws Exception {

        givenScriptFiles("/script.groovy");
        postBuildScript = new PostBuildScript(
            Collections.emptyList(),
            scriptFiles,
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );

        whenBuilt();

        thenSuccessfulBuild();
        thenWroteHelloWorldToFile();

    }

    private void givenScriptFiles(String scriptFileLocation) throws URISyntaxException {
        String scriptFilePath = getClass().getResource(scriptFileLocation).toURI().getPath();
        String command = scriptFilePath + " " + outFile.getPath();
        ScriptFile scriptFile = new ScriptFile(Collections.singleton("SUCCESS"), command);
        scriptFiles = Collections.singleton(scriptFile);
    }

    private void whenBuilt() throws IOException, InterruptedException, java.util.concurrent.ExecutionException {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.getPublishersList().add(postBuildScript);
        build = project.scheduleBuild2(0).get();
    }

    private void thenWroteHelloWorldToFile() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(outFile.toURI()));
        String outFileContent = new String(encoded, Charset.forName("UTF-8"));
        assertThat(outFileContent, startsWith("Hello world"));
    }

    private void thenSuccessfulBuild() {
        assertThat(build.getResult(), is(Result.SUCCESS));
    }


}
