package org.psylo.sensgraph;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * AppWidgetProvider used for SensGraph widget
 * Created by psylo on 17.2.1.
 */

public class SensGraphWidgetProvider extends AppWidgetProvider {

    static final String TAG = "SensGraphWidgetProvider";
    static final String APP_NAME = "SensGraph";
//    static final DevTools dt = new DevTools(); //dev
    protected static AlarmManager alarmManager;
//    protected static SimpleDb settings;// = new SimpleDb(10);
    ArrayList<String> debuggingArrayList = new ArrayList<>();

//    public SensGraphWidgetProvider() {
//        DevTools.log(TAG, "SensGraphWidgetProvider constructor called settings", settings);
//        if (settings == null) {
//            settings = new SimpleDb(10);
//        }
//    }

//    widgetConfig structure:
//    0: (String) sensorNamePath
//    1: (String) sensorValuePath
//    2: (String) url
//    3: (Long) Update interval in minutes
//    4: (String[]) Sensor values List, to draw graph from
//    6: ---
//    7: ---
//    8: ---
//    9: ---

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //manually updating widget appWidgetIds consists only of single widget id
//        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, 0);
        DevTools.log(TAG, "SensGraphWidgetProvider onUpdate appWidgetIds", appWidgetIds);
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

            String[] widgetConfig = FileDb.getEntry(context, widgetId);
            if (widgetConfig != null) {
                DevTools.log(TAG, "widgetConfig", widgetConfig);
                UpdateViewFromJSON jsonTask = new UpdateViewFromJSON(context, remoteViews, widgetId,
                        appWidgetManager, widgetConfig[1], widgetConfig[2]);
                jsonTask.execute(widgetConfig[3]);


                //sets pending intent in position 3 (4) to reuse it in onDeleted() method to
                //cancel alarmManager job
                long interval;
                if (!widgetConfig[4].equals("")) {
                    //updates when device is awake only
                    if (alarmManager == null) {
                        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    }
//                    settings.setField(3, updateWidgetPendingIntent);
                    if (!widgetConfig[4].equals("")) {
                        interval = 1000 * 60 * Long.parseLong(widgetConfig[4]) ; //minutes
                    } else {
                        interval = 1000 * 60 * 30; //default 30 minutes
                    }
                    alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime(),
                            interval, updateWidgetPendingIntent);

//                    updateWidgetDebugList(context, "alarmManager.setRepeating updateWidgetPendingIntent widgetId " + String.valueOf(widgetId));
                }
            }
        }
    }

    /**
     * Called in response to the ACTION_APPWIDGET_OPTIONS_CHANGED broadcast when this widget has been layed out at a new size.
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
//        dt.logV(TAG,String.format("onAppWidgetOptionsChanged wiID %03d", appWidgetId));
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        for (int i : appWidgetIds) {
//            if (settings.getCurrEntry(i)) {
//                if (settings.getField(3) != null) {
//                    PendingIntent pi = (PendingIntent) settings.getField(3);
//                    alarmManager.cancel(pi);
//                }
//                settings.deleteCurrEntry();
//            }
            FileDb.deleteEntry(context, i);
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

    public void updateWidgetDebugList(Context context, String strToUpdate) {
        Date currSysDate = new Date();
        currSysDate.setTime(System.currentTimeMillis());
        debuggingArrayList.add(String.format(
                getDefaultLocale(context), "%1$tY.%1$tm.%1$td %1$tT", currSysDate) + " " + strToUpdate);
    }

    private class UpdateViewFromJSON extends AsyncTask<String, Integer, String> {
        private Context context;
        private RemoteViews remoteViews;
        private int widgetID;
        private AppWidgetManager widgetManager;
        private JSONWorker jWorker;
        private String sensorNamePath;
        private String sensorValuePath;

        UpdateViewFromJSON(Context context, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager,
                                  String nameName, String valueName){
            this.context = context;
            this.remoteViews = views;
            this.widgetID = appWidgetID;
            this.widgetManager = appWidgetManager;
            this.jWorker = new JSONWorker();
            this.sensorNamePath = nameName;
            this.sensorValuePath = valueName;
        }

        /**
         * returns http response from String URL supplied
         */
        protected String doInBackground(String... urls) {
            String response = "";
            if (urls.length > 0) {
                response = jWorker.GetHttpResponse(urls[0]);
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
                try {

                    float fSensorValue = Float.parseFloat(sSensorValue);
                    int iMaxValuesAmount = 50;

                    String[] widgetConfig = FileDb.getEntry(context, widgetID);
                    ArrayList sensorValuesList;
                    if (widgetConfig != null) {
                        sensorValuesList = parseStringToFloatArray(widgetConfig[5]);
                        if (sensorValuesList.size() == iMaxValuesAmount) {
                            sensorValuesList.remove(0);
                        }
                        sensorValuesList.add(fSensorValue);
                        Bitmap bitmap = drawGraphBitmap(sensorValuesList);
                        if (bitmap != null) {
                            remoteViews.setImageViewBitmap(R.id.graphImageView, bitmap);
                        }


                        //update widgetConfig
                        Object[] newValues = sensorValuesList.toArray();
                        StringBuilder sb = new StringBuilder();
                        sb.append("[");
                        for (Object obj : newValues) {
                            sb.append(String.valueOf(obj));
                            sb.append(",");
                        }
                        sb.delete(sb.length()-1, sb.length());
                        sb.append("]");
                        widgetConfig[5] = sb.toString();
                        FileDb.saveEntry(context, widgetConfig);
//                        if (sensorValuesList.size() > 1) {
//                            //finding min max values
//                            for (Object value : sensorValuesList) {
//                                fSensorValue = (float) value;
//                                if (fSensorValue > fMaxValue) {
//                                    fMaxValue = fSensorValue;
//                                }
//                                if (fSensorValue < fMinValue) {
//                                    fMinValue = fSensorValue;
//                                }
//                            }
//                            fValueRange = fMaxValue - fMinValue;
//                            fVerticalStepSizePixelMultiplyier = fValueRange / iBitmapHeight;
//                            if (fVerticalStepSizePixelMultiplyier == 0) {
//                                fVerticalStepSizePixelMultiplyier = 1;
//                            }
//                            int iStepSize = Math.round(iBitmapWidth / sensorValuesList.size());
//                            Bitmap bitmap = Bitmap.createBitmap(iBitmapWidth, iBitmapHeight, Bitmap.Config.ARGB_8888);
//                            Canvas canvas = new Canvas(bitmap);
//
//                            Paint boundsPaint = new Paint();
//                            boundsPaint.setColor(Color.WHITE);
//                            boundsPaint.setStrokeWidth(5);
//                            boundsPaint.setStyle(Paint.Style.STROKE);
//                            boundsPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
//                            boundsPaint.setAlpha(90);
//
//                            Path boundsPath = new Path();
//                            boundsPath.moveTo(0, 3);
//                            boundsPath.lineTo(iBitmapWidth, 0);
//                            boundsPath.moveTo(0, iBitmapHeight);
//                            boundsPath.lineTo(iBitmapWidth, iBitmapHeight - 3);
//                            boundsPath.moveTo(0, iBitmapHeight / 2);
//                            boundsPath.lineTo(iBitmapWidth, iBitmapHeight / 2);
//                            canvas.drawPath(boundsPath, boundsPaint);
//
//                            Paint paintGraph = new Paint();
//                            paintGraph.setColor(Color.argb(150,255,0,0)); //red
//                            paintGraph.setStrokeWidth(10);
//                            paintGraph.setStyle(Paint.Style.STROKE);
//                            paintGraph.setFlags(Paint.ANTI_ALIAS_FLAG);
//
//                            Paint textPaint = new Paint();
//                            textPaint.setColor(Color.WHITE);
//                            textPaint.setTextSize(20);
//                            canvas.drawText(String.valueOf(fMinValue), iBitmapWidth - 100, iBitmapHeight - 1, textPaint);
//                            if (fMaxValue != fMinValue) {
//                                canvas.drawText(String.valueOf(fMaxValue), iBitmapWidth - 100, 15, textPaint);
//                                canvas.drawText(String.valueOf((fMaxValue + fMinValue) / 2), iBitmapWidth - 100, iBitmapHeight / 2, textPaint);
//                            }
//                            float fSensValue;
//                            int iX = 0;
//                            Path graphPath = new Path();
//                            graphPath.moveTo(0, iBitmapHeight - ((float) sensorValuesList.get(0) - fMinValue) / fVerticalStepSizePixelMultiplyier);
//                            for (Object value : sensorValuesList) {
//                                fSensValue = (float) value;
//                                graphPath.lineTo(iX, iBitmapHeight - (fSensValue - fMinValue) / fVerticalStepSizePixelMultiplyier);
//                                iX += iStepSize;
//                            }
//                            canvas.drawPath(graphPath, paintGraph);
//                            remoteViews.setImageViewBitmap(R.id.graphImageView, bitmap);
//                        }
                    }
//                    }
//                    List sensorValuesList = (ArrayList) settings.getField(5);
//                    if (sensorValuesList.size() == iMaxValuesAmount) {
//                        sensorValuesList.remove(0);
//                    }
//                    sensorValuesList.add(fSensorValue);
//                    if (sensorValuesList.size() > 1) {
//                        //finding min max values
//                        for (Object value : sensorValuesList) {
//                            fSensorValue = (float) value;
//                            if (fSensorValue > fMaxValue) {
//                                fMaxValue = fSensorValue;
//                            }
//                            if (fSensorValue < fMinValue) {
//                                fMinValue = fSensorValue;
//                            }
//                        }
//                        fValueRange = fMaxValue - fMinValue;
//                        fVerticalStepSizePixelMultiplyier = fValueRange / iBitmapHeight;
//                        if (fVerticalStepSizePixelMultiplyier == 0) {
//                            fVerticalStepSizePixelMultiplyier = 1;
//                        }
//                        int iStepSize = Math.round(iBitmapWidth / sensorValuesList.size());
////                    dt.logV("iStepSize", iStepSize, "fMinValue", fMinValue, "fMaxValue", fMaxValue,
////                            "fValueRange", fValueRange, "fVerticalStepSizePixelMultiplyier", fVerticalStepSizePixelMultiplyier);
//                        Bitmap bitmap = Bitmap.createBitmap(iBitmapWidth, iBitmapHeight, Bitmap.Config.ARGB_8888);
//                        Canvas canvas = new Canvas(bitmap);
//
//                        Paint boundsPaint = new Paint();
//                        boundsPaint.setColor(Color.WHITE);
//                        boundsPaint.setStrokeWidth(5);
//                        boundsPaint.setStyle(Paint.Style.STROKE);
//                        boundsPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
//                        boundsPaint.setAlpha(90);
//
//                        Path boundsPath = new Path();
//                        boundsPath.moveTo(0, 3);
//                        boundsPath.lineTo(iBitmapWidth, 0);
//                        boundsPath.moveTo(0, iBitmapHeight);
//                        boundsPath.lineTo(iBitmapWidth, iBitmapHeight - 3);
//                        boundsPath.moveTo(0, iBitmapHeight / 2);
//                        boundsPath.lineTo(iBitmapWidth, iBitmapHeight / 2);
//                        canvas.drawPath(boundsPath, boundsPaint);
////                    dt.logV("canvas.drawPath(boundsPath, boundsPaint);", boundsPath);
//
//                        Paint paintGraph = new Paint();
//                        paintGraph.setColor(Color.argb(150,255,0,0)); //red
//                        paintGraph.setStrokeWidth(10);
//                        paintGraph.setStyle(Paint.Style.STROKE);
//                        paintGraph.setFlags(Paint.ANTI_ALIAS_FLAG);
//
//                        Paint textPaint = new Paint();
//                        textPaint.setColor(Color.WHITE);
//                        textPaint.setTextSize(20);
//                        canvas.drawText(String.valueOf(fMinValue), iBitmapWidth - 100, iBitmapHeight - 1, textPaint);
//                        if (fMaxValue != fMinValue) {
//                            canvas.drawText(String.valueOf(fMaxValue), iBitmapWidth - 100, 15, textPaint);
//                            canvas.drawText(String.valueOf((fMaxValue + fMinValue) / 2), iBitmapWidth - 100, iBitmapHeight / 2, textPaint);
//                        }
//                        float fSensValue;
//                        int iX = 0;
//                        Path graphPath = new Path();
//                        graphPath.moveTo(0, iBitmapHeight - ((float) sensorValuesList.get(0) - fMinValue) / fVerticalStepSizePixelMultiplyier);
//                        for (Object value : sensorValuesList) {
//                            fSensValue = (float) value;
//                            graphPath.lineTo(iX, iBitmapHeight - (fSensValue - fMinValue) / fVerticalStepSizePixelMultiplyier);
////                        dt.logV(TAG, "x, y", iX, iBitmapHeight - (fSensValue - fMinValue) / fVerticalStepSizePixelMultiplyier, "fSensValue", fSensValue);
//                            iX += iStepSize;
//                        }
//                        canvas.drawPath(graphPath, paintGraph);
//                        remoteViews.setImageViewBitmap(R.id.graphImageView, bitmap);
//                    }
                } catch (Exception e) {
                    DevTools.logE(TAG, e);
                }
            } else { //error
                remoteViews.setTextViewText(R.id.sensorNameTextView,
                        context.getString(R.string.just_error) + " " + errStr);
                remoteViews.setTextViewText(R.id.sensorValueTextView, "");

            }
            //set update datetime view
            Date currSysTime = new Date();
            currSysTime.setTime(System.currentTimeMillis());
            remoteViews.setTextViewText(R.id.sensorUpdatedDtTextView, String.format(
                    getDefaultLocale(context), "%1$tY.%1$tm.%1$td %1$tT", currSysTime));

            //updates debugging list
            Intent serviceIntent = new Intent(context, UpdateWidgetService.class);
            serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetID);
            serviceIntent.setData(Uri.parse(serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
            updateWidgetDebugList(context, "UpdateViewFromJSON widgetId " + String.valueOf(widgetID) +
                    " sSensorValue " + sSensorValue);
            serviceIntent.putStringArrayListExtra("LIST_VALUES", debuggingArrayList);
            //debugging list
//            remoteViews.setRemoteAdapter(R.id.widgetDebugList, serviceIntent);
//            widgetManager.notifyAppWidgetViewDataChanged(widgetID,R.id.widgetDebugList);

            widgetManager.updateAppWidget(widgetID, remoteViews);
        }

        @NonNull
        private ArrayList parseStringToFloatArray(String values) {
            if (values.equals("[]")) {
                return new ArrayList();
            }

            values = values.substring(1,values.length()-1); //removes [] symbols
            String[] valuesSplited = values.split(",");
            ArrayList resultList = new ArrayList();
            for (String str : valuesSplited) {
                resultList.add(Float.parseFloat(str));
            }

            DevTools.log(TAG, "parseStringToFloatArray resultList", resultList);
            return resultList;
        }

        private Bitmap drawGraphBitmap(ArrayList sensorValuesList) {

            if (sensorValuesList.size() < 1) {
                return null;
            }

            float fSensorValue;
            int iBitmapWidth = 500;
            int iBitmapHeight = 300;
            float fValueRange;
            float fVerticalStepSizePixelMultiplyier;
            float fMaxValue = Float.MIN_VALUE;
            float fMinValue = Float.MAX_VALUE;

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
            fVerticalStepSizePixelMultiplyier = fValueRange / iBitmapHeight;
            if (fVerticalStepSizePixelMultiplyier == 0) {
                fVerticalStepSizePixelMultiplyier = 1;
            }
            int iStepSize = Math.round(iBitmapWidth / sensorValuesList.size());
            Bitmap bitmap = Bitmap.createBitmap(iBitmapWidth, iBitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Paint boundsPaint = new Paint();
            boundsPaint.setColor(Color.WHITE);
            boundsPaint.setStrokeWidth(5);
            boundsPaint.setStyle(Paint.Style.STROKE);
            boundsPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            boundsPaint.setAlpha(90);

            Path boundsPath = new Path();
            boundsPath.moveTo(0, 3);
            boundsPath.lineTo(iBitmapWidth, 0);
            boundsPath.moveTo(0, iBitmapHeight);
            boundsPath.lineTo(iBitmapWidth, iBitmapHeight - 3);
            boundsPath.moveTo(0, iBitmapHeight / 2);
            boundsPath.lineTo(iBitmapWidth, iBitmapHeight / 2);
            canvas.drawPath(boundsPath, boundsPaint);

            Paint paintGraph = new Paint();
            paintGraph.setColor(Color.argb(150,255,0,0)); //red
            paintGraph.setStrokeWidth(10);
            paintGraph.setStyle(Paint.Style.STROKE);
            paintGraph.setFlags(Paint.ANTI_ALIAS_FLAG);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(20);
            canvas.drawText(String.valueOf(fMinValue), iBitmapWidth - 100, iBitmapHeight - 1, textPaint);
            if (fMaxValue != fMinValue) {
                canvas.drawText(String.valueOf(fMaxValue), iBitmapWidth - 100, 15, textPaint);
                canvas.drawText(String.valueOf((fMaxValue + fMinValue) / 2), iBitmapWidth - 100, iBitmapHeight / 2, textPaint);
            }
            float fSensValue;
            int iX = 0;
            Path graphPath = new Path();
            graphPath.moveTo(0, iBitmapHeight - ((float) sensorValuesList.get(0) - fMinValue) / fVerticalStepSizePixelMultiplyier);
            for (Object value : sensorValuesList) {
                fSensValue = (float) value;
                graphPath.lineTo(iX, iBitmapHeight - (fSensValue - fMinValue) / fVerticalStepSizePixelMultiplyier);
                iX += iStepSize;
            }
            canvas.drawPath(graphPath, paintGraph);
            return bitmap;
        }
    }
}

