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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * AppWidgetProvider used for SensGraph widget
 */

public class SensGraphWidgetProvider extends AppWidgetProvider {

    static final String TAG = "SensGraphWidgetProvider";
    static final String APP_NAME = "SensGraph";
    protected static AlarmManager alarmManager;
    SimpleDateFormat dateFormatFull, dateFormatHourMinute, dateFormatMonthDay, dateFormatDay;
//    widgetConfig structure:
//    0: (String) sensorNamePath
//    1: (String) sensorValuePath
//    2: (String) url
//    3: (Long) Update interval in minutes
//    4: (String[]) Sensor values List, to draw graph from
//    5: (String[]) widget update times
//    6: ---
//    7: ---
//    8: ---

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        //manually updating widget appWidgetIds consists only of single widget id
//        SharedPreferences sharedPreferences = context.getSharedPreferences(APP_NAME, 0);
        dateFormatFull = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", getDefaultLocale(context));
        dateFormatHourMinute = new SimpleDateFormat("HH:mm", getDefaultLocale(context));
        dateFormatMonthDay = new SimpleDateFormat("MM.dd", getDefaultLocale(context));
        dateFormatDay = new SimpleDateFormat("dd", getDefaultLocale(context));
        //dev+
        //set update datetime view
//        FileDb.deleteEntry(context, 0);
//        Date currSysTime = new Date();
//        currSysTime.setTime(System.currentTimeMillis());
//        String[] debugEntries = FileDb.getEntry(context, 0);
//        StringBuilder sb = new StringBuilder();
//        if (debugEntries == null) {
//            debugEntries = new String[7];
//            DevTools.stringArrToStringBuilder(sb, debugEntries);
//            debugEntries[0] = String.valueOf(0);
//            debugEntries[1] = "onUpdate appWidgetIds " + intArrToString(appWidgetIds) + "DT " + dateFormatFull.format(currSysTime);
//            debugEntries[2] = "";
//            debugEntries[3] = "";
//            debugEntries[4] = "";
//            debugEntries[5] = "";
//            debugEntries[6] = "";
//        } else {
//            String entries = debugEntries[1];
//                entries = entries + "+appWidgetIds " + intArrToString(appWidgetIds) + "DT " + dateFormatFull.format(currSysTime);
//            debugEntries[1] = entries;
//        }
//        FileDb.saveEntry(context, debugEntries);
        //dev-

        for (int widgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_view);
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
                UpdateViewFromJSON jsonTask = new UpdateViewFromJSON(context, remoteViews, widgetId,
                        appWidgetManager, widgetConfig[1], widgetConfig[2]);
                jsonTask.execute(widgetConfig[3]);

                long interval;
                if (!widgetConfig[4].equals("")) {
                    //updates when device is awake only
                    if (alarmManager == null) {
                        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                    }
                    if (!widgetConfig[4].equals("")) {
                        interval = 1000 * 60 * Long.parseLong(widgetConfig[4]) ; //minutes
                    } else {
                        interval = 1000 * 60 * 30; //default 30 minutes
                    }
                    alarmManager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + interval, updateWidgetPendingIntent);
                }
            }
        }
    }

//    protected static String stringArrToString(Object obj) {
//        StringBuilder sb = new StringBuilder();
//        String[] strArr = (String[]) obj;
//        sb.append("String[");
//        for (String str : strArr) {
//            sb.append(str);
//            sb.append(";");
//        }
//        sb.delete(sb.length() - 1, sb.length()); //removes last ARRAY_VALUE_SEPARATOR to look beautifully
//        sb.append("]");
//        return sb.toString();
//    }

