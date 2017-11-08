package org.jenkinsci.plugins.postbuildscript.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ConfigurationTest {

    @Mock
    private PostBuildStep postBuildStep;

    @Mock
    private ScriptFile scriptFile;

    @Mock
    private Script script;

    private Configuration configuration = new Configuration();

    @Test
    public void addsBuildStep() throws Exception {

        configuration.addBuildStep(postBuildStep);

        assertThat(configuration.getBuildSteps(), contains(postBuildStep));
        assertThat(configuration.buildStepIndexOf(postBuildStep), is(0));

    }


    @Test
    public void addsBuildSteps() throws Exception {

        configuration.addBuildSteps(Collections.singleton(postBuildStep));

        assertThat(configuration.getBuildSteps(), contains(postBuildStep));
        assertThat(configuration.buildStepIndexOf(postBuildStep), is(0));

    }

    @Test
    public void storesMarkBuildUnstable() throws Exception {

        configuration.setMarkBuildUnstable(true);

        assertThat(configuration.isMarkBuildUnstable(), is(true));

    }

    @Test
    public void addsGenericScriptFiles() throws Exception {

        configuration.addGenericScriptFiles(Collections.singleton(scriptFile));

        assertThat(configuration.getGenericScriptFiles(), contains(scriptFile));
        assertThat(configuration.genericScriptFileIndexOf(scriptFile), is(0));

    }


    @Test
    public void addsGroovyScripts() throws Exception {

        configuration.addGroovyScripts(Collections.singleton(script));

        assertThat(configuration.getGroovyScripts(), contains(script));
        assertThat(configuration.groovyScriptIndexOf(script), is(0));

    }

    @Test
    public void addsGroovyScriptFiles() throws Exception {

        configuration.addGroovyScriptFiles(Collections.singleton(scriptFile));

        assertThat(configuration.getGroovyScriptFiles(), contains(scriptFile));
        assertThat(configuration.groovyScriptFileIndexOf(scriptFile), is(0));

    }


}
