package org.psylo.sensgraph;

import android.content.Context;
import android.content.res.Resources;

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
 */

class JSONWorker {

    private static final String TAG = "SensGraphJSONWorker"; //dev
    private static final String NAME_SEP = ":"; //separator used in JSON paths
    private static final char NULL_CHAR = 0;//null char, used for list identification
    private JSONObject mainJObj;
    ArrayList<String> namesList;
    ArrayList<String> valuesList;

    //error constants
    private static final String ERR_GENERAL = "ERR_GENERAL"; //general not unindentified error
    private static final String ERR_URL = "ERR_URL"; //URL error
    private static final String ERR_TIMEOUT = "ERR_TIMEOUT"; //timeout error
    private static final String ERR_IO = "ERR_IO"; //IO error
    private static final String ERR_JSON = "ERR_JSON"; //IO error

    /**
     * Returns Http response
     * If error occures returns error code msg
     * ERR_GENERAL = Unknown general error
     * ERR_URL = URL parsing error
     * ERR_TIMEOUT = TimeOut error
     * ERR_IO = IO error
     * */
    String GetHttpResponse(String urlString) {
        URL url;
        String response;
        try {
            url = new URL(urlString);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(10000); //10s
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            response = readStream(in);
            urlConnection.disconnect();
        } catch (MalformedURLException e) {
            return ERR_URL;
        } catch (SocketTimeoutException e) {
            return ERR_TIMEOUT;
        } catch (IOException e) {
            return ERR_IO;
        } catch (Exception e) {
            return ERR_GENERAL;
        }
        return response;
    }

    String parseJSONFromResponse(Context context, String response) {
        String errMsg;
        errMsg = getFullErrorString(context, response); //checks if response is an error response
        if (errMsg.equals("")) { //no error
            errMsg = mainJsonObjFromString(response);
            if (!errMsg.equals("")) { //error
                errMsg = getFullErrorString(context, errMsg);
            }
        }
        return errMsg;
    }

    private String getFullErrorString(Context context, String strToParse) {
        String errMsg = "";
        Resources res = context.getResources();
        try {
            switch (strToParse) {
                case ERR_URL:
                    errMsg = res.getString(R.string.json_worker_err1);
                    break;
                case ERR_TIMEOUT:
                    errMsg = res.getString(R.string.json_worker_err2);
                    break;
                case ERR_IO:
                    errMsg = res.getString(R.string.json_worker_err3);
                    break;
                case ERR_JSON:
                    errMsg = res.getString(R.string.json_worker_err4);
                    break;
                case ERR_GENERAL:
                    errMsg = res.getString(R.string.json_worker_err0);
                    break;
                default: //nor error
                    break;
            }
        } catch (Exception e) {
            errMsg = res.getString(R.string.json_worker_err00);
        }
        return errMsg;
    }

