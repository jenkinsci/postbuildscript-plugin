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

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MatrixPostBuildScriptTest {

    @Mock
    private Script script;

    @Mock
    private ScriptFile scriptFile;

    @Mock
    private PostBuildStep postBuildStep;

    private PostBuildScript postBuildScript;

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

}
