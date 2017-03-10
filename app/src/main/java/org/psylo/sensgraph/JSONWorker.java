package org.psylo.sensgraph;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Class designed to work with JSON file (loaded as mainJObj type JSONObject attribute) (download, parse etc.)
 * Created by psylo on 17.3.3.
 */

public class JSONWorker {

    static final String TAG = "SensGraphJSONWorker"; //dev
    public JSONObject mainJObj;

    public static String GetHttpResponse(String urlString) {
        URL url = null;
        String response = "";
        try {
            url = new URL(urlString);
        } catch (Exception e) {
            Log.e(TAG, "Exception making URL", e);
        }
        try {
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(5000);
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = readStream(in);
            Log.v(TAG, "Response " + response); //dev
            urlConnection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        } finally {
        }
        return response;
    }

    public static String readStream(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);//ev
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException", e); //dev
            }
        }
        return sb.toString();
    }

//    public String[] GetElements(){
//        String[] result;
//
//        return result;
//    }

    public void mainJsonObjFromString(String jString) {
        Log.v(TAG, "mainJsonObjFromString jString: " + jString); //dev
        try {
            if (jString.length() > 0) {
                mainJObj = new JSONObject(jString);
            } else {
                Log.v(TAG, "mainJsonObjFromString jString length() == 0"); //dev
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException ", e); //dev
        }
    }

    public Boolean makeJsonFromHttpResponse(String urlString) {
//        View tv = new View();
//        String test = "Vienas";//dev
        mainJsonObjFromString(GetHttpResponse(urlString));
        if (mainJObj == null) {
            return false;
        } else {
            ArrayList<String> li = new ArrayList<String>();
//            Log.v(TAG, "li.toString()1 " + li.toString());
//            addJsonNamesToList(li, mainJObj, 0);
            Log.v(TAG, "jsonNamesArrayList().toString() " + jsonNamesArrayList().toString()); //dev
            Log.v(TAG, "mainJObj: " + mainJObj.toString()); //dev
//            Log.v(TAG, "li.toString()2 " + li.size());
//            Log.v(TAG, "li.toString()3 " + li.toString());
//            ArrayList<String> ar = jsonNames();
//            Log.v(TAG, "ar " + ar.toString()); //dev
//            JSONObject jt = mainJObj;
//            Test(jt);
//            Log.v(TAG, "jt.toString()3x " + String.valueOf(jt.hashCode()));//dev
//            String[] sa = new String[10];
//            sa[0] = "1";
//            sa[1] = "2";
//            Test(sa);
//            Log.v(TAG, "sa 2: " + sa[2]);
            return true;
        }
    }

    public ArrayList<String> jsonNamesArrayList() {
        ArrayList<String> namesList = new ArrayList<>(); //??
        if (mainJObj != null) {
            addJsonNamesToList(namesList, mainJObj, 0);
        }
        return namesList;
    }

//    public void Test(String[] str) {
//        str[2] = "3";
////        Log.v(TAG, "jt.toString()1 " + String.valueOf(parj.hashCode()));
////        parj = new JSONObject();
////        Log.v(TAG, "jt.toString()2 " + String.valueOf(parj.hashCode()));
//    }
//    public void addNamesFromJSONArrayToList(ArrayList<String> nameList, JSONArray jArray, int)


    public void addJsonNamesToList(ArrayList<String> nameList, JSONObject jObjToProcess, int level) {
        Iterator<String> namesIter = jObjToProcess.keys();
        String currName;// = "";
        String tabbedName;// = "";
        int locLevel = level + 1;
        Object obj = new Object();
        JSONObject jsonObj;// = new JSONObject();
        JSONArray jsonArr;// = new JSONArray();
//        Log.v(TAG,namesIter);
        while (namesIter.hasNext()) {
//            try {
            currName = namesIter.next();
            obj = mainJObj.opt(currName);
//            Log.v(TAG, "currName " + currName);
//            } catch (JSONException e) {
//                Log.e(TAG, "JSONException", e);
//            }

            //tabulating and adding element
            tabbedName = currName;
            if (level > 0) {
//                tabbedName = " " + tabbedName;
                for (int i = 0; i < level; i++) {
                    tabbedName = " " + tabbedName;
                }
            }
            nameList.add(tabbedName);
//            Log.v(TAG, "nameList.add(currName) " + tabbedName);

            if (obj instanceof JSONObject) {
                try {
                    jsonObj = mainJObj.getJSONObject(currName);
                    addJsonNamesToList(nameList, jsonObj, locLevel);
                } catch (Exception e) {
                    Log.e(TAG, "Exception", e);
                }
//                Log.v(TAG, "obj instanceof JSONObject");
            } else if (obj instanceof JSONArray) {
                try {
                    jsonArr = mainJObj.getJSONArray(currName);
                    for (int i = 0; i < jsonArr.length(); i++) {
                        jsonObj = jsonArr.getJSONObject(i);
                        addJsonNamesToList(nameList, jsonObj, locLevel);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Exception", e);
                }
//                Log.v(TAG, "obj instanceof JSONArray");
            }
        }

//        public String

//        Log.v(TAG, "mainJObj.keys().toString(): " + mainJObj.keys().toString()); //dev
//        try {
//            for (int i = 0; i < jsonNames.length(); i++) {
//                Object obj = mainJObj.get(jsonNames.get(i).toString());
//                if (obj instanceof JSONObject) {
//                    Log.v(TAG, "obj instanceof JSONObject");
//                } else if (obj instanceof JSONArray) {
//                    Log.v(TAG, "obj instanceof JSONArray");
//                }
//                names.add(jsonNames.get(i).toString());
////                Log.v(TAG, "obj.toString() " + obj.toString());
////                Log.v(TAG, "jsonNames.get(i).toString() " + jsonNames.get(i).toString());
//    //                    Log.v(TAG, '');
////                Log.v(TAG, "jObj: " + String.valueOf(i) + " value: " + jsonNames.getString(i));// +
//    //                            "obj length: " + jobj.toString()); //dev
//            }
//        } catch (JSONException e) {
//            Log.e(TAG, "Error parsing data " + e.toString());
//        }
//        Log.v(TAG, "jsonNames.toString()" + jsonNames.toString()); //dev
//        return names;
    }

//    private class

}