//    private static String intArrToString(Object obj) {
//        StringBuilder sb = new StringBuilder();
//        int[] intArr = (int[]) obj;
//        sb.append("int[");
//        for (int i : intArr) {
//            sb.append(String.valueOf(i));
//            sb.append(";");
//        }
//        sb.delete(sb.length()-1, sb.length());
//        sb.append("]");
//        return sb.toString();
//    }

    /**
     * Called in response to the ACTION_APPWIDGET_OPTIONS_CHANGED broadcast when this widget has been layed out at a new size.
     */
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        if (alarmManager == null) {
            alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }
        for (int i : appWidgetIds) {
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

    /**
     * Parses String separated by "," to a float value ArrayList
     * @param values string to parse
     * @return ArrayList<Float> of values
     */
    @NonNull
    private ArrayList<Float> parseStringToFloatList(String values) {
        if (values.equals("[]")) {
            return new ArrayList<>();
        }

        values = values.substring(1,values.length()-1); //removes [] symbols
        String[] valuesSplitted = values.split(",");
        ArrayList<Float> resultList = new ArrayList<>();
        for (String str : valuesSplitted) {
            resultList.add(Float.parseFloat(str));
        }

        return resultList;
    }

    /**
     * Parses String separated by "," to ArrayList<Date>
     * @param values String to parse
     * @return ArrayList<Date>
     */
    @NonNull
    private ArrayList<Date> parseStringToDateList(String values) {

        if (values.equals("[]")) {
            return new ArrayList<>();
        }

        Date date;
        values = values.substring(1,values.length()-1); //removes [] symbols
        String[] valuesSplitted = values.split(",");
        ArrayList<Date> resultList = new ArrayList<>();
        try {
            for (String str : valuesSplitted) {
                date = dateFormatFull.parse(str);
                resultList.add(date);
            }
        } catch (ParseException e) {
            DevTools.logE(TAG, e.toString());
        }

        return resultList;
    }

    /**
     * AsyncTask class used to fetch JSON from url parse it and update widget as per widgetConfig
     */
    private class UpdateViewFromJSON extends AsyncTask<String, Integer, String> {
        private Context context;
        private RemoteViews remoteViews;
        private int widgetID;
        private AppWidgetManager widgetManager;
        private JSONWorker jWorker;
        private String sensorNamePath;
        private String sensorValuePath;
        private SimpleDateFormat dateFormat;

        UpdateViewFromJSON(Context context, RemoteViews views, int appWidgetID, AppWidgetManager appWidgetManager,
                                  String nameName, String valueName){
            this.context = context;
            this.remoteViews = views;
            this.widgetID = appWidgetID;
            this.widgetManager = appWidgetManager;
            this.jWorker = new JSONWorker();
            this.sensorNamePath = nameName;
            this.sensorValuePath = valueName;
            this.dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss", getDefaultLocale(context));
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

        /**
         * Updates widget if result ir not error
         * @param result String got from doInBackground function (http response, or error msg)
         */
        protected void onPostExecute(String result) {
//            result = "{\"Sensor_name1\":\"Outside_sensor\", \"Sensor1_value\":\"50.55\"}";  //dev
            String errStr = jWorker.parseJSONFromResponse(context, result);
            String sSensorValue;
            DevTools.log(TAG, "onPostExecute result", result, errStr);
            if (errStr.equals("")) { //no error
                jWorker.mainJsonObjFromString(result);
                remoteViews.setTextViewText(R.id.sensorNameTextView,
                        jWorker.getValueFromPath(sensorNamePath));
                sSensorValue = jWorker.getValueFromPath(sensorValuePath);
                remoteViews.setTextViewText(R.id.sensorValueTextView,
                        sSensorValue);

                //set update datetime view
                Date currSysTime = new Date();
                currSysTime.setTime(System.currentTimeMillis());
                remoteViews.setTextViewText(R.id.sensorUpdatedDtTextView, dateFormat.format(currSysTime));

                try {
                    float fSensorValue = Float.parseFloat(sSensorValue);
                    int iMaxValuesAmount = 50;

                    String[] widgetConfig = FileDb.getEntry(context, widgetID);
                    ArrayList<Float> widgetValuesList;
                    ArrayList<Date> widgetUpdateTimesList;
                    if (widgetConfig != null) {
                        widgetValuesList = parseStringToFloatList(widgetConfig[5]);
                        widgetUpdateTimesList = parseStringToDateList(widgetConfig[6]);
                        if (widgetValuesList.size() == iMaxValuesAmount) {
                            widgetValuesList.remove(0);
                            widgetUpdateTimesList.remove(0);
                        }
                        widgetValuesList.add(fSensorValue);
                        widgetUpdateTimesList.add(currSysTime);

                        Bitmap bitmap = drawGraphBitmap(widgetValuesList, widgetUpdateTimesList);
                        if (bitmap != null) {
                            remoteViews.setImageViewBitmap(R.id.graphImageView, bitmap);
                        }

                        //update widgetConfig
                        //values list
                        Object[] newValues = widgetValuesList.toArray();
                        StringBuilder sb = new StringBuilder();
                        sb.append("[");
                        for (Object obj : newValues) {
                            sb.append(String.valueOf(obj));
                            sb.append(",");
                        }
                        sb.delete(sb.length()-1, sb.length());
                        sb.append("]");
                        widgetConfig[5] = sb.toString();

                        //update times list
                        newValues = widgetUpdateTimesList.toArray();
                        sb = new StringBuilder();
                        sb.append("[");
                        for (Object obj : newValues) {
                            sb.append(dateFormat.format(obj));
                            sb.append(",");
                        }
                        sb.delete(sb.length()-1, sb.length());
                        sb.append("]");
                        widgetConfig[6] = sb.toString();
                        //save config
                        FileDb.saveEntry(context, widgetConfig);//
                    }
                } catch (Exception e) {
                    DevTools.logE(TAG, e);
                }
            } else { //error
                remoteViews.setTextViewText(R.id.sensorNameTextView,
                        context.getString(R.string.just_error) + " " + errStr);
                remoteViews.setTextViewText(R.id.sensorValueTextView, "");
            }
            widgetManager.updateAppWidget(widgetID, remoteViews);
        }

        /**
         * Draws widget graph bitmap
         * @param widgetValuesList widget values list
         * @param widgetUpdateTimesList widget update times list
         * @return drawn Bitmap if drawing was successful
         */
        private Bitmap drawGraphBitmap(ArrayList widgetValuesList, ArrayList widgetUpdateTimesList) {

            if ((widgetValuesList.size() < 2) || (widgetUpdateTimesList.size() < 2)) {
                return null;
            }

            float fSensorValue;
            int iBitmapWidth = 500;
            int iBitmapHeight = 300;
            float fValueRange;
            float fVerticalStepSizePixelMultiplyier;
            float fMaxValue = Float.MIN_VALUE;
            float fMinValue = Float.MAX_VALUE;
            float graphTextSize = 25;

            //finding min max values
            for (Object value : widgetValuesList) {
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
            Bitmap bitmap = Bitmap.createBitmap(iBitmapWidth, iBitmapHeight, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            //Paints
            Paint boundsPaint = new Paint();
            boundsPaint.setColor(Color.WHITE);
            boundsPaint.setStrokeWidth(5);
            boundsPaint.setStyle(Paint.Style.STROKE);
            boundsPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
            boundsPaint.setAlpha(90);

            Paint paintGraph = new Paint();
            paintGraph.setColor(Color.argb(150,255,0,0)); //red
            paintGraph.setStrokeWidth(10);
            paintGraph.setStyle(Paint.Style.STROKE);
            paintGraph.setFlags(Paint.ANTI_ALIAS_FLAG);

            Paint textPaint = new Paint();
            textPaint.setColor(Color.WHITE);
            textPaint.setTextSize(25);
            textPaint.setAlpha(150);

            //drawing
            //bitmap step size (line length) in pixels, here -1 because one last point has no line
            int iStepSize = Math.round(iBitmapWidth / (widgetValuesList.size() - 1));

            //graph
            float fSensValue;
            int iX = 0;
            Path graphPath = new Path();
            graphPath.moveTo(0, iBitmapHeight - ((float) widgetValuesList.get(0) - fMinValue) / fVerticalStepSizePixelMultiplyier);
            for (Object value : widgetValuesList) {
                fSensValue = (float) value;
                graphPath.lineTo(iX, iBitmapHeight - (fSensValue - fMinValue) / fVerticalStepSizePixelMultiplyier);
                iX += iStepSize;
            }
            canvas.drawPath(graphPath, paintGraph);
            //value lines
            Path boundsPath = new Path();
            boundsPath.moveTo(0, 3);
            boundsPath.lineTo(iBitmapWidth, 0);
            boundsPath.moveTo(0, iBitmapHeight);
            boundsPath.lineTo(iBitmapWidth, iBitmapHeight - 3);
            boundsPath.moveTo(0, iBitmapHeight / 2);
            boundsPath.lineTo(iBitmapWidth, iBitmapHeight / 2);

            //time lines
            //last value
            boundsPath.moveTo(3, 0);
            boundsPath.lineTo(3, iBitmapHeight);
            Date firstUpdate = (Date) widgetUpdateTimesList.get(0);
            Date lastUpdate = (Date) widgetUpdateTimesList.get(widgetUpdateTimesList.size()-1);
            Date midUpdate;
            canvas.drawText(dateFormatHourMinute.format(firstUpdate), 8, iBitmapHeight / 2 + graphTextSize, textPaint);
            if (!dateFormatDay.format(lastUpdate).equals(dateFormatDay.format(firstUpdate))) {
                canvas.drawText(dateFormatMonthDay.format(firstUpdate), 8, iBitmapHeight / 2 + graphTextSize * 2, textPaint);
            }

            //middle value
            if (widgetUpdateTimesList.size() > 2) {
                int midPosition = (int) (Math.round(widgetUpdateTimesList.size() / 2.0));
                midPosition -= 1; //here -1 because List index first element is 0, not 1
                float midPositionPixels = (float) (iStepSize * midPosition);
                boundsPath.moveTo(midPositionPixels, 0);
                boundsPath.lineTo(midPositionPixels, iBitmapHeight);
                midUpdate = (Date) widgetUpdateTimesList.get(midPosition);
                canvas.drawText(dateFormatHourMinute.format(midUpdate), midPositionPixels + 5,
                        iBitmapHeight / 2 + graphTextSize, textPaint);
                if (!dateFormatDay.format(lastUpdate).equals(dateFormatDay.format(midUpdate))) {
                    canvas.drawText(dateFormatMonthDay.format(midUpdate), midPositionPixels + 5,
                            iBitmapHeight / 2 + graphTextSize * 2, textPaint);
                }
            }
            if (widgetUpdateTimesList.size() > 5) {

                //start-middle value
                int startMidPosition = (int) (Math.round(widgetUpdateTimesList.size() / 4.0));
                startMidPosition -= 1; //here -1 because List index first element is 0, not 1
                float startMidPositionPixels = (float) (iStepSize * startMidPosition);
                boundsPath.moveTo(startMidPositionPixels, 0);
                boundsPath.lineTo(startMidPositionPixels, iBitmapHeight);
                midUpdate = (Date) widgetUpdateTimesList.get(startMidPosition);
                canvas.drawText(dateFormatHourMinute.format(midUpdate), startMidPositionPixels + 5,
                        iBitmapHeight / 2 + graphTextSize, textPaint);
                if (!dateFormatDay.format(lastUpdate).equals(dateFormatDay.format(midUpdate))) {
                    canvas.drawText(dateFormatMonthDay.format(midUpdate), startMidPositionPixels + 5,
                            iBitmapHeight / 2 + graphTextSize * 2, textPaint);
                }

                //end-middle value
                int endMidPosition = (int) (Math.round(widgetUpdateTimesList.size() * 3 / 4.0));
                endMidPosition -= 1; //here -1 because List index first element is 0, not 1
                float endMidPositionPixels = (float) (iStepSize * endMidPosition);
                boundsPath.moveTo(endMidPositionPixels, 0);
                boundsPath.lineTo(endMidPositionPixels, iBitmapHeight);
                midUpdate = (Date) widgetUpdateTimesList.get(endMidPosition);
                canvas.drawText(dateFormatHourMinute.format(midUpdate), endMidPositionPixels + 5,
                        iBitmapHeight / 2 + graphTextSize, textPaint);
                if (!dateFormatDay.format(lastUpdate).equals(dateFormatDay.format(midUpdate))) {
                    canvas.drawText(dateFormatMonthDay.format(midUpdate), endMidPositionPixels + 5,
                            iBitmapHeight / 2 + graphTextSize * 2, textPaint);
                }
            }

            //line drawing
            canvas.drawPath(boundsPath, boundsPaint);

            //min, mid, max values of the graph
            double drawingRoundedValue;
            drawingRoundedValue = Math.round(fMinValue * 1000.0) / 1000.0;
            canvas.drawText(String.valueOf(drawingRoundedValue), iBitmapWidth - calculateOffset(drawingRoundedValue), iBitmapHeight - 1, textPaint);
            if (fMaxValue != fMinValue) {
                drawingRoundedValue = Math.round(fMaxValue * 1000.0) / 1000.0;
                canvas.drawText(String.valueOf(drawingRoundedValue),
                        iBitmapWidth - calculateOffset(drawingRoundedValue), 20, textPaint);
                drawingRoundedValue = Math.round(((fMaxValue + fMinValue) / 2) * 1000.0) / 1000.0;
                canvas.drawText(String.valueOf(drawingRoundedValue),
                        iBitmapWidth - calculateOffset(drawingRoundedValue),
                        iBitmapHeight / 2, textPaint);
            }

            return bitmap;
        }

        /**
         * Calculates offset from right widget side to show min/mid/max graph values nicely
         * @param number number calculate format
         * @return calculated width
         */
        private int calculateOffset(double number) {
            int singleNumberWidth = 14; //width in pixels
            return String.valueOf(number).length() * singleNumberWidth;
        }

//        /**
//         * Converts double time to human readable format
//         * @param time double to format
//         * @return time converted to String
//         */
//        private String convertDoubleTimeToString(double time) {
//            if (time < 0) {
//                return "";
//            }
//            String result;
//            double sMul = 1000;
//            double mMul = sMul * 60;
//            double hMul = mMul * 60;
//            double dMul = hMul * 24;
//            double moMul = dMul * 30;
//            if (time < sMul) { //ms
//                return String.valueOf((int) time) + "ms";
//            } else if (time < mMul) { //s
//                return divideAndFormat(time, sMul) + "s";
//            } else if (time < hMul) { //min
//                return divideAndFormat(time, mMul) + "min";
//            } else if (time < dMul) { //hr
//                result = divideAndFormat(time, hMul) +
//                        context.getResources().getString(R.string.hours_short); //lt val
//                return result;
//            } else if (time < moMul) { //day
//                result = divideAndFormat(time, dMul) + "d";
//                return result;
//            } else {
//                return "";
//            }
//        }

//        /**
//         * Converts double time number of whole days
//         * @param time double to format
//         * @return calculated whole days plus "day" abbreviation
//         */
//        private String generateDayDifferenceText(double time) {
//
//            String result = "";
//            double dayMul = 1000 * 60 * 60 * 24;
////            if (time < dayMul) {
////                return result;
////            }
//            result = "-" + divideAndFormat(time, dayMul) + "d";
//            return result;
//        }
//
//        /**
//         * Helper function to divide time by time unit, convert to int and the to String
//         * @param time time to format
//         * @param divisor time unit
//         * @return division result if divisor is not 0
//         */
//        private String divideAndFormat(double time, double divisor) {
//            if (divisor == 0) {
//                return "";
//            }
//            return String.valueOf((int) (time / divisor));
//        }
    }
}

