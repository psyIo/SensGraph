package org.psylo.sensgraph;

import android.content.Context;
import android.content.res.Resources;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Iterator;

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

    /**
     * Returns Http response
     * If error occures returns error code msg
     * "err0 = Unknown error
     * "err1 = URL parsing error
     * "err2 = TimeOut error
     * "err3 = IO error
     * */
    String GetHttpResponse(String urlString) {
        URL url;
        String response = "";
        try {
            url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000); //10s
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = readStream(in);
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            return "\"err1";
        } catch (SocketTimeoutException e) {
            return "\"err2";
        } catch (IOException e) {
            return "\"err3";
        } catch (Exception e) {
            return "\"err0";
        }
        return response;
    }

    public String parseJSONFromResponse(Context context, String response) {
        String errMsg;
        errMsg = getFullErrorString(context, response); //checks if response is an error response
        if (errMsg.equals("")) { //no error
            errMsg = mainJsonObjFromString(response);
            if (!errMsg.equals("")) { //error occured
                errMsg = getFullErrorString(context, errMsg);
            }
        }
        return errMsg;
    }

    public String getFullErrorString(Context context, String strToParse) {
        int errNo;
        String errMsg = "";
        Resources res = context.getResources();
        try {
            if (strToParse.substring(0, 4).equals("\"err")) {
                errNo = Integer.parseInt(strToParse.substring(4));
                switch (errNo) {
                    case 1:
                        errMsg = res.getString(R.string.json_worker_err1);
                        break;
                    case 2:
                        errMsg = res.getString(R.string.json_worker_err2);
                        break;
                    case 3:
                        errMsg = res.getString(R.string.json_worker_err3);
                        break;
                    case 4:
                        errMsg = res.getString(R.string.json_worker_err4);
                        break;
                    default: //case 0:
                        errMsg = res.getString(R.string.json_worker_err0);
                        break;
                }
            }
        } catch (Exception e) {
            errMsg = res.getString(R.string.json_worker_err00);
        }
        return errMsg;
    }

    public static String readStream(InputStream is) {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
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

    public String mainJsonObjFromString(String jString) {
        String errStr = "";
        try {
            mainJObj = new JSONObject(jString);
        } catch (JSONException e) {
            errStr = "\"err4";
            Log.e(TAG, "JSONException ", e); //dev
        }
        return errStr;
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
                    addNamesFromJSONArrayToList(nameList, valueList, (JSONArray) obj, localNames + "/"); //sets next element JSONArray
                } else { //just a value String, Boolean etc.
                    nameList.add(localNames);
                    valueList.add(clearNotValueSymbols(obj.toString()));
                }
            }
        }
    }

    Boolean mainJObjLoaded() {
        return mainJObj != null;
    }

    /**
     * Adds JSON names paths and values to corresponding ArrayLists from given JSONObject
     *
     */
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
            if (currName.equals("")) {
                localNames = parentNames + "\"\""; //indicates empty string
            } else {
                localNames = parentNames + currName;
            }
            localNames += "/";
            obj = jObj.opt(currName);
            if (obj != null) {
                if (obj instanceof JSONObject) {
                    try {
                        jsonObj = jObj.getJSONObject(currName); //gets value
                        addNamesFromJsonObjectToList(namesList, valuesList, jsonObj, localNames);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception", e);
                    }
                } else if (obj instanceof JSONArray) {
                    try {
                        jsonArr = jObj.getJSONArray(currName);
                        addNamesFromJSONArrayToList(namesList, valuesList, jsonArr, localNames);
                    } catch (Exception e) {
                        Log.e(TAG, "Exception", e);
                    }
                } else { //just a value String, Boolean etc.
                    namesList.add(localNames);
                    valuesList.add(clearNotValueSymbols(obj.toString()));
                }
            }
        }
    }

    String getValueFromPath(String path) {
        if (mainJObjLoaded() && (path != null) && (!path.equals(""))) {
            JSONObject jObj;
            JSONArray jArr = new JSONArray();
            String sep = "/";
            Object valueObj = new Object();
            int listStartIdx;
            int listIdx = -1;
            dt.logV(TAG, "getValueFromPath path", path);
            jObj = mainJObj;
            path = path.substring(1); //removes first separator symbol
            String[] pathList = path.split(sep);
            for (String name : pathList) {
                dt.logV("name", name);
                if (name.equals("\"\"")) { //empty string in path saved as ""
                    name = "";
                } else {
                    listIdx = -1;
                    //list name processing
                    if (name.substring(name.length() - 1).equals("]")) {
                        listStartIdx = name.lastIndexOf("[");
                        listIdx = Integer.parseInt(name.substring(listStartIdx + 1, name.length() - 1));
                        if (listIdx < 0) {
                            dt.logE(TAG, "getValueFromPath() Error parsing path listIdx < 0");
                            return "";
                        }
                        name = name.substring(0, listStartIdx);
                        dt.logV("name", name, "listIdx", listIdx);
                    }
                }
                if (listIdx > -1) { //list element
                    valueObj = jArr.opt(listIdx);
                } else {
                    valueObj = jObj.opt(name);
                }
                dt.logV("valueObj", valueObj);
                if (valueObj instanceof JSONObject) {
                    jObj = (JSONObject) valueObj;
                } else if (valueObj instanceof JSONArray) {
                    jArr = (JSONArray) valueObj;
                } else { //Value found. String, int etc.
                    break;
                }
            }

            if (valueObj != null) {
                return clearNotValueSymbols(valueObj.toString());
            } else {
                return "";
            }
        } else {
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
//                    key = namesIter.next().toString();
                    key = namesIter.next();
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


