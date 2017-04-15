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
import java.util.jar.JarInputStream;

/**
 * Class designed to work with JSON file (loaded as mainJObj type JSONObject attribute) (download, parse etc.)
 * Created by psylo on 17.3.3.
 */

public class JSONWorker {

    static final String TAG = "SensGraphJSONWorker"; //dev
    static DevTools dt = new DevTools();
    JSONObject mainJObj;
    ArrayList<String> namesList;
    ArrayList<String> valuesList;

    static String GetHttpResponse(String urlString) {
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
//            Log.v(TAG, "Response " + response); //dev
            urlConnection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "Exception", e);
        }// finally {
        //}
        return response;
    }

    public static String readStream(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            Log.e(TAG, "IOException", e);//dev
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "IOException", e); //dev
            }
        }
        return sb.toString();
    }

    public void mainJsonObjFromString(String jString) {
//        Log.v(TAG, "mainJsonObjFromString jString: " + jString); //dev
        try {
            if (jString.length() > 0) {
                mainJObj = new JSONObject(jString);
            } else {
//                Log.v(TAG, "mainJsonObjFromString jString length() == 0"); //dev
            }

        } catch (JSONException e) {
            Log.e(TAG, "JSONException ", e); //dev
        }
    }

//    public Boolean makeJsonFromHttpResponse(String urlString) {
//        mainJsonObjFromString(GetHttpResponse(urlString));
//        if (mainJObj == null) {
//            return false;
//        } else {
//            return true;
//        }
//    }

