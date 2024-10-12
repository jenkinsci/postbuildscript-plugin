package org.jenkinsci.plugins.postbuildscript;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Collections;
import org.jenkinsci.plugins.postbuildscript.model.PostBuildStep;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.jenkinsci.plugins.postbuildscript.model.ScriptFile;
import org.jenkinsci.plugins.postbuildscript.model.ScriptType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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

    @Test
    public void keepsPostBuildItems() {

        given(genericScriptFile.getScriptType()).willReturn(ScriptType.GENERIC);
        given(groovyScriptFile.getScriptType()).willReturn(ScriptType.GROOVY);

        postBuildScript = new PostBuildScript(
                Collections.singleton(genericScriptFile),
                Collections.singleton(groovyScriptFile),
                Collections.singleton(script),
                Collections.singleton(postBuildStep),
                true);

        assertThat(postBuildScript.getGenericScriptFiles(), contains(genericScriptFile));
        assertThat(postBuildScript.getGroovyScriptFiles(), contains(groovyScriptFile));
        assertThat(postBuildScript.getGroovyScripts(), contains(script));
        assertThat(postBuildScript.getBuildSteps(), contains(postBuildStep));
        assertThat(postBuildScript.isMarkBuildUnstable(), is(true));

        verify(genericScriptFile).setScriptType(ScriptType.GENERIC);
        verify(groovyScriptFile).setScriptType(ScriptType.GROOVY);
    }
}
