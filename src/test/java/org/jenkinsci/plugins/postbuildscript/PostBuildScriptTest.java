package org.jenkinsci.plugins.postbuildscript;

import com.thoughtworks.xstream.XStream;
import hudson.tasks.BatchFile;
import org.hamcrest.Matchers;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.jenkinsci.plugins.postbuildscript.model.ScriptType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PostBuildScriptTest {

    @Mock
    private Script script;

    @Mock
    private ScriptFile scriptFile;

    @Mock
    private PostBuildStep postBuildStep;

    private PostBuildScript postBuildScript;
    private PostBuildScript resolvedPostBuildScript;

    @Test
    public void keepsPostBuildItems() {

        postBuildScript = new PostBuildScript(
            Collections.singleton(scriptFile),
            Collections.singleton(scriptFile),
            Collections.singleton(script),
            Collections.singleton(postBuildStep),
            true
        );

        postBuildScript.getGenericScriptFiles().contains(scriptFile);
        postBuildScript.getGroovyScriptFiles().contains(scriptFile);
        postBuildScript.getGroovyScripts().contains(script);
        postBuildScript.getBuildSteps().contains(postBuildStep);

        verify(scriptFile).setScriptType(ScriptType.GENERIC);
        verify(scriptFile).setScriptType(ScriptType.GROOVY);

    }

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
