package org.jenkinsci.plugins.postbuildscript;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PostBuildScriptExceptionTest {

    @Mock
    private Exception cause;

    private static final String MESSAGE = "message";

    @Test
    public void containsMessage() {

        PostBuildScriptException exception = new PostBuildScriptException(MESSAGE);

        assertThat(exception.getMessage(), is(MESSAGE));

    }

    @Test
    public void containsMessageAndCause() {

        PostBuildScriptException exception = new PostBuildScriptException(MESSAGE, cause);

        assertThat(exception.getMessage(), is(MESSAGE));
        assertThat(exception.getCause(), is(cause));

    }

    @Test
    public void containsCause() {

        PostBuildScriptException exception = new PostBuildScriptException(cause);

        assertThat(exception.getCause(), is(cause));

    }

}
