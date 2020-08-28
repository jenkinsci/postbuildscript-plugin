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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PostBuildScriptTest {

    @Mock
    private Script script;

    @Mock
    private ScriptFile genericScriptFile;

    @Mock
    private ScriptFile groovyScriptFile;

    @Mock
    private PostBuildStep postBuildStep;

    private PostBuildScript postBuildScript;
    private PostBuildScript resolvedPostBuildScript;

    @Test
    public void keepsPostBuildItems() {

        given(genericScriptFile.getScriptType()).willReturn(ScriptType.GENERIC);
        given(groovyScriptFile.getScriptType()).willReturn(ScriptType.GROOVY);

        postBuildScript = new PostBuildScript(
            Collections.singleton(genericScriptFile),
            Collections.singleton(groovyScriptFile),
            Collections.singleton(script),
            Collections.singleton(postBuildStep),
            true
        );

        assertThat(postBuildScript.getGenericScriptFiles(), contains(genericScriptFile));
        assertThat(postBuildScript.getGroovyScriptFiles(), contains(groovyScriptFile));
        assertThat(postBuildScript.getGroovyScripts(), contains(script));
        assertThat(postBuildScript.getBuildSteps(), contains(postBuildStep));
        assertThat(postBuildScript.isMarkBuildUnstable(), is(true));

        verify(genericScriptFile).setScriptType(ScriptType.GENERIC);
        verify(groovyScriptFile).setScriptType(ScriptType.GROOVY);

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

    private void givenScriptFromConfig(String configResourceName) {
        XStream xstream = new XStream();
        postBuildScript = (PostBuildScript) xstream.fromXML(getClass().getResource(configResourceName));
    }

    private void whenReadResolves() {
        resolvedPostBuildScript = (PostBuildScript) postBuildScript.readResolve();
    }

}
