package org.jenkinsci.plugins.postbuildscript.service;

import hudson.EnvVars;
import org.junit.Test;

import java.io.File;
import java.net.URL;

import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertThat;

public class LoadScriptContentCallableTest {

    @Test
    public void replacesMacroInScript() throws Exception {

        URL url = getClass().getResource("/test_script");
        EnvVars.masterEnvVars.put("name", "world");

        LoadScriptContentCallable callable = new LoadScriptContentCallable();
        String script = callable.invoke(new File(url.toURI()), null);

        assertThat(script, startsWith("Hello world"));

    }
}
