package org.jenkinsci.plugins.postbuildscript;

import hudson.Functions;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStep;
import hudson.tasks.Builder;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;

public class PostBuildScriptIT {

    private static final Set<String> SUCCESS_RESULTS = Collections.singleton("SUCCESS");

    @Rule
    public final JenkinsRule jenkinsRule = new JenkinsRule();
    private File outFile;
    private Collection<ScriptFile> scriptFiles;
    private PostBuildScript postBuildScript;
    private FreeStyleBuild build;
    private final Collection<BuildStep> buildSteps = new ArrayList<>();
    private TestBuildStep firstBuildStep;
    private TestBuildStep secondBuildStep;

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

        assumeFalse(Functions.isWindows());

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

        givenOutfile();
        String scriptContent = String.format("def out = new File(\"%s\")%nout << \"Hello world\"", outFile.getPath().replace("\\", "\\\\")); //NON-NLS
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
        assertEquals(1, firstBuildStep.getInvocations());

    }

    @Test
    public void executesPostBuildStepRegardlessOfFailures() throws Exception {

        givenFailingFirstBuildStep();
        givenSecondBuildStep();
        givenPostBuildStep(false);

        whenBuilt();

        thenFailedBuild();
        assertEquals(1, firstBuildStep.getInvocations());
        assertEquals(1, secondBuildStep.getInvocations());

    }

    @Test
    public void stopOnBuildStepFailure() throws Exception {

        givenFailingFirstBuildStep();
        givenSecondBuildStep();
        givenPostBuildStep(true);

        whenBuilt();

        thenFailedBuild();
        assertEquals(1, firstBuildStep.getInvocations());
        assertEquals(0, secondBuildStep.getInvocations());

    }

    private void givenSuccessfulFirstBuildStep() throws InterruptedException, IOException {
        firstBuildStep = new TestBuildStep(true);
        buildSteps.add(firstBuildStep);
    }

    private void givenFailingFirstBuildStep() throws InterruptedException, IOException {
        firstBuildStep = new TestBuildStep(false);
        buildSteps.add(firstBuildStep);
    }

    private void givenSecondBuildStep() {
        secondBuildStep = new TestBuildStep(false);
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

    private void givenScriptFiles(String scriptFileLocation) throws Exception {
        Path scriptFilePath = Files.createTempFile("script", ".groovy");
        Files.copy(getClass().getResourceAsStream(scriptFileLocation), scriptFilePath, StandardCopyOption.REPLACE_EXISTING);
        String command = '"' + scriptFilePath.toString() + "\" " + outFile.getPath();
        ScriptFile scriptFile = new ScriptFile(SUCCESS_RESULTS, command);
        scriptFiles = Collections.singleton(scriptFile);
    }

    private void whenBuilt() throws IOException, InterruptedException, ExecutionException {
        FreeStyleProject project = jenkinsRule.createFreeStyleProject();
        project.getPublishersList().add(postBuildScript);
        build = project.scheduleBuild2(0).get();
    }

    private void thenWroteHelloWorldToFile() throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(outFile.toURI()));
        String outFileContent = new String(encoded, StandardCharsets.UTF_8);
        assertThat(outFileContent, startsWith("Hello world"));
    }

    private void thenSuccessfulBuild() {
        assertThat(build.getResult(), is(Result.SUCCESS));
    }

    private void thenFailedBuild() {
        assertThat(build.getResult(), is(Result.FAILURE));
    }

    private static class TestBuildStep extends TestBuilder {
        private final boolean result;
        private volatile int invocations;

        public TestBuildStep(boolean result) {
            this.result = result;
        }

        public int getInvocations() {
            return invocations;
        }

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
            invocations += 1;
            return result;
        }
    }
}
