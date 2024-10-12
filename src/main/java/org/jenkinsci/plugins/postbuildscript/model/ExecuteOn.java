package org.jenkinsci.plugins.postbuildscript.model;

/**
 * @author <a href="mailto:nicolas.deloof@gmail.com">Nicolas De Loof</a>
 */
public enum ExecuteOn {
    BOTH,
    MATRIX,
    AXES;

    public boolean matrix() {
        return this == MATRIX || this == BOTH;
    }

    public boolean axes() {
        return this == AXES || this == BOTH;
    }
}
