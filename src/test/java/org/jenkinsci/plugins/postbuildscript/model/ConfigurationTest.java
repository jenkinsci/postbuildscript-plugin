package org.jenkinsci.plugins.postbuildscript.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
public class ConfigurationTest {

    @Mock
    private PostBuildStep postBuildStep;

    @Mock
    private ScriptFile scriptFile;

    @Mock
    private Script script;

    private final Configuration configuration = new Configuration();

    @Test
    public void addsBuildStep() {

        configuration.addBuildStep(postBuildStep);

        assertThat(configuration.getBuildSteps(), contains(postBuildStep));
        assertThat(configuration.buildStepIndexOf(postBuildStep), is(0));

    }


    @Test
    public void addsBuildSteps() {

        configuration.addBuildSteps(Collections.singleton(postBuildStep));

        assertThat(configuration.getBuildSteps(), contains(postBuildStep));
        assertThat(configuration.buildStepIndexOf(postBuildStep), is(0));

    }

    @Test
    public void storesMarkBuildUnstable() {

        configuration.setMarkBuildUnstable(true);

        assertThat(configuration.isMarkBuildUnstable(), is(true));

    }

    @Test
    public void addsGenericScriptFiles() {

        given(scriptFile.getScriptType()).willReturn(ScriptType.GENERIC);

        configuration.addScriptFiles(Collections.singleton(scriptFile));

        assertThat(configuration.getScriptFiles(ScriptType.GENERIC), contains(scriptFile));
        assertThat(configuration.scriptFileIndexOf(scriptFile), is(0));

    }


    @Test
    public void addsGroovyScripts() {

        configuration.addGroovyScripts(Collections.singleton(script));

        assertThat(configuration.getGroovyScripts(), contains(script));
        assertThat(configuration.groovyScriptIndexOf(script), is(0));

    }

    @Test
    public void addsGroovyScriptFiles() {

        given(scriptFile.getScriptType()).willReturn(ScriptType.GROOVY);

        configuration.addScriptFiles(Collections.singleton(scriptFile));

        assertThat(configuration.getScriptFiles(ScriptType.GROOVY), contains(scriptFile));
        assertThat(configuration.scriptFileIndexOf(scriptFile), is(0));

    }


}
