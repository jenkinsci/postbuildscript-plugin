package org.jenkinsci.plugins.postbuildscript;

import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.jenkinsci.plugins.postbuildscript.model.ScriptType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertThat;
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

    private MatrixPostBuildScript matrixPostBuildScript;

    @Test
    public void keepsPostBuildItems() {

        given(genericScriptFile.getScriptType()).willReturn(ScriptType.GENERIC);
        given(groovyScriptFile.getScriptType()).willReturn(ScriptType.GROOVY);

        matrixPostBuildScript = new MatrixPostBuildScript(
            Collections.singleton(genericScriptFile),
            Collections.singleton(groovyScriptFile),
            Collections.singleton(script),
            Collections.singleton(postBuildStep),
            true
        );

        assertThat(matrixPostBuildScript.getGenericScriptFiles(), contains(genericScriptFile));
        assertThat(matrixPostBuildScript.getGroovyScriptFiles(), contains(groovyScriptFile));
        assertThat(matrixPostBuildScript.getGroovyScripts(), contains(script));
        assertThat(matrixPostBuildScript.getBuildSteps(), contains(postBuildStep));
        assertThat(matrixPostBuildScript.isMarkBuildUnstable(), is(true));

        verify(genericScriptFile).setScriptType(ScriptType.GENERIC);
        verify(groovyScriptFile).setScriptType(ScriptType.GROOVY);

    }

}
