package org.psylo.sensgraph;

import java.util.Formatter;
import java.util.Locale;

import android.util.Log;


/**
 * Created by psylo on 17.3.11.
 */

public class DevTools {
//    private StringBuilder sb;
    final String TAG = "DevTools";
    final String separator = " ";

    public void logV(Object... objects){
        Log.v(TAG, addObjsToStringBuilder(objects).toString());
    }

    public void logE(Object... objects){
        Log.e(TAG, addObjsToStringBuilder(objects).toString());
    }

    public StringBuilder addObjsToStringBuilder(Object... objects) {
        final StringBuilder sb = new StringBuilder();
        for (Object obj : objects) {
            if (obj == null) {
                sb.append("null");
            } else {
                sb.append(obj);
//                if (obj instanceof String) {
//                    sb.append(":");
//                }
            }
            sb.append(separator);
        }
        return sb;
    }
}
