package org.psylo.sensgraph;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
//import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AppWidgetProvider used for SensGraph widget
 * Created by psylo on 17.2.1.
 */

public class SensGraphWidgetProvider extends AppWidgetProvider {

    static final String TAG = "SensGraphWidgetProvider";
    static final String APP_NAME = "SensGraph";
    static final DevTools dt = new DevTools(); //dev
    protected static AlarmManager alarmManager;
    protected static SimpleDb settings = new SimpleDb(10);
//    settings.entries.fields structure:
//    0: (String) sensorNamePath
//    1: (String) sensorValuePath
//    2: (String) url
//    3: (PendingIntent) pendingIntent to update widget
//    4: (Long) Update interval in minutes
//    5: (ArrayList) Sensor values List, to draw graph from
//    6: ---
//    7: ---
//    8: ---
//    9: ---

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int widgetId : appWidgetIds) {
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
                //cancel alarmManager job
                long interval;
                if (settings.getField(3) == null) {
                    settings.setField(3, updateWidgetPendingIntent);
                    if (settings.getField(4) != null) {
                        interval = 1000 * 60 * (Long) settings.getField(4); //minutes
                    } else {
                        interval = 1000 * 60 * 30; //default 30 minutes
                    }
                    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                            interval, updateWidgetPendingIntent);
                }
            }
        }
    }

    /**
     * Called in response to the ACTION_APPWIDGET_OPTIONS_CHANGED broadcast when this widget has been layed out at a new size.
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
//        Log.v(TAG,String.format("onAppWidgetOptionsChanged wiID %03d", appWidgetId));
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


        UpdateViewFromJSON(Context context, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager,
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
         * returns http response from String URL supplied
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
            String sSensorValue = "";
            if (errStr.equals("")) { //no error
                jWorker.mainJsonObjFromString(result);
                remoteViews.setTextViewText(R.id.sensorNameTextView,
                        jWorker.getValueFromPath(sensorNamePath));
                sSensorValue = jWorker.getValueFromPath(sensorValuePath);
                remoteViews.setTextViewText(R.id.sensorValueTextView,
                        sSensorValue);
            } else { //error
                remoteViews.setTextViewText(R.id.sensorNameTextView,
                        context.getString(R.string.just_error) + " " + errStr);
                remoteViews.setTextViewText(R.id.sensorValueTextView, "");
            }
            Date currSysTime = new Date();
            currSysTime.setTime(System.currentTimeMillis());
            remoteViews.setTextViewText(R.id.sensorUpdatedDtTextView, String.format(
                    getDefaultLocale(context), "%1$tY.%1$tm.%1$td %1$tT", currSysTime));

            //dev+
//            dt.logV(TAG, "settings.getField(5)", settings.getField(5), "settings", settings);


            try {
                float fSensorValue = Float.parseFloat(sSensorValue);
                int iBitmapWidth = 100;
                int iBitmapHeight = 60;
                int iMaxValuesAmount = 50;
                float fValueRange;// = (float) iBitmapHeight;
                float fVerticalStepSizePixelMultiplyier = iBitmapHeight / iBitmapHeight;
                float fMaxValue = Float.MIN_VALUE;
                float fMinValue = Float.MAX_VALUE;
                List sensorValuesList = (ArrayList) settings.getField(5);
                if (sensorValuesList.size() == iMaxValuesAmount) {
                    sensorValuesList.remove(0);
                }
                sensorValuesList.add(fSensorValue);
                if (sensorValuesList.size() > 1) {
                    //finding min max values
                    for (Object value : sensorValuesList) {
                        fSensorValue = (float) value;
                        if (fSensorValue > fMaxValue) {
                            fMaxValue = fSensorValue;
                        }
                        if (fSensorValue < fMinValue) {
                            fMinValue = fSensorValue;
                        }
                    }
                    fValueRange = fMaxValue - fMinValue;
                    fVerticalStepSizePixelMultiplyier = fValueRange / iBitmapHeight;// + 1;
//                int iStepSize = Integer.valueOf(Math.round(100 / sensorValuesList.size()));
                    int iStepSize = Math.round(iBitmapWidth / sensorValuesList.size());
//                int iVerticalStepSize = Integer.valueOf()
                    dt.logV("iStepSize", iStepSize, "fMinValue", fMinValue, "fMaxValue", fMaxValue,
                            "fValueRange", fValueRange, "fVerticalStepSizePixelMultiplyier", fVerticalStepSizePixelMultiplyier);
                    Bitmap bitmap = Bitmap.createBitmap(iBitmapWidth, iBitmapHeight, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);

                    Paint paint = new Paint();
                    paint.setColor(Color.WHITE);
                    paint.setStrokeWidth(2);
                    paint.setStyle(Paint.Style.STROKE);
                    paint.setFlags(Paint.ANTI_ALIAS_FLAG);

//                Paint backgroundPaint = new Paint();
//                backgroundPaint.setColor(Color.BLACK);
//                backgroundPaint.setAlpha(50);
//
//                Rect backgroundRect = new Rect(0, 0, 100, 60);
//                canvas.drawRect(backgroundRect, backgroundPaint);

                    float fSensValue;
                    int iX = 0;
                    Path graphPath = new Path();
//                    graphPath.moveTo(iX, fValueRange / 2);
                    graphPath.moveTo(0, ((float) sensorValuesList.get(0) - fMinValue) / fVerticalStepSizePixelMultiplyier);
                    for (Object value : sensorValuesList) {
                        fSensValue = (float) value;
                        graphPath.lineTo(iX, (fSensValue - fMinValue) / fVerticalStepSizePixelMultiplyier);
                        dt.logV(TAG, "x, y", iX, fSensValue / fVerticalStepSizePixelMultiplyier, "fSensValue", fSensValue);
                        iX += iStepSize;
                    }
                    canvas.drawPath(graphPath, paint);
                    remoteViews.setImageViewBitmap(R.id.graphImageView, bitmap);
                }
            } catch (Exception e) {
                dt.logE(TAG, e);
            }

            //dev-
            widgetManager.updateAppWidget(WidgetID, remoteViews);
        }
    }

//    public class GraphView extends View {
//
//        public GraphView(Context context) {
//            super(context);
//        }
//
//        @Override
//        protected void onDraw(Canvas canvas) {
//            super.onDraw(canvas);
//
////            Pt[] myPath = { new Pt(10, 10),
////                    new Pt(20, 20),
////                    new Pt(20, 50),
////                    new Pt(40, 50),
////                    new Pt(40, 20)
////            };
////
////            Paint paint = new Paint();
////            paint.setColor(Color.WHITE);
////            paint.setStrokeWidth(3);
////            paint.setStyle(Paint.Style.STROKE);
////            Path path = new Path();
////
////            path.moveTo(myPath[0].x, myPath[0].y);
////            for (int i = 1; i < myPath.length; i++){
////                path.lineTo(myPath[i].x, myPath[i].y);
////            }
////            canvas.drawPath(path, paint);
//        }
//    }
//
//    private class Pt {
//        float x, y;
//
//        Pt(float _x, float _y){
//            x = _x;
//            y = _y;
//        }
//    }
}

