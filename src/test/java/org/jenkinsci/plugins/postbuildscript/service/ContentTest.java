package org.jenkinsci.plugins.postbuildscript.service;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import hudson.FilePath;
import hudson.FilePath.FileCallable;
import hudson.remoting.LocalChannel;
import hudson.remoting.VirtualChannel;
import java.io.File;
import org.jenkinsci.plugins.postbuildscript.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
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
