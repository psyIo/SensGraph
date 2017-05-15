package org.psylo.sensgraph;

import android.util.Log;


/**
 * Developement tools
 * Created by psylo on 17.3.11.
 */

class DevTools {
    final String separator = " ";

    void logV(String tag, Object... objects){
        Log.v(tag, makeStringBuilderFromObjects(objects).toString());
    }

    void logE(String tag, Object... objects){
        Log.e(tag, makeStringBuilderFromObjects(objects).toString());
    }

    StringBuilder makeStringBuilderFromObjects(Object... objects) {
        final StringBuilder sb = new StringBuilder();
        for (Object obj : objects) {
            if (obj == null) {
                sb.append("null_ref");
            } else {
                sb.append(obj);
            }
            sb.append(separator);
        }
        return sb;
    }
}
