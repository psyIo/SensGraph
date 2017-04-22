package org.psylo.sensgraph;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;
import java.util.Set;


public class SensGraphConfigure extends AppCompatActivity {

    static final String TAG = "SensGraphConfAct";
    static final String APP_NAME = "SensGraph";
    static final String USED_URLS_AUTOCOMPLETE_SHARED_PREF = "USED_URLS";
    static DevTools dt = new DevTools(); //dev
    protected String sensorName;  //sensor name from JSON
    protected String sensorValue; //sensor value from JSON
    protected String url; //url to get response from
    protected Long updateInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        int mAppWidgetId = 0;
        super.onCreate(savedInstanceState);
        final Resources res = getResources();
//        Intent intent = getIntent();
//        Bundle extras = intent.getExtras();
//        if (extras != null) {
//            mAppWidgetId = extras.getInt(
//                    AppWidgetManager.EXTRA_APPWIDGET_ID,
//                    AppWidgetManager.INVALID_APPWIDGET_ID);
//        }
        setContentView(R.layout.activity_sensgraph_configure);

        final AutoCompleteTextView urlAutoCompleteTv = (AutoCompleteTextView) findViewById(R.id.nameUrlValueEdit);
        SharedPreferences settings = getSharedPreferences(APP_NAME, 0);// + "_" + String.valueOf(mAppWidgetId), 0);
        Set<String> usedUrls = new ArraySet<>();
        usedUrls = settings.getStringSet(USED_URLS_AUTOCOMPLETE_SHARED_PREF, usedUrls);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.single_text_view, usedUrls.toArray(new String[0]));
        urlAutoCompleteTv.setAdapter(adapter);
        urlAutoCompleteTv.setOnClickListener(new TextView.OnClickListener(){
            @Override
            public void onClick(View v) {
                if (urlAutoCompleteTv.getText().toString().equals(
                        res.getString(R.string.sensor_url_text_place_holder))) {
                    urlAutoCompleteTv.setText("");
                }
                dt.logV("urlAutoCompleteTv clicked onClick");
            }
        });
        urlAutoCompleteTv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //if action is DONE then updates list and hides softKeyboard
                //if action is NEXT then replaces empty url string to a default one
                boolean handled = false;
                dt.logV("onEditorAction actionId", actionId);
                switch (actionId) {

                    case EditorInfo.IME_ACTION_DONE:
                        View view = getCurrentFocus();
                        if (view != null) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                            getResponseAndUpdateNameList();
                        }
                        handled = true;
                        break;
                    case EditorInfo.IME_ACTION_NEXT:
                        if (v.getText().toString().equals("")) {
                            v.setText(res.getString(R.string.sensor_url_text_place_holder));
                        }

                        break;
                }
                return handled;
            }
        });

        EditText updateIntervalEt = (EditText) findViewById(R.id.updateIntervalEditText);
        updateIntervalEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //if action is DONE then updates list and hides softKeyboard
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    View view = getCurrentFocus();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        getResponseAndUpdateNameList();
                    }
                    handled = true;
                }
                return handled;
            }
        });

        //dev+
