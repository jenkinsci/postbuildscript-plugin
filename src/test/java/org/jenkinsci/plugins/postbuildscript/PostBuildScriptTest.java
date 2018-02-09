package org.jenkinsci.plugins.postbuildscript;

import com.thoughtworks.xstream.XStream;
import hudson.tasks.BatchFile;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class PostBuildScriptTest {

    private PostBuildScript postBuildScript;
    private PostBuildScript resolvedPostBuildScript;

    @Test
    public void returnsSameInstanceOnResolve() {

        givenScriptFromConfig("/v0.18_config_a.xml");

        whenReadResolves();

        assertThat(resolvedPostBuildScript, is(postBuildScript));

    }

    @Test
    public void markBuildUnstableIsTrue() {

        givenScriptFromConfig("/v0.18_config_a.xml");

        whenReadResolves();

        assertThat(resolvedPostBuildScript.isMarkBuildUnstable(), is(true));

    }

    @Test
    public void containsBatchFileStep() {

        givenScriptFromConfig("/v0.18_config_a.xml");

        whenReadResolves();

        PostBuildStep postBuildStep = resolvedPostBuildScript.getBuildSteps().get(0);
        assertThat(postBuildStep.getBuildSteps(), contains(
            allOf(
                instanceOf(BatchFile.class),
                Matchers.hasProperty("command", is("somecommand")))
        ));

    }

    @Test
    public void noScriptOnlyActivatedSelectsEveryResult() {

        givenScriptFromConfig("/v0.18_config_a.xml");

        whenReadResolves();

        PostBuildStep postBuildStep = resolvedPostBuildScript.getBuildSteps().get(0);
        assertThat(postBuildStep.getResults(), containsInAnyOrder("SUCCESS", "UNSTABLE", "FAILURE", "NOT_BUILT", "ABORTED"));

    }

    @Test
    public void scriptOnlyIfSuccessSelectsSuccessResult() {

        givenScriptFromConfig("/v0.18_config_b.xml");

        whenReadResolves();

        PostBuildStep postBuildStep = resolvedPostBuildScript.getBuildSteps().get(0);
        assertThat(postBuildStep.getResults(), containsInAnyOrder("SUCCESS"));

    }

    @Test
    public void scriptOnlyIfFailureSelectsFailure() {

        givenScriptFromConfig("/v0.18_config_c.xml");

        whenReadResolves();

        PostBuildStep postBuildStep = resolvedPostBuildScript.getBuildSteps().get(0);
        assertThat(postBuildStep.getResults(), containsInAnyOrder("FAILURE"));

    }

    @Test
    public void bothScriptOnlyActivatedSelectsSuccessAndFailure() {

        givenScriptFromConfig("/v0.18_config_d.xml");

        whenReadResolves();

        PostBuildStep postBuildStep = resolvedPostBuildScript.getBuildSteps().get(0);
        assertThat(postBuildStep.getResults(), containsInAnyOrder("SUCCESS", "FAILURE"));

    }

    void whenReadResolves() {
        resolvedPostBuildScript = (PostBuildScript) postBuildScript.readResolve();
    }

    void givenScriptFromConfig(String configResourceName) {
        XStream xstream = new XStream();
        postBuildScript = (PostBuildScript) xstream.fromXML(getClass().getResource(configResourceName));
    }
}
