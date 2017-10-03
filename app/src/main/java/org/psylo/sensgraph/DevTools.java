package org.psylo.sensgraph;

import android.util.Log;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
//import java.util.Base64;
import android.util.Base64;


/**
 * Developement tools
 * Created by psylo on 17.3.11.
 */

class DevTools {
    final static private String SEPARATOR = " ";
    final static private String ARRAY_VALUE_SEPARATOR = ";";
    final static private String TAG = "DevTools";

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

    void logV(String tag, Object... objects){
        Log.v(tag, makeStringBuilderFromObjects(objects).toString());
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
        } else if (obj instanceof Float) {
            sb.append(String.valueOf(obj));
        } else if (obj instanceof ArrayList) {
            arrayListToStringBuilder(sb, obj);
        } else if (obj instanceof Object[]) {
            objectArrToStringBuilder(sb, obj);
        } else {
            sb.append(obj);
        }

        return sb.toString();
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
            sb.append(SEPARATOR);
        }
        return sb;
    }

    /** Read the object from Base64 string. */
    public static Object fromBase64String(String s) {
        Object o = new Object();
        try {
            byte[] data = Base64.decode(s, 0);
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
            o = ois.readObject();
            ois.close();
        } catch (Exception e) {
            DevTools.logE(TAG, e.toString());
        }
        return o;
    }

    /** Write the object to a Base64 string. */
    public static String toBase64String(Object o) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(o);
            oos.close();
            return Base64.encodeToString(baos.toByteArray(), 0);
        } catch (IOException e) {
            DevTools.logE(TAG, e.toString());
        }
        return "";
    }

//    /**
//     * Return Base64 encoded string as per Android SDK version
//     * */
//    @SuppressWarnings("deprecation")
//    protected String encodeToBase64VersionSafe(Serializable o) throws IOException{
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        ObjectOutputStream oos = new ObjectOutputStream(baos);
//        oos.writeObject(o);
//        oos.close();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) { // 26 and newer
////            ByteArrayOutputStream baos = new ByteArrayOutputStream();
////            ObjectOutputStream oos = new ObjectOutputStream(baos);
////            oos.writeObject(o);
////            oos.close();
////            Base64.getEncoder().encodeToString(baos.toByteArray());
//        } else { // 25 and older
//            android.util.Base64.encode(baos.toByteArray(),0);
//        }
//    }


}
