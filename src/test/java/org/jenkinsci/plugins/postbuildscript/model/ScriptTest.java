package org.jenkinsci.plugins.postbuildscript.model;

import hudson.tasks.BuildStep;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class ScriptTest {

    private static final String RESULT = "SUCCESS";
    private static final String CONTENT = "println 'Hello World'";

    @Mock
    private BuildStep buildStep;

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
