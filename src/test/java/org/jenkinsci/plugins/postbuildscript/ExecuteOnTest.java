package org.jenkinsci.plugins.postbuildscript;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ExecuteOnTest {

    @Test
    public void bothAxesIsTrue() throws Exception {

        assertThat(ExecuteOn.BOTH.axes(), is(true));

    }

    @Test
    public void bothMatrixIsTrue() throws Exception {

        assertThat(ExecuteOn.BOTH.matrix(), is(true));

    }

    @Test
    public void matrixAxesIsFalse() throws Exception {

        assertThat(ExecuteOn.MATRIX.axes(), is(false));

    }

    @Test
    public void matrixMatrixIsTrue() throws Exception {

        assertThat(ExecuteOn.MATRIX.matrix(), is(true));

    }

    @Test
    public void axesMatrixIsFalse() throws Exception {

        assertThat(ExecuteOn.AXES.matrix(), is(false));

    }

    @Test
    public void axesAxesIsTrue() throws Exception {

        assertThat(ExecuteOn.AXES.axes(), is(true));

    }

}
