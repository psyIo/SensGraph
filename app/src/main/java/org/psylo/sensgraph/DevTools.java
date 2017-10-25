package org.psylo.sensgraph;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


/**
 * Development tools used for debugging
 */

class DevTools {
    final static private String SEPARATOR = " ";
    final static private String ARRAY_VALUE_SEPARATOR = ";";
    final static private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss"); //nera locales

    static void log(String tag, Object... objects){
        Log.v(tag, makeStringBuilderFromObjectsStatic(objects).toString());
    }

    static void logE(String tag, Object... objects){
        Log.e(tag, makeStringBuilderFromObjectsStatic(objects).toString());
    }

    private static StringBuilder makeStringBuilderFromObjectsStatic(Object... objects) {
        final StringBuilder sb = new StringBuilder();
        for (Object obj : objects) {
            sb.append(convertObjectToString(obj));
            sb.append(SEPARATOR);
        }
        return sb;
    }

    private static void stringArrToStringBuilder(StringBuilder sb, Object obj) {
        String[] strArr = (String[]) obj;
        sb.append("String[");
        for (String str : strArr) {
            sb.append(str);
            sb.append(ARRAY_VALUE_SEPARATOR);
        }
        sb.delete(sb.length()-1, sb.length()); //removes last ARRAY_VALUE_SEPARATOR to look beautifully
        sb.append("]");
    }

    private static void intArrToStringBuilder(StringBuilder sb, Object obj) {
        int[] intArr = (int[]) obj;
        sb.append("int[");
        for (int i : intArr) {
            sb.append(String.valueOf(i));
            sb.append(ARRAY_VALUE_SEPARATOR);
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("]");
    }

    private static void longArrToStringBuilder(StringBuilder sb, Object obj) {
        long[] longArr = (long[]) obj;
        sb.append("long[");
        for (long l : longArr) {
            sb.append(String.valueOf(l));
            sb.append(ARRAY_VALUE_SEPARATOR);
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("]");
    }

    private static void floatArrToStringBuilder(StringBuilder sb, Object obj) {
        float[] floatArr = (float[]) obj;
        sb.append("float[");
        for (float f : floatArr) {
            sb.append(String.valueOf(f));
            sb.append(ARRAY_VALUE_SEPARATOR);
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("]");
    }

    private static void objectArrToStringBuilder(StringBuilder sb, Object obj) {
        Object[] objectArr = (Object[]) obj;
        sb.append("Object[");
        for (Object o : objectArr) {
            sb.append(convertObjectToString(o));
            sb.append(ARRAY_VALUE_SEPARATOR);
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("]");
    }

    private static void arrayListToStringBuilder(StringBuilder sb, Object obj) {
        ArrayList arrayList = (ArrayList) obj;
        sb.append("ArrayList[");
        for (Object o : arrayList) {
            sb.append(convertObjectToString(o));
            sb.append(ARRAY_VALUE_SEPARATOR);
        }
        sb.delete(sb.length()-1, sb.length());
        sb.append("]");
    }

    private static String convertObjectToString(Object obj) {

        if (obj == null) {
            return "null_ref";
        }

        StringBuilder sb = new StringBuilder();
        if (obj instanceof String[]) {
            stringArrToStringBuilder(sb, obj);
        } else if (obj instanceof int[]) {
            intArrToStringBuilder(sb, obj);
        } else if (obj instanceof long[]) {
            longArrToStringBuilder(sb, obj);
        } else if (obj instanceof float[]) {
            floatArrToStringBuilder(sb, obj);
        } else if (obj instanceof String) {
            sb.append((String) obj);
        } else if (obj instanceof Integer) {
            sb.append(String.valueOf(obj));
        } else if (obj instanceof Long) {
            sb.append(String.valueOf(obj));
        } else if (obj instanceof Double) {
            sb.append(String.valueOf(obj));
        } else if (obj instanceof Float) {
            sb.append(String.valueOf(obj));
        } else if (obj instanceof Date) {
            Date date = (Date) obj;
            sb.append(dateFormat.format(date));
        } else if (obj instanceof ArrayList) {
            arrayListToStringBuilder(sb, obj);
        } else if (obj instanceof Object[]) {
            objectArrToStringBuilder(sb, obj);
        } else {
            sb.append(obj);
        }

        return sb.toString();
    }

    private StringBuilder makeStringBuilderFromObjects(Object... objects) {
        final StringBuilder sb = new StringBuilder();
        for (Object obj : objects) {
            if (obj == null) {
                sb.append("null_ref");
            } else {
                sb.append(obj);
            }
            sb.append(SEPARATOR);
        }
        return sb;
    }

}