    private static String readStream(InputStream is) {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append("\n");
            }
        } catch (IOException e) {
            DevTools.logE(TAG, e);
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                DevTools.logE(TAG, e);
            }
        }
        return sb.toString();
    }

    String mainJsonObjFromString(String jString) {
        String errStr = "";
        try {
            mainJObj = new JSONObject(jString);
        } catch (JSONException e) {
            errStr = ERR_JSON;
        }
        return errStr;
    }

    void makeNamesValuesLists() {
        namesList = new ArrayList<>();
        valuesList = new ArrayList<>();
        if (mainJObjLoaded()) {
            DevTools.log(TAG, "mainJObj.toString()", mainJObj.toString());
            addNamesFromJsonObjectToList(namesList, valuesList, mainJObj, "", 0);
        }
    }

    private void addNamesFromJSONArrayToList(ArrayList<String> nameList, ArrayList<String> valueList,
                                            JSONArray jArray, String parentNames, int level) {
        String localNames;
        int locLevel = level + 1;
        for (int i = 0; i < jArray.length(); i++) {
            Object obj = jArray.opt(i);
            if (obj != null) {
                localNames = parentNames;
                if (level > 0) {
                    //null char added to identify tabbed text later parsing path
                    localNames += NULL_CHAR;
                    localNames += repeatedString("  ", level);
                }
                localNames += "\0[" + String.valueOf(i) +"]\0:";
                if (obj instanceof JSONObject) {
                    localNames += "\n";
                    addNamesFromJsonObjectToList(nameList, valueList, (JSONObject) obj, localNames, locLevel); //sets next element JSONObject
                } else if (obj instanceof JSONArray) {
                    localNames += "\n";
                    addNamesFromJSONArrayToList(nameList, valueList, (JSONArray) obj, localNames, locLevel); //sets next element JSONArray
                } else { //just a value String, Boolean etc.
                    nameList.add(localNames);
                    valueList.add(clearNotValueSymbols(obj.toString()));
                }
            }
        }
    }

    private Boolean mainJObjLoaded() {
        return mainJObj != null;
    }

    /**
     * Adds JSON names paths and values to corresponding ArrayLists from given JSONObject
     *
     */
    private void addNamesFromJsonObjectToList(ArrayList<String> namesList, ArrayList<String> valuesList,
                                             JSONObject jObj, String parentNames, int level) {
        Iterator<String> namesIter = jObj.keys();
        String currName;
        String localNames;
        Object obj;
        JSONObject jsonObj;
        JSONArray jsonArr;
        int locLevel = level + 1;

        while (namesIter.hasNext()) {
            currName = namesIter.next();
            localNames = parentNames;
            if (level > 0) {
                localNames = localNames + NULL_CHAR + repeatedString(" ", level);
            }
            if (currName.equals("")) {
                localNames = localNames + "\"\""; //indicates empty string
            } else {
                    localNames = localNames + "\"" + currName + "\"";
            }
            localNames += NAME_SEP;
            obj = jObj.opt(currName);
            if (obj != null) {
                if (obj instanceof JSONObject) {
                    try {
                        jsonObj = jObj.getJSONObject(currName); //gets value
                        localNames += "\n";
                        addNamesFromJsonObjectToList(namesList, valuesList, jsonObj, localNames, locLevel);
                    } catch (Exception e) {
                        DevTools.logE(TAG, e);
                    }
                } else if (obj instanceof JSONArray) {
                    try {
                        jsonArr = jObj.getJSONArray(currName);
                        localNames += "\n";
                        addNamesFromJSONArrayToList(namesList, valuesList, jsonArr, localNames, locLevel);
                    } catch (Exception e) {
                        DevTools.logE(TAG, e);
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
            Object valueObj = new Object();
            int listStartIdx;
            int listIdx = -1;
            jObj = mainJObj;

            //clears unnecessary \" chars and converts element separators to \", to split later
            path = path.substring(1)
                    .replace("\":\"", "\"") //obj, next obj
                    .replace("\":", "\"")   //last obj
                    .replace("]\0:\0[", "]\"\0[") //list element next list element
                    .replace("]\0:", "]");  //list element end
            String[] pathList = path.split("\"");
            for (String name : pathList) {
                if (name.equals("\"\"")) { //empty string in path saved as ""
                    name = "";
                } else {
                    //list name processing
                    listIdx = -1;
                    if (name.substring(name.length() - 1).equals("]")) {
                        listStartIdx = name.lastIndexOf("\0[");
                        if (listStartIdx > -1) {
                            listIdx = Integer.parseInt(name.substring(listStartIdx + 2, name.length() - 1));
                        }
                    }
                }
                if (listIdx > -1) { //list element
                    valueObj = jArr.opt(listIdx);
                } else {
                    valueObj = jObj.opt(name);
                }
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

    private String clearNotValueSymbols(String strToClear) {
        return strToClear.replace("\n", "").replace("\r", "");
    }

    private String repeatedString(String strToRepeat, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(strToRepeat);
        }
        return sb.toString();
    }
}