//        TextView urlField = (TextView) findViewById(R.id.nameUrlValueEdit);
//        urlField.setText("http://api.thingspeak.com/channels/99791/feeds.json?results=1");
//        urlField.setText("http://46.251.48.58:6969/get_json/");
//        urlField.setText("http://46.251.48.58:6969/get_json_saved/");
        //dev-

        TextView refreshBtn = (TextView) findViewById(R.id.refreshBtnTv);
        refreshBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getResponseAndUpdateNameList();
            }
        });

        //save settings button
        final Button saveButton = (Button) findViewById(R.id.save_settings_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int mAppWidgetId = 0;
                Boolean hasErrors = false;
                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                StringBuilder infoSb = new StringBuilder();
                if (extras != null) {
                    mAppWidgetId = extras.getInt(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                }
                Log.v(TAG, "mAppWidgetId: " + Integer.toString(mAppWidgetId));

                final TextView tvName = (TextView) findViewById(R.id.nameValueTv);
                if (tvName.getText() == res.getString(R.string.sensor_name_text_place_holder)) {
                    addInfoString(infoSb, "Sensor Name not selected");
                    hasErrors = true;
                } else {
                    sensorName = tvName.getText().toString();
                }
                final TextView tvValue = (TextView) findViewById(R.id.valueValueTv);
                if (tvValue.getText() == res.getString(R.string.sensor_value_text_place_holder)) {
                    addInfoString(infoSb, "Sensor Value not selected");
                    hasErrors = true;
                } else {
                    sensorValue = tvValue.getText().toString();
                }

                final EditText edUpdateInterval = (EditText) findViewById(R.id.updateIntervalEditText);
                if (edUpdateInterval.getText().toString().equals("0") || edUpdateInterval.getText().toString().equals("")) {
                    addInfoString(infoSb, "Update interval can not be 0 or empty");
                    hasErrors = true;
                } else {
                    try {
                        updateInterval = Long.parseLong(edUpdateInterval.getText().toString());
                    } catch (Exception e) {
                        hasErrors = true;
                        addInfoString(infoSb, "Error parsing update interval");
                        dt.logE(TAG, e);
                    }
                }

                //if there are no errors finishes activity as system expects and updates widget view
                if (!hasErrors) {

                    SharedPreferences settings = getSharedPreferences(APP_NAME, 0);// + "_" + String.valueOf(mAppWidgetId), 0);
                    Set<String> usedUrls = new ArraySet<>();
                    usedUrls = settings.getStringSet(USED_URLS_AUTOCOMPLETE_SHARED_PREF, usedUrls);

                    //dev+ clear SharredPreferences
//                    SharedPreferences.Editor ed = settings.edit();
//                    usedUrls.add(url);
//                    ed.remove(USED_URLS_AUTOCOMPLETE_SHARED_PREF);
//                    ed.commit();
                    //dev-

                    if (!usedUrls.contains(url)) {
                        SharedPreferences.Editor editor = settings.edit();
                        usedUrls.add(url);
                        editor.remove(USED_URLS_AUTOCOMPLETE_SHARED_PREF);
                        editor.commit(); //apply() does not work correctly here
                        editor.putStringSet(USED_URLS_AUTOCOMPLETE_SHARED_PREF, usedUrls);
                        editor.apply();

                        //dev+
//                        usedUrls = settings.getStringSet(USED_URLS_AUTOCOMPLETE_SHARED_PREF, usedUrls);
//                        dt.logV("usedUrls.size()", usedUrls.size());
//                        for (String s : usedUrls) {
//                            dt.logV("usedUrls", s);
//                        }
                        //dev-
                    }

                    //save settings
                    SimpleDb sdb = SensGraphWidgetProvider.settings;
                    if (sdb.createEntry(mAppWidgetId)) {
                        sdb.setField(0,sensorName);
                        sdb.setField(1,sensorValue);
                        sdb.setField(2,url);
                        //position 3 is for widget pendingIntent
                        sdb.setField(4,updateInterval);
//                        sdb.setField(5, getDefaultLocale());
                        dt.logV("save settings updateInterval", updateInterval, "sensorName", sensorName,
                            "sensorValue", sensorValue, "url", url);
                    }

                    //manual first widget update using SensGraphWidgetProvider class
                    SensGraphWidgetProvider sensgraphWidgetProvider = new SensGraphWidgetProvider();
                    sensgraphWidgetProvider.onUpdate(v.getContext(),
                        AppWidgetManager.getInstance(v.getContext()),
                        new int[] { mAppWidgetId }
                    );

                    //finnish activity
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                    resultIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    setResult(RESULT_OK, resultIntent);
                    finish();
                } else {
                    showInfoDialog(v, infoSb.toString());
                }
            }

            public void addInfoString(StringBuilder sb, String str) {
                if (sb.length() > 0) {
                    sb.append("\n");
                }
                sb.append(str);
            }
        });
    }

    protected void getResponseAndUpdateNameList() {
        final ListView namesListView = (ListView) findViewById(R.id.namesList);
        final DisplayJSONNames jsonTask = new DisplayJSONNames(namesListView, this);
        final TextView urlField = (TextView) findViewById(R.id.nameUrlValueEdit);
        url = urlField.getText().toString();
        //clears last "/" char from the url, to ensure that there is no duplicate URLs saved in
        //SharedPreferences with "/" and without "/" at the end
        if (url.substring(url.length()-1).equals("/")) {
            url = url.substring(0, url.length()-1);
            urlField.setText(url);
        }
        jsonTask.execute(url);
    }

    protected void showInfoDialog(View v, String msg) {
        new AlertDialog.Builder(v.getContext())
            .setIcon(android.R.drawable.ic_dialog_info)
            .setTitle("Information")
            .setMessage(msg)
            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }

                })
