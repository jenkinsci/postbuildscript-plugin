package org.jenkinsci.plugins.postbuildscript.service;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.net.URL;
import org.junit.jupiter.api.Test;

public class LoadScriptContentCallableTest {

    @Test
    public void loadsScript() throws Exception {

        URL url = getClass().getResource("/test_script");

        LoadScriptContentCallable callable = new LoadScriptContentCallable();
        String script = callable.invoke(new File(url.toURI()), null);

        assertThat(script, startsWith("Hello world"));
    }
}
