package org.psylo.sensgraph;

import java.util.Arrays;

/**
 * Created by psylo on 17.7.22.
 * DbEntry
 */

public class DbEntry {
    public Object[] fields;

    DbEntry() {
        this.fields = new Object[10];
    }

    public Object getField1(int idx) {
        DevTools.logE("DbEntry", "Arrays.toString(fields)", Arrays.toString(fields), "fields", fields);
        if (SensGraphConfigure.equalsNullSafe(fields, null)) {
            return false;
        } else {
            return fields[idx];
        }

    }

    @Override
    public String toString() {
        return "DbEntry{" +
                "fields=" + Arrays.toString(fields) +
                "} hash " +
                this.hashCode();
    }
}

