package org.jenkinsci.plugins.postbuildscript.model;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ScriptFileTest {

    private static final String RESULT = "SUCCESS";
    private static final String FILE_PATH = "println 'Hello World'";

    @Test
    public void containsFilePath() throws Exception {

        ScriptFile scriptFile = new ScriptFile(
            Collections.singleton(RESULT), FILE_PATH);

        assertThat(scriptFile.getFilePath(), is(FILE_PATH));

    }

}
