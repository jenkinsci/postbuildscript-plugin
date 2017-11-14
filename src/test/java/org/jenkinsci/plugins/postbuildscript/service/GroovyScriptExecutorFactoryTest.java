package org.jenkinsci.plugins.postbuildscript.service;

import hudson.model.AbstractBuild;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class GroovyScriptExecutorFactoryTest {

    @Mock
    private Logger logger;

    @Mock
    private AbstractBuild<?, ?> build;

    @InjectMocks
    private GroovyScriptExecutorFactory executorFactory;

    @Test
    public void createsExecutor() throws Exception {

        GroovyScriptExecutor executor = executorFactory.create("scriptContent");

        assertThat(executor, is(notNullValue()));

    }
}