//            .setNegativeButton("No", null)
            .show();
    }

//    @SuppressWarnings("deprecation")
//    protected Locale getDefaultLocale() {
//        Locale locale;
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) { // 24 and newer (Nougat 7)
//            locale = getResources().getConfiguration().getLocales().get(0);
//        } else { // 23 and older
//            locale = getResources().getConfiguration().locale;
//        }
//        return locale;
//    }

    private class DisplayJSONNames extends AsyncTask<String, Integer, String> {
        private ListView namesListView;
        private JSONWorker jWorker;
        private Activity context;

        public DisplayJSONNames(ListView view, Activity context){
            this.namesListView = view;
            this.jWorker = new JSONWorker();
            this.context = context;
        }

        /**
         * Returns JSON string from HttpResponse
         * Uses just first element of strings param
         */
        protected String doInBackground(String... strings) {
            String response = "";
            if (strings.length > 0) {
                JSONWorker jWorker = new JSONWorker();
                response = jWorker.GetHttpResponse(strings[0]);
            }
            return response;
        }

        protected void onProgressUpdate(Integer... progress) {
//            setProgressPercent(progress[0]);
        }

        /**
         * Parses response, if response is error, user info msg will be shown
         * */
        protected void onPostExecute(String result) {
            String errStr = jWorker.parseJSONFromResponse(context, result);
            if (!errStr.equals("")) { //error
                final Resources res = getResources();
                final TextView tvName = (TextView) findViewById(R.id.nameValueTv);
                tvName.setText(res.getString(R.string.sensor_name_text_place_holder));
                final TextView tvValue = (TextView) findViewById(R.id.valueValueTv);
                tvValue.setText(res.getString(R.string.sensor_value_text_place_holder));
                sensorName = null;
                sensorValue = null;

                //clears JSON names list (sets an empty list)
                CustomList cl = new CustomList(context, new String[0], new String[0]);
                namesListView.setAdapter(cl);

                //user info
                View v = findViewById(R.id.activity_sens_graph_configure);
                showInfoDialog(v, errStr);
            } else { //no error
                jWorker.makeNamesValuesLists();
                CustomList cl = new CustomList(context, jWorker.namesList.toArray(new String[0]),
                        jWorker.valuesList.toArray(new String[0]));
                namesListView.setAdapter(cl);

                namesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    /**
                     * First clicked element sets sensorNamePath
                     * Second - sets sensorValuePath
                     * Click on already clicked element clears selection
                     */
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        String nameClicked;
                        ExpandedTextView etv = (ExpandedTextView) v.findViewById(R.id.namesListTextView);

                        nameClicked = etv.getText().toString();
                        dt.logV("sensorName", sensorName, "sensorValue", sensorValue);
                        if ((sensorName == null) && (!sensorValue.equals(nameClicked))) {
                            sensorName = nameClicked;
                            TextView tv = (TextView) findViewById(R.id.nameValueTv);
                            tv.setText(sensorName);
                        } else if ((sensorValue == null) && (!sensorName.equals(nameClicked))) {
                            sensorValue = nameClicked;
                            TextView tv = (TextView) findViewById(R.id.valueValueTv);
                            tv.setText(sensorValue);
                        } else {
                            //resets selections
                            if (nameClicked.equals(sensorName)) {
                                sensorName = null;
                                TextView tv = (TextView) findViewById(R.id.nameValueTv);
                                tv.setText(R.string.sensor_name_text_place_holder);
                            }
                            if (nameClicked.equals(sensorValue)) {
                                sensorValue = null;
                                TextView tv2 = (TextView) findViewById(R.id.valueValueTv);
                                tv2.setText(R.string.sensor_value_text_place_holder);
                            }
                        }
                    }
                });
            }
        }
    }
}
