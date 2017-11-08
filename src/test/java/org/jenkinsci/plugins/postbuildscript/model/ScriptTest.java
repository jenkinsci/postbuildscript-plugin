package org.jenkinsci.plugins.postbuildscript.model;

import org.junit.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

public class ScriptTest {

    private static final String RESULT = "SUCCESS";
    private static final String CONTENT = "println 'Hello World'";

    @Test
    public void containsScript() throws Exception {

        Script script = new Script(
            Collections.singleton(RESULT), CONTENT);

        assertThat(script.getContent(), is(CONTENT));

    }

    @Test
    public void containsNullOnEmptyScript() throws Exception {

        Script script = new Script(
            Collections.singleton(RESULT), "");

        assertThat(script.getContent(), is(nullValue()));

    }
}
