package org.jenkinsci.plugins.postbuildscript.service;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.jenkinsci.plugins.postbuildscript.model.Script;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptExecutorFactoryTest {

    @Mock
    private Logger logger;

    @Mock
    private AbstractBuild<?, ?> build;

    @Mock
    private Script script;

    @InjectMocks
    private GroovyScriptExecutorFactory executorFactory;

    @Test
    public void createsExecutor() throws Exception {

        GroovyScriptExecutor executor = executorFactory.create(script, Collections.emptyList());

        assertThat(executor, is(notNullValue()));

    }
}
