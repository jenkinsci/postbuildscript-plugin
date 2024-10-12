package org.jenkinsci.plugins.postbuildscript.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import hudson.FilePath;
import hudson.remoting.VirtualChannel;
import java.io.File;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoadFileCallableTest {

    @Mock
    private VirtualChannel virtualChannel;

    @Test
    public void fileExists() throws Exception {

        File tempFile = File.createTempFile(LoadFileCallableTest.class.getName(), ".txt");
        tempFile.deleteOnExit();

        LoadFileCallable callable = new LoadFileCallable(tempFile.getPath(), null);
        FilePath filePath = callable.invoke(null, virtualChannel);

        assertThat(filePath.getRemote(), is(tempFile.getPath()));
    }

    @Test
    public void fileExistsInWorkspace() throws Exception {

        File tempFile = File.createTempFile(LoadFileCallableTest.class.getName(), ".txt");
        tempFile.deleteOnExit();

        FilePath workspace = new FilePath(tempFile.getParentFile());

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
