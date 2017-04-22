package org.psylo.sensgraph;

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
import java.util.Locale;

//import java.util.Random;
//import java.net.URL;

/**
 * AppWidgetProvider used for SensGraph widget
 * Created by psylo on 17.2.1.
 */

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
        for (int i = 0; i < count; i++) {
            int widgetId = appWidgetIds[i];
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.simple_widget);
            Intent intent = new Intent(context, SensGraphWidgetProvider.class);

            //This causes each widget to have a unique PendingIntent
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

            if (settings.getCurrEntry(widgetId)) {
                UpdateViewFromJSON jsonTask = new UpdateViewFromJSON(context, remoteViews, widgetId,
                        appWidgetManager, settings.getField(0).toString(), settings.getField(1).toString());
                jsonTask.execute(settings.getField(2).toString());

                //updates when device is awake only
                if (alarmManager == null) {
                    alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                }

                //sets pending intent in position 3 (4) to reuse it in onDeleted() method to
                // cancel alarmManager job
                long interval;
                if (settings.getField(3) == null) {
                    settings.setField(3, updateWidgetPendingIntent);
                    if (settings.getField(4) != null) {
                        interval = 1000 * 60 * (Long) settings.getField(4); //minutes
                    } else {
                        interval = 1000 * 60 * 30; //30 minutes
                    }
                    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                            interval, updateWidgetPendingIntent);
                    dt.logV("updateWidgetPendingIntent setRepeating", settings.getField(3),
                            "widgetId", widgetId);
                }
            }
        }
    }

    /**
     * Called in response to the ACTION_APPWIDGET_OPTIONS_CHANGED broadcast when this widget has been layed out at a new size.
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
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

    /**
     * Return Locale as per Android SDK version
     * */
    @SuppressWarnings("deprecation")
    protected Locale getDefaultLocale(Context context) {
        Locale locale;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) { // 24 and newer (Nougat 7)
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else { // 23 and older
            locale = context.getResources().getConfiguration().locale;
        }
        return locale;
    }

    private class UpdateViewFromJSON extends AsyncTask<String, Integer, String> {
        private Context context;
        private RemoteViews remoteViews;
        private int WidgetID;
        private AppWidgetManager widgetManager;
        private JSONWorker jWorker;
        private String sensorNamePath;
        private String sensorValuePath;


        public UpdateViewFromJSON(Context context, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager,
                                  String nameName, String valueName){
            this.context = context;
            this.remoteViews = views;
            this.WidgetID = appWidgetID;
            this.widgetManager = appWidgetManager;
            this.jWorker = new JSONWorker();
            this.sensorNamePath = nameName;
            this.sensorValuePath = valueName;
        }

        /**
         * returns http response from String supplied
         */
        protected String doInBackground(String... strings) {
            String response = "";
            if (strings.length > 0) {
                response = jWorker.GetHttpResponse(strings[0]);
            }
            return response;
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
            
        }

        protected void onPostExecute(String result) {
            String errStr = jWorker.parseJSONFromResponse(context, result);
            if (errStr.equals("")) { //no error
                jWorker.mainJsonObjFromString(result);
                remoteViews.setTextViewText(R.id.sensorNameTextView, jWorker.getValueFromPath(sensorNamePath));
                remoteViews.setTextViewText(R.id.sensorValueTextView, jWorker.getValueFromPath(sensorValuePath));
                Date currSysTime = new Date();
                currSysTime.setTime(System.currentTimeMillis());
                remoteViews.setTextViewText(R.id.sensorUpdatedDtTextView, String.format(getDefaultLocale(context), "%1$tY.%1$tm.%1$td %1$tT", currSysTime));
                widgetManager.updateAppWidget(WidgetID, remoteViews);
            } else {
                //cia padaryti kad rodytu klaida widgete
                dt.logE("error occured getting Http repsonse, result = ", result);
            }
        }
    }
}

