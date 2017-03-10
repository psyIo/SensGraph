package org.psylo.sensgraph;

/**
 * Created by psylo on 17.2.1.
 */

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

import org.json.JSONObject;
import org.json.JSONException;
import org.json.JSONArray;

import java.util.Random;
import java.net.URL;

public class SimpleWidgetProvider extends AppWidgetProvider {

    static final String TAG = "SensGraphWidgetProvider";
    static int COUNT = 0; //dev

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;
        Log.v(TAG, " length appWidgetIds " + Integer.toString(count)); //dev

        URL url = null;
        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];
            @SuppressLint("DefaultLocale") String number = String.format("%03d",
                    (new Random().nextInt(900) + 100));
            Log.v(TAG, "Update widget id " + Integer.toString(widgetId)); //dev
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.simple_widget);
//            remoteViews.setTextViewText(R.id.textView, number);
            Intent intent = new Intent(context, SimpleWidgetProvider.class);

            //This causes each widget to have a unique PendingIntent
            String URI_SCHEME = "SENS_GRAPH";
            Uri data = Uri.withAppendedPath(
                    Uri.parse(URI_SCHEME + "://widget/id/")
                    ,String.valueOf(widgetId));
            intent.setData(data);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);
            remoteViews.setOnClickPendingIntent(R.id.widget_relative_layout, pendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
//            try {
////                url = new URL("http://46.251.48.58:6969/get_json/");
//                url = new URL("http://api.thingspeak.com/channels/99791/feeds.json?results=1");
//            } catch (Exception e) {
//                Log.e(TAG, "Exception making URL", e);
//            }
            Log.v(TAG, "appWidgetManager onUpdate1: " + appWidgetManager.toString()); //dev
            final UpdateViewFromJSON jsonTask = new UpdateViewFromJSON(remoteViews, widgetId, appWidgetManager);
            Log.v(TAG, "appWidgetManager onUpdate2: " + appWidgetManager.toString()); //dev
//            jsonTask.execute(url);
            jsonTask.execute("http://api.thingspeak.com/channels/99791/feeds.json?results=1");
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        /**
         * Called in response to the ACTION_APPWIDGET_OPTIONS_CHANGED broadcast when this widget has been layed out at a new size.
         */
        Log.v(TAG,String.format("onAppWidgetOptionsChanged wiID %03d", appWidgetId));
    }

    private class UpdateViewFromJSON extends AsyncTask<String, Integer, String> {
        private RemoteViews remoteViews;
        private int WidgetID;
        private AppWidgetManager WidgetManager;
        private JSONWorker jWorker;

        public UpdateViewFromJSON(RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager){
            this.remoteViews = views;
            this.WidgetID = appWidgetID;
            this.WidgetManager = appWidgetManager;
            this.jWorker = new JSONWorker();
//            Log.v(TAG, "appWidgetManagerUpdateViewFromJSON consturctor: " + appWidgetManager.toString()); //dev
//            Log.v(TAG, "appWidgetManagerUpdateViewFromJSON this consturctor: " + this.WidgetManager.toString()); //dev
        }

        protected String doInBackground(String... strings) {
            /**
             * Returns empty string because doInBackground must return something
             * Result actually passed jWorker object's mainJObj attribute (JSONObject type)
             */

            JSONWorker jWorker = new JSONWorker();
            if (strings.length > 0) {
//                jWorker.mainJsonObjFromString(jWorker.GetHttpResponse(strings[0]));
                if (jWorker.makeJsonFromHttpResponse(strings[0])) {
                    COUNT += 1; //dev
                }
            }
            String response = "";
            return response;
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        protected void onPostExecute(String result) {

            remoteViews.setTextViewText(R.id.resultTextView, Integer.toString(COUNT) + " " + result); //dev
            WidgetManager.updateAppWidget(WidgetID, remoteViews);
//            Log.v(TAG, "onPostExecute result: " + result); //dev
//            try {
////                JSONObject jObj = new JSONObject(result);
//                if (jWorker.mainJObj != null) {
//                    JSONObject jObj = jWorker.mainJObj;
//                    JSONArray jsonNames = jObj.names(); //dev
//                    Log.v(TAG, "jObj: " + jsonNames); //dev
//
////                    for (int i = 0; i < jsonNames.length(); i++) {
////                        //                    JSONObject jo = jsonNames.getJSONObject(i); //jsonNames.getString(i));
////                        //                    Log.v(TAG, "jsonNames.jsonNames.getJSONObject(i).toString() " + jsonNames.getJSONObject(i).toString());
////                        //                    JSONObject job = jObj.getJSONObject(jsonNames.get(i).toString());
////                        //                    JSONObject job = jObj.getJSONObject(jsonNames.get(i).toString());
////                        //                    Log.v(TAG, "job.toString() " + job.toString());
////                        Object obj = jObj.get(jsonNames.get(i).toString());
////                        if (obj instanceof JSONObject) {
////                            Log.v(TAG, "obj instanceof JSONObject");
////                        } else if (obj instanceof JSONArray) {
////                            Log.v(TAG, "obj instanceof JSONArray");
////                        }
////
////                        Log.v(TAG, "obj.toString() " + obj.toString());
////                        Log.v(TAG, "jsonNames.get(i).toString() " + jsonNames.get(i).toString());
////                        //                    Log.v(TAG, '');
////                        Log.v(TAG, "jObj: " + String.valueOf(i) + " value: " + jsonNames.getString(i));// +
////                        //                            "obj length: " + jobj.toString()); //dev
////                    }
//                }
//            } catch (Exception e) {
//                Log.e(TAG, "Error parsing data " + e.toString());
//            }

//            Log.v(TAG, "onPostExecute WidgetManager: " + WidgetManager.toString()); //dev
//            Intent intent = new Intent(this,SimpleWidgetProvider.class);
//                intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//                // Use an array and EXTRA_APPWIDGET_IDS instead of AppWidgetManager.EXTRA_APPWIDGET_ID,
//                // since it seems the onUpdate() is only fired on that:
//                int[] ids = {widgetId};
//                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
//                sendBroadcast(intent);
//            showDialog("Downloaded " + result + " bytes");
        }
    }
}

