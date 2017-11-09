package org.jenkinsci.plugins.postbuildscript.service;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import org.jenkinsci.plugins.postbuildscript.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.File;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ContentTest {

    @Mock
    private FileCallable<String> callable;

    @Mock
    private Logger logger;

    @InjectMocks
    private Content content;

    @Captor
    private ArgumentCaptor<File> fileArgumentCaptor;

    @Test
    public void resolvesByCallingCallable() throws Exception {

        FilePath filePath = new FilePath((VirtualChannel) null, "remote");
        given(callable.invoke(isA(File.class), isA(LocalChannel.class))).willReturn("resolvedContent");

        String resolvedContent = content.resolve(filePath);

        verify(callable).invoke(fileArgumentCaptor.capture(), isA(LocalChannel.class));
        File file = fileArgumentCaptor.getValue();
        assertThat(file.getPath(), is("remote"));
        assertThat(resolvedContent, is("resolvedContent"));


    }
}
