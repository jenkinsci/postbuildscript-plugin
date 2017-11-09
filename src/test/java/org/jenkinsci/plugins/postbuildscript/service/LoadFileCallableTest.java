package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(MockitoJUnitRunner.class)
public class LoadFileCallableTest {

    @Mock
    private VirtualChannel virtualChannel;

    @Test
    public void fileExists() throws Exception {

        File tempFile = File.createTempFile(LoadFileCallableTest.class.getName(),
            ".txt");
        tempFile.deleteOnExit();

        LoadFileCallable callable = new LoadFileCallable(tempFile.getPath(), null);
        FilePath filePath = callable.invoke(null, virtualChannel);

        assertThat(filePath.getRemote(), is(tempFile.getPath()));

    }

    @Test
    public void fileExistsInWorkspace() throws Exception {

        File tempFile = File.createTempFile(LoadFileCallableTest.class.getName(),
            ".txt");
        tempFile.deleteOnExit();

        FilePath workspace = new FilePath((VirtualChannel) null, tempFile.getParent());

        LoadFileCallable callable = new LoadFileCallable(tempFile.getName(), workspace);
        FilePath filePath = callable.invoke(null, virtualChannel);

        assertThat(filePath.getRemote(), is(tempFile.getPath()));

    }

    @Test
    public void fileDoesNotExist() throws Exception {

        FilePath workspace = new FilePath((VirtualChannel) null, "doesNotExist");

        LoadFileCallable callable = new LoadFileCallable("doesNotExist", workspace);
        FilePath filePath = callable.invoke(null, virtualChannel);

        assertThat(filePath, is(nullValue()));

    }

}
