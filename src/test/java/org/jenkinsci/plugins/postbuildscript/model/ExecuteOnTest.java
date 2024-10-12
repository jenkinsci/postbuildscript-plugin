package org.jenkinsci.plugins.postbuildscript.model;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.Test;

public class ExecuteOnTest {

    @Test
    public void bothAxesIsTrue() {
        assertThat(ExecuteOn.BOTH.axes(), is(true));
    }

    @Test
    public void bothMatrixIsTrue() {
        assertThat(ExecuteOn.BOTH.matrix(), is(true));
    }

    @Test
    public void matrixAxesIsFalse() {
        assertThat(ExecuteOn.MATRIX.axes(), is(false));
    }

    @Test
    public void matrixMatrixIsTrue() {
        assertThat(ExecuteOn.MATRIX.matrix(), is(true));
    }

    @Test
    public void axesMatrixIsFalse() {
        assertThat(ExecuteOn.AXES.matrix(), is(false));
    }

    @Test
    public void axesAxesIsTrue() {
        assertThat(ExecuteOn.AXES.axes(), is(true));
    }
}
