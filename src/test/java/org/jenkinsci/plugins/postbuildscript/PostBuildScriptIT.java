package org.jenkinsci.plugins.postbuildscript;

import hudson.AbortException;
import hudson.Functions;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStep;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;
import org.jvnet.hudson.test.junit.jupiter.WithJenkins;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

@WithJenkins
public class PostBuildScriptIT {

    private static final Set<String> SUCCESS_RESULTS = Collections.singleton("SUCCESS");

    private File outFile;
    private Collection<ScriptFile> scriptFiles;
    private PostBuildScript postBuildScript;
    private FreeStyleBuild build;
    private final Collection<BuildStep> buildSteps = new ArrayList<>();
    private TestBuildStep firstBuildStep;
    private TestBuildStep secondBuildStep;
    private TestAbortingBuildStep abortingBuildStep;

    @Test
    public void executesShellScriptFile(JenkinsRule jenkinsRule) throws Exception {
        Assumptions.assumeFalse(Functions.isWindows());

        givenOutfile();
        givenScriptFiles("/script.sh"); //NON-NLS
        postBuildScript = new PostBuildScript(
            scriptFiles,
            Collections.emptyList(),
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );

        whenBuilt(jenkinsRule);

        thenSuccessfulBuild();
        thenWroteHelloWorldToFile();
    }

    @Test
    public void executesGroovyScriptFile(JenkinsRule jenkinsRule) throws Exception {

        Assumptions.assumeFalse(Functions.isWindows());

        givenOutfile();
        givenScriptFiles("/script.groovy"); //NON-NLS
        postBuildScript = new PostBuildScript(
            Collections.emptyList(),
            scriptFiles,
            Collections.emptyList(),
            Collections.emptyList(),
            false
        );

        whenBuilt(jenkinsRule);

        thenSuccessfulBuild();
        thenWroteHelloWorldToFile();
    }

    @Test
    public void executesGroovyScript(JenkinsRule jenkinsRule) throws Exception {

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

        whenBuilt(jenkinsRule);

        thenSuccessfulBuild();
        thenWroteHelloWorldToFile();
    }

    @Test
    public void executesPostBuildStep(JenkinsRule jenkinsRule) throws Exception {

        givenSuccessfulFirstBuildStep();
        givenPostBuildStep(false);

        whenBuilt(jenkinsRule);

        thenSuccessfulBuild();
        Assertions.assertEquals(1, firstBuildStep.getInvocations());

    }

    @Test
    public void executesPostBuildStepRegardlessOfFailures(JenkinsRule jenkinsRule) throws Exception {

        givenAbortingBuildStep();
        givenFailingFirstBuildStep();
        givenSecondBuildStep();
        givenPostBuildStep(false);

        whenBuilt(jenkinsRule);

        thenFailedBuild();
        Assertions.assertEquals(1, firstBuildStep.getInvocations());
        Assertions.assertEquals(1, secondBuildStep.getInvocations());

    }

    @Test
    public void stopOnBuildStepFailure(JenkinsRule jenkinsRule) throws Exception {

        givenFailingFirstBuildStep();
        givenSecondBuildStep();
        givenPostBuildStep(true);

        whenBuilt(jenkinsRule);

        thenFailedBuild();
        Assertions.assertEquals(1, firstBuildStep.getInvocations());
        Assertions.assertEquals(0, secondBuildStep.getInvocations());

    }

    @Test
    public void handlesAbortException(JenkinsRule jenkinsRule) throws Exception {
        givenAbortingBuildStep();
        givenSecondBuildStep();
        givenPostBuildStep(true);

        whenBuilt(jenkinsRule);

        thenNoProblemOccured(jenkinsRule);
        thenFailedBuild();
        Assertions.assertEquals(0, secondBuildStep.getInvocations());
    }

    private void givenSuccessfulFirstBuildStep() {
        firstBuildStep = new TestBuildStep(true);
        buildSteps.add(firstBuildStep);
    }

    private void givenFailingFirstBuildStep() {
        firstBuildStep = new TestBuildStep(false);
        buildSteps.add(firstBuildStep);
    }

    private void givenSecondBuildStep() {
        secondBuildStep = new TestBuildStep(false);
        buildSteps.add(secondBuildStep);
    }

    private void givenAbortingBuildStep() {
        abortingBuildStep = new TestAbortingBuildStep();
        buildSteps.add( abortingBuildStep );
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

    private void whenBuilt(JenkinsRule jenkinsRule) throws IOException, InterruptedException, ExecutionException {
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

    private void thenNoProblemOccured(JenkinsRule jenkinsRule) throws IOException {
        jenkinsRule.assertLogNotContains(Messages.PostBuildScript_ProblemOccured(), build);
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
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
            invocations += 1;
            return result;
        }
    }

    private static class TestAbortingBuildStep extends TestBuilder {

        @Override
        public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws AbortException {
            throw new AbortException();
        }

    }
}
