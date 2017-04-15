package org.psylo.sensgraph;

/**
 * Created by psylo on 17.2.1.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
//import android.app.Service;
import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.widget.RemoteViews;

import java.util.Date;

//import java.util.Random;
//import java.net.URL;

public class SensGraphWidgetProvider extends AppWidgetProvider {

    static final String TAG = "SensGraphWidgetProvider";
    static final String APP_NAME = "SensGraph";
    static DevTools dt = new DevTools(); //dev
    protected static AlarmManager alarmManager;
    protected static SimpleDb settings = new SimpleDb(10);
//    settings.entries.fields structure:
//    0: (String) sensorNamePath
//    1: (String) sensorValuePath
//    2: (String) url
//    3: (PendingIntent) pendingIntent to update widget
//    4: (Long) Update interval in minutes
//    5: ---
//    6: ---
//    7: ---
//    8: ---
//    9: ---

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        final int count = appWidgetIds.length;
//        Log.v(TAG, "onUpdate called length appWidgetIds " + Integer.toString(count)); //dev
        if (count > 0) {
            dt.logV(TAG, "onUpdate", "appWidgetIds[0]", appWidgetIds[0]);
        }
//        setNameTest(); //dev
//        dt.logV("nameTest onUpdate", nameTest, "sensorNamePath", sensorNamePath, "this", this);
        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.simple_widget);
            Intent intent = new Intent(context, SensGraphWidgetProvider.class);

            //This causes each widget to have a unique PendingIntent
//            String URI_SCHEME = "SENS_GRAPH";
            Uri data = Uri.withAppendedPath(
                    Uri.parse(APP_NAME + "://widget/id/")
                    ,String.valueOf(widgetId));
            intent.setData(data);

            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);

            PendingIntent updateWidgetPendingIntent = PendingIntent.getBroadcast(context,
                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            remoteViews.setOnClickPendingIntent(R.id.widget_relative_layout, updateWidgetPendingIntent);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);

//            dt.logE("onUpdate called");
//            dt.logV("APP_NAME + \"_\" + String.valueOf(widgetId)", APP_NAME + "_" + String.valueOf(widgetId));
//            SharedPreferences settings = context.getSharedPreferences(APP_NAME + "_" + String.valueOf(widgetId), 0);
            if (settings.getCurrEntry(widgetId)) {
                UpdateViewFromJSON jsonTask = new UpdateViewFromJSON(remoteViews, widgetId,
                        appWidgetManager, settings.getField(0).toString(), settings.getField(1).toString());
                jsonTask.execute(settings.getField(2).toString());

                //updates when device is awake only
                if (alarmManager == null) {
                    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                }

                //sets pending intent in position 3 (4) to reuse it in onDeleted() method
                if (settings.getField(3) == null) {
                    settings.setField(3, updateWidgetPendingIntent);
                    dt.logV("updateWidgetPendingIntent set to", updateWidgetPendingIntent,
                            "widgetId", widgetId);
                    if (settings.getField(4) != null) {
                        long interval = 1000 * 60 * (Long) settings.getField(4); //minutes
                        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                                interval, (PendingIntent) settings.getField(3));

                        dt.logV("interval", interval);
                    }
                    dt.logV("updateWidgetPendingIntent setRepeating", settings.getField(3),
                            "widgetId", widgetId);
                } else {
//                    alarmManager.cancel((PendingIntent) settings.getField(3));
//                    dt.logV("updateWidgetPendingIntent cancel", updateWidgetPendingIntent,
//                            "widgetId", widgetId);
                }
//                long interval = 1000*20;
//                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
//                        interval, (PendingIntent) settings.getField(3));
//                dt.logV("updateWidgetPendingIntent setRepeating", settings.getField(3),
//                        "widgetId", widgetId);

//                dt.logV("settings.getField(0), settings.getField(1)", settings.getField(0),settings.getField(1),
//                        "settings.getField(2)", settings.getField(2), "settings.getField(3)", settings.getField(3));
            }
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        /**
         * Called in response to the ACTION_APPWIDGET_OPTIONS_CHANGED broadcast when this widget has been layed out at a new size.
         */
        Log.v(TAG,String.format("onAppWidgetOptionsChanged wiID %03d", appWidgetId));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        for (int i : appWidgetIds) {
            if (settings.getCurrEntry(i)) {
                if (settings.getField(3) != null) {
                    PendingIntent pi = (PendingIntent) settings.getField(3);
                    alarmManager.cancel(pi);
//                    dt.logV("onDeleted pi", pi, "alarmManager", alarmManager);
                }
                settings.deleteCurrEntry();
            }
        }
    }

    private class UpdateViewFromJSON extends AsyncTask<String, Integer, String> {
        private RemoteViews remoteViews;
        private int WidgetID;
        private AppWidgetManager widgetManager;
        private JSONWorker jWorker;
        private String sensorNamePath;
        private String sensorValuePath;

        public UpdateViewFromJSON(RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager,
                                  String nameName, String valueName){
            this.remoteViews = views;
            this.WidgetID = appWidgetID;
            this.widgetManager = appWidgetManager;
            this.jWorker = new JSONWorker();
            this.sensorNamePath = nameName;
            this.sensorValuePath = valueName;
        }

        protected String doInBackground(String... strings) {
            /**
             * returns http response from String supplied
             */
            String response = "";
            if (strings.length > 0) {
                response = JSONWorker.GetHttpResponse(strings[0]);
            }
            return response;
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
            
        }

        protected void onPostExecute(String result) {
            jWorker.mainJsonObjFromString(result);
            remoteViews.setTextViewText(R.id.sensorNameTextView, jWorker.getValueFromPath(sensorNamePath));
            remoteViews.setTextViewText(R.id.sensorValueTextView, jWorker.getValueFromPath(sensorValuePath));
            Date currSysTime = new Date();
            currSysTime.setTime(System.currentTimeMillis());
            remoteViews.setTextViewText(R.id.sensorUpdatedDtTextView, String.format("%1$tY.%1$tm.%1$td %1$tT", currSysTime));
            widgetManager.updateAppWidget(WidgetID, remoteViews);
        }
    }
}

