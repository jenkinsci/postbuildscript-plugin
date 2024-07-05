package org.jenkinsci.plugins.postbuildscript.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class ScriptTest {

    private static final String RESULT = "SUCCESS";
    private static final String CONTENT = "println 'Hello World'";

    @Test
    public void containsScript() {

        Script script = new Script(
            Collections.singleton(RESULT), CONTENT);

        assertThat(script.getContent(), is(CONTENT));

    }

    @Test
    public void containsNullOnEmptyScript() {

        Script script = new Script(
            Collections.singleton(RESULT), "");

        assertThat(script.getContent(), is(nullValue()));

    }
}
