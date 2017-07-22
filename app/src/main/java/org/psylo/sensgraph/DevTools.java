package org.psylo.sensgraph;

import android.util.Log;


/**
 * Developement tools
 * Created by psylo on 17.3.11.
 */

class DevTools {
    final static private String separator = " ";

    static void log(String tag, Object... objects){
        Log.v(tag, makeStringBuilderFromObjectsStatic(objects).toString());
    }

    static void logE(String tag, Object... objects){
        Log.e(tag, makeStringBuilderFromObjectsStatic(objects).toString());
    }

    private static StringBuilder makeStringBuilderFromObjectsStatic(Object... objects) {
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

    void logV(String tag, Object... objects){
        Log.v(tag, makeStringBuilderFromObjects(objects).toString());
    }

//    void logE(String tag, Object... objects){
//        Log.e(tag, makeStringBuilderFromObjects(objects).toString());
//    }

    private StringBuilder makeStringBuilderFromObjects(Object... objects) {
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
