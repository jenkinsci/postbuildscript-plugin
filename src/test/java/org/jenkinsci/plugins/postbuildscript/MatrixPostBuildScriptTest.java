package org.jenkinsci.plugins.postbuildscript;

import com.thoughtworks.xstream.XStream;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.matrix.MatrixAggregator;
import hudson.matrix.MatrixBuild;
import hudson.matrix.MatrixProject;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import org.jenkinsci.plugins.postbuildscript.MatrixPostBuildScript.DescriptorImpl;
import org.jenkinsci.plugins.postbuildscript.model.ExecuteOn;
import org.jenkinsci.plugins.postbuildscript.model.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MatrixPostBuildScriptTest {

    @Mock
    private Script script;

    @Mock
    private ScriptFile genericScriptFile;

    @Mock
    private ScriptFile groovyScriptFile;

    @Mock
    private PostBuildStep postBuildStep;

    @Mock
    private MatrixBuild matrixBuild;

    @Mock
    private Launcher launcher;

    @Mock
    private BuildListener listener;

    @Mock
    private EnvVars envVars;

    @Mock
    private PrintStream logger;

    private MatrixPostBuildScript resolvedMatrixPostBuildScript;

    private MatrixPostBuildScript matrixPostBuildScript;
    private DescriptorImpl descriptor;

    @Before
    public void setUp() {

        given(genericScriptFile.getScriptType()).willReturn(ScriptType.GENERIC);
        given(groovyScriptFile.getScriptType()).willReturn(ScriptType.GROOVY);

    }

    @Test
    public void keepsPostBuildItems() {

        givenMatrixPostBuildScript();

        assertThat(matrixPostBuildScript.getGenericScriptFiles(), contains(genericScriptFile));
        assertThat(matrixPostBuildScript.getGroovyScriptFiles(), contains(groovyScriptFile));
        assertThat(matrixPostBuildScript.getGroovyScripts(), contains(script));
        assertThat(matrixPostBuildScript.getBuildSteps(), contains(postBuildStep));
        assertThat(matrixPostBuildScript.isMarkBuildUnstable(), is(true));

        verify(genericScriptFile).setScriptType(ScriptType.GENERIC);
        verify(groovyScriptFile).setScriptType(ScriptType.GROOVY);

    }

    @Test
    public void createsAggregator() throws IOException, InterruptedException {

        given(matrixBuild.getEnvironment(listener)).willReturn(envVars);
        given(envVars.get("POSTBUILDSCRIPT_VERBOSE", "false")).willReturn("true");
        givenMatrixPostBuildScript();

        MatrixAggregator aggregator = matrixPostBuildScript.createAggregator(matrixBuild, launcher, listener);

        assertThat(aggregator, is(notNullValue()));

    }

    @Test
    public void containsHelpFile() {

        givenDescriptor();

        assertThat(descriptor.getHelpFile(), is("/plugin/postbuildscript/help/postbuildscript.html"));

    }

    @Test
    public void appliesToMatrixJob() {

        givenDescriptor();

        Class<? extends AbstractProject> jobType = MatrixProject.class;
        assertThat(descriptor.isApplicable(jobType), is(true));

    }

    private void givenMatrixPostBuildScript() {
        matrixPostBuildScript = new MatrixPostBuildScript(
            Collections.singleton(genericScriptFile),
            Collections.singleton(groovyScriptFile),
            Collections.singleton(script),
            Collections.singleton(postBuildStep),
            true
        );
    }

    private void givenScriptFromConfig(String configResourceName) {
        XStream xstream = new XStream();
        matrixPostBuildScript = (MatrixPostBuildScript) xstream.fromXML(getClass().getResource(configResourceName));
    }


    private void givenDescriptor() {
        descriptor = new DescriptorImpl();
    }

}
