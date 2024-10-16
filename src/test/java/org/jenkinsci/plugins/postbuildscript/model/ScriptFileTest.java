package org.jenkinsci.plugins.postbuildscript.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Collections;
import org.junit.jupiter.api.Test;

public class ScriptFileTest {

    private static final String RESULT = "SUCCESS";
    private static final String FILE_PATH = "println 'Hello World'";

    @Test
    public void containsFilePath() {

        ScriptFile scriptFile = new ScriptFile(Collections.singleton(RESULT), FILE_PATH);

        assertThat(scriptFile.getFilePath(), is(FILE_PATH));
    }
}