//    public void makeNamesValuesLists() {
//        namesList = makeNamesValuesLists();
//    }

    protected void makeNamesValuesLists() {
        namesList = new ArrayList<>();
        valuesList = new ArrayList<>();
        if (mainJObjLoaded()) {
            addNamesFromJsonObjectToList(namesList, valuesList, mainJObj, "/");
        }
//        return namesList;
    }

    private void addNamesFromJSONArrayToList(ArrayList<String> nameList, ArrayList<String> valueList,
                                            JSONArray jArray, String parentNames) {
        String localNames;
        for (int i = 0; i < jArray.length(); i++) {
            Object obj = jArray.opt(i);
            if (obj != null) {
                localNames = parentNames + "[" + String.valueOf(i) +"]";
                if (obj instanceof JSONObject) {
                    addNamesFromJsonObjectToList(nameList, valueList, (JSONObject) obj, localNames + "/"); //sets next element JSONObject
                } else if (obj instanceof JSONArray) {
//                    addNamesFromJSONArrayToList(nameList, valueList, (JSONArray) obj, localNames + "/["); //sets next element JSONArray
                    addNamesFromJSONArrayToList(nameList, valueList, (JSONArray) obj, localNames + "/"); //sets next element JSONArray
                } else { //just a value String, Boolean etc.
                    nameList.add(localNames);
                    valueList.add(clearNotValueSymbols(obj.toString()));
                }
            }
//            jsonObj = jArray.optJSONObject(i);
//            if (jsonObj != null) {
//                addNamesFromJsonObjectToList(nameList, valueList, jsonObj, parentNames + String.valueOf(i) +"]/");
//            }
        }
    }

    Boolean mainJObjLoaded() {
        return mainJObj != null;
    }

    private void addNamesFromJsonObjectToList(ArrayList<String> namesList, ArrayList<String> valuesList,
                                             JSONObject jObj, String parentNames) {
        Iterator<String> namesIter = jObj.keys();
        String currName;
        String localNames;
        Object obj;
        JSONObject jsonObj;
        JSONArray jsonArr;

        while (namesIter.hasNext()) {
            currName = namesIter.next();
//            dt.logV("currName", currName); //dev
            localNames = parentNames + currName;
            obj = jObj.opt(currName);
            if (obj != null) {
                if (obj instanceof JSONObject) {
                    try {
                        jsonObj = jObj.getJSONObject(currName); //gets value
                        localNames += "/";
                        addNamesFromJsonObjectToList(namesList, valuesList, jsonObj, localNames);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception", e);
                    }
                } else if (obj instanceof JSONArray) {
                    try {
//                        ??? padaryti kad listo eiles nr zymimas kitu elelmentu list/[0] ir tt
                        jsonArr = jObj.getJSONArray(currName);
//                        localNames +=  "["; //]/"; start of the list name
                        localNames +=  "/"; //]/"; start of the list name
                        addNamesFromJSONArrayToList(namesList, valuesList, jsonArr, localNames);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception", e);
                    }
                } else { //just a value String, Boolean etc.
                    localNames += "/";
                    namesList.add(localNames);
                    valuesList.add(clearNotValueSymbols(obj.toString()));
//                    dt.logV("addNamesFromJsonObjectToList obj", obj);
                }
            }
        }
    }

    String getValueFromPath(String path) {
        if (mainJObjLoaded() && (path != null) && (!path.equals(""))) {
//            String value = "";
            JSONObject jobj;
            JSONArray jarr = new JSONArray();
            String sep = "/";
//            String liInd = "[]"; //pathList indicator
            String name;
            Object valueObj;
            int listStartIdx;
            int listIdx = -1;
            dt.logV(TAG, "getValueFromPath path", path);
            jobj = mainJObj;
            if (path.equals("//")) { //path consists only from name equal to ""
                valueObj = jobj.opt("");
            } else {
                path = path.substring(1); //removes first separator symbol
                String[] pathList = path.split(sep);
                for (int i = 0; i < pathList.length /**- 1*/; i++) {  //getting objects till the last - 1 or none if length == 1
                    name = pathList[i];
                    dt.logV("name", name);
                    listIdx = -1;
                    //list name processing
                    //max here is used to eliminate exception of "".length()-1 giving negative index, "".substring(0) is an empty string
                    if (name.substring(Math.max(0, name.length()-1)).equals("]")) {
                        listStartIdx = name.lastIndexOf("[");
//                        if (listStartIdx > 0) {
                        listIdx = Integer.parseInt(name.substring(listStartIdx + 1, name.length() - 1));
                        if (listIdx < 0) {
                            dt.logE(TAG, "getValueFromPath() Error parsing path listIdx < 0");
                            return "";
                        }
                        name = name.substring(0, listStartIdx);
                        dt.logV("name", name, "listIdx", listIdx);
//                        }
                    }
                    if (listIdx > -1) { //list element
                        valueObj = jarr.opt(listIdx);
                    } else {
                        valueObj = jobj.opt(name);
                    }
                    dt.logV("valueObj", valueObj);
                    if (valueObj instanceof JSONObject) {
                        jobj = (JSONObject) valueObj;
                        valueObj = jobj.opt(pathList[pathList.length - 1]);
//                        valueObj = jobj.opt(pathList[pathList.length - 1]);
                    } else if (valueObj instanceof JSONArray) {
                        jarr = (JSONArray) valueObj;
//                        valueObj = jarr.opt(getArrayIDFromName(jarr, pathList[i+1])); //should be always JSONObject
                        try {
//                            valueObj = jarr.get(listIdx); //should be always JSONObject
                            dt.logV("valueObj", valueObj, "valueObj instanceof JSONArray", "listIdx",
                                    listIdx, "jarr", jarr);
                        } catch (Exception e) {
                            dt.logE(e);
                            return "";
                        }
                        if (!(valueObj instanceof JSONObject)) {
//                            Log.e(TAG, "getValueFromPath return value is not JSONObject");
                            jarr = (JSONArray) valueObj;
                        } else {
                            jobj = (JSONObject) valueObj;
                        }
                    } else { //Value found. String, int etc.
                        if (valueObj != null) {
                            return valueObj.toString();
                        }
                    }
                }
                valueObj = jobj.opt(pathList[pathList.length - 1]);
            }

            //last path element
//            if (pathList.length > 0) {
//                valueObj = jobj.opt(pathList[pathList.length - 1]);
//            } else { //if path ( consists only from empty string "/"
//                valueObj = jobj.opt("");
//            }
            if (valueObj != null) {
                return clearNotValueSymbols(valueObj.toString());
            } else {
                return "";
            }
        } else {
            Log.e(TAG, "Error with");
            return "";
        }
    }

    int getArrayIDFromName(JSONArray jarr, String name) {
        //list in list is not allowed!
        int pos = -1;
        Object obj;
        JSONObject jobj;
        String key;
        Boolean bIDFound = false;
//        dt.logV("getArrayIDFromName jarr name jarr.length()", jarr, name, jarr.length());
        for (int i = 0; i < jarr.length(); i++) {
            obj = jarr.opt(i);
            if (obj instanceof JSONObject) {
                jobj = (JSONObject) obj;
                Iterator<String> namesIter = jobj.keys();
                while (namesIter.hasNext()) {
                    key = namesIter.next().toString();
                    if (name.equals(key)) {
                        pos = i;
                        bIDFound = true;
                        break;
                    }
                }
            }
            if (obj instanceof JSONArray) {
                jarr = (JSONArray) obj;
                if (getArrayIDFromName(jarr, name) > -1) {
                    pos = i;
                    bIDFound = true;
                    break;
                }
            }
            if (bIDFound) {
                break;
            }
        }
        return pos;
    }

    private String clearNotValueSymbols(String strToClear) {
        return strToClear.replace("\n", "").replace("\r", "");
    }

//    private String returnOnlyNameIfList(String str) {
//        if (str.lastIndexOf("]") == (str.length() - 1)) {
//
//        }
//        if (name.indexOf(liInd) == (name.length() - liInd.length())) { //element is a pathList
//            name = name.substring(0, name.length() - liInd.length());
//        }
//    }


}


