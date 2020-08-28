package org.jenkinsci.plugins.postbuildscript;

import hudson.Functions;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStep;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assume.assumeFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

public class PostBuildScriptIT {

    private static final Set<String> SUCCESS_RESULTS = Collections.singleton("SUCCESS");

    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();
    private File outFile;
    private Collection<ScriptFile> scriptFiles;
    private PostBuildScript postBuildScript;
    private FreeStyleBuild build;
    private final Collection<BuildStep> buildSteps = new ArrayList<>();
    private BuildStep firstBuildStep;
    private BuildStep secondBuildStep;

    @Test
    public void executesShellScriptFile() throws Exception {
        assumeFalse(Functions.isWindows());

        givenOutfile();
        givenScriptFiles("/script.sh"); //NON-NLS
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

        givenOutfile();
        givenScriptFiles("/script.groovy"); //NON-NLS
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

    @Test
    public void executesGroovyScript() throws Exception {
        assumeFalse(Functions.isWindows());

        givenOutfile();
        String scriptContent = String.format("def out = new File(\"%s\")%nout << \"Hello world\"", outFile.getPath()); //NON-NLS
        Script script = new Script(SUCCESS_RESULTS, scriptContent);
        Collection<Script> scripts = Collections.singleton(script);
        postBuildScript = new PostBuildScript(
            Collections.emptyList(),
            Collections.emptyList(),
            scripts,
            Collections.emptyList(),
            false
        );

        whenBuilt();

        thenSuccessfulBuild();
        thenWroteHelloWorldToFile();
    }

    @Test
    public void executesPostBuildStep() throws Exception {

        givenSuccessfulFirstBuildStep();
        givenPostBuildStep(false);

        whenBuilt();

        thenSuccessfulBuild();
        verify(firstBuildStep).perform(eq(build), any(Launcher.class), any(BuildListener.class));

    }

    @Test
    public void executesPostBuildStepRegardlessOfFailures() throws Exception {

        givenFailingFirstBuildStep();
        givenSecondBuildStep();
        givenPostBuildStep(false);

        whenBuilt();

        thenFailedBuild();
        verify(firstBuildStep).perform(eq(build), any(Launcher.class), any(BuildListener.class));
        verify(secondBuildStep).perform(eq(build), any(Launcher.class), any(BuildListener.class));

    }

    @Test
    public void stopOnBuildStepFailure() throws Exception {

        givenFailingFirstBuildStep();
        givenSecondBuildStep();
        givenPostBuildStep(true);

        whenBuilt();

        thenFailedBuild();
        verify(firstBuildStep).perform(eq(build), any(Launcher.class), any(BuildListener.class));
        verify(secondBuildStep, never()).perform(eq(build), any(Launcher.class), any(BuildListener.class));

    }

    private void givenSuccessfulFirstBuildStep() throws InterruptedException, IOException {
        firstBuildStep = mock(BuildStep.class);
        given(firstBuildStep.perform(any(AbstractBuild.class), any(Launcher.class), any(BuildListener.class))).willReturn(true);
        buildSteps.add(firstBuildStep);
    }

    private void givenFailingFirstBuildStep() throws InterruptedException, IOException {
        firstBuildStep = mock(BuildStep.class);
        given(firstBuildStep.perform(any(AbstractBuild.class), any(Launcher.class), any(BuildListener.class))).willReturn(false);
        buildSteps.add(firstBuildStep);
    }

    private void givenSecondBuildStep() {
        secondBuildStep = mock(BuildStep.class);
        buildSteps.add(secondBuildStep);
    }

    private void givenPostBuildStep(boolean stopOnFailure) {
        PostBuildStep step = new PostBuildStep(SUCCESS_RESULTS, buildSteps, stopOnFailure);
        Collection<PostBuildStep> steps = Collections.singleton(step);
        postBuildScript = new PostBuildScript(
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            steps,
            false
        );
    }

    private void givenOutfile() throws Exception {
        outFile = File.createTempFile(getClass().getName(), ".out");
        outFile.deleteOnExit();
    }

    private void givenScriptFiles(String scriptFileLocation) throws URISyntaxException {
        String scriptFilePath = getClass().getResource(scriptFileLocation).toURI().getPath();
        String command = scriptFilePath + " " + outFile.getPath();
        ScriptFile scriptFile = new ScriptFile(SUCCESS_RESULTS, command);
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

    private void thenFailedBuild() {
        assertThat(build.getResult(), is(Result.FAILURE));
    }


}
