package org.psylo.sensgraph;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.util.ArraySet;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.support.v7.view.menu.ActionMenuItemView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Set;


public class SensGraphConfigure extends AppCompatActivity {

    static final String TAG = "SensGraphConfAct";
    static final String APP_NAME = "SensGraph";
    static final String USED_URLS_AUTOCOMPLETE_SHARED_PREF = "USED_URLS";
    static final DevTools dt = new DevTools(); //dev
    protected String sensorName;  //sensor name from JSON
    protected String sensorValue; //sensor value from JSON
    protected String url; //url to get response from
    protected Long updateInterval;
    private Resources res;
    private Boolean bConfigOkToSaveState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();
        setContentView(R.layout.activity_sensgraph_configure);
        bConfigOkToSaveState = false;

        //sets saved used URLs saved list for autocompletion
        final AutoCompleteTextView urlAutoCompleteTv = findViewById(R.id.nameUrlValueEdit);
//        final AutoCompleteTextView urlAutoCompleteTv = (AutoCompleteTextView) findViewById(R.id.nameUrlValueEdit);
        SharedPreferences sharedPrefs = getSharedPreferences(APP_NAME, 0);
        Set<String> usedUrls = new ArraySet<>();
        usedUrls = sharedPrefs.getStringSet(USED_URLS_AUTOCOMPLETE_SHARED_PREF, usedUrls);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                R.layout.single_text_view, usedUrls.toArray(new String[0]));
//                R.layout.single_text_view_for_debug_list, usedUrls.toArray(new String[0]));
        urlAutoCompleteTv.setAdapter(adapter);

        //softInput handling
        urlAutoCompleteTv.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //if action is DONE or NEXT then updates list and hides softKeyboard
                boolean handled = false;
                switch (actionId) {

                    case EditorInfo.IME_ACTION_DONE:
                        hideSoftInputAndUpdateList();
                        handled = true;
                        break;
                    case EditorInfo.IME_ACTION_NEXT:
                        hideSoftInputAndUpdateList();
                        handled = true;
                        break;
                }
                return handled;
            }
        });

        //selects all text for the first time
        urlAutoCompleteTv.selectAll();

        //update interval EditText
        EditText updateIntervalEt = findViewById(R.id.updateIntervalEditText);
//        EditText updateIntervalEt = (EditText) findViewById(R.id.updateIntervalEditText);
        updateIntervalEt.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //if action is DONE or NEXT then updates list and hides softKeyboard
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    hideSoftInputAndUpdateList();
                    hideSoftInput();
                    configHasErrors(v, false);
                    handled = true;
                }
                if (actionId == EditorInfo.IME_ACTION_NEXT) { //next is nameList which is focused and softKeyboard is shown, and we do not want that
//                    hideSoftInputAndUpdateList();
                    hideSoftInput();
                    configHasErrors(v, false);
                    handled = true;
                }
                return handled;
            }
        });

        updateIntervalEt.setOnFocusChangeListener(new View.OnFocusChangeListener(){
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) { //true when other element clicked :/
                    if (urlAutoCompleteTv.getText().toString().equals("")) {
                        urlAutoCompleteTv.setText(res.getString(R.string.sensor_url_text_place_holder));
                    }
                } else {
                    if (urlAutoCompleteTv.getText().toString().equals(res.getString(R.string.sensor_url_text_place_holder))) {
                        urlAutoCompleteTv.setText("");
                    }
                }
            }
        });

        //dev+
//        TextView urlField = findViewById(R.id.nameUrlValueEdit);
//        urlField.setText("http://api.thingspeak.com/channels/99791/feeds.json?results=1");
//        urlField.setText("http://46.251.48.58:6969/get_saved_json/");
//        urlField.setText("http://46.251.48.58:6969/get_rand_json/");
//        SharedPreferences settings = getSharedPreferences(APP_NAME, 0);// + "_" + String.valueOf(mAppWidgetId), 0);
//        Set<String> usedUrls = new ArraySet<>();
//        usedUrls = settings.getStringSet(USED_URLS_AUTOCOMPLETE_SHARED_PREF, usedUrls);

        //dev+ clear SharredPreferences
//                    SharedPreferences.Editor ed = settings.edit();
//                    usedUrls.add(url);
//                    ed.remove(USED_URLS_AUTOCOMPLETE_SHARED_PREF);
//                    ed.commit();
        //dev-

//        if (!usedUrls.contains(url)) {
//            SharedPreferences.Editor editor = settings.edit();
//            usedUrls.add(url);
//            editor.remove(USED_URLS_AUTOCOMPLETE_SHARED_PREF);
//            editor.commit(); //apply() does not work correctly here
//            editor.putStringSet(USED_URLS_AUTOCOMPLETE_SHARED_PREF, usedUrls);
//            editor.apply();
//        }
        //dev-

        final TextView sensorNameTv = findViewById(R.id.sensorNameTv);
        sensorNameTv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetName();
            }
        });

        final TextView sensorValueTv = findViewById(R.id.sensorValueTv);
        sensorValueTv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetValue();
            }
        });

        final Button buttonClearUrl = findViewById(R.id.clearUrlBtn);
        buttonClearUrl.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                urlAutoCompleteTv.setText("");
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.config_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                getResponseAndUpdateNameList();
                break;
            case R.id.action_save:
                saveAndFinnish(findViewById(R.id.activity_sens_graph_configure));
                break;
            case R.id.action_help:
                showHelp(findViewById(R.id.activity_sens_graph_configure));
                break;
            default:
                break;
        }
        return true;
    }

    private void resetName() {
        final TextView sensorNameTv = findViewById(R.id.sensorNameTv);
        sensorNameTv.setText(res.getString(R.string.sensor_name_text_place_holder));
        sensorNameTv.setTextColor(getColorVersionSafe(R.color.redLight));
        sensorName = null;
        setSaveBtnState(false);
    }

    private void resetValue() {
        final TextView sensorValueTv = findViewById(R.id.sensorValueTv);
        sensorValueTv.setText(res.getString(R.string.sensor_value_text_place_holder));
        sensorValueTv.setTextColor(getColorVersionSafe(R.color.redLight));
        sensorValue = null;
        setSaveBtnState(false);
    }

    private void setSaveBtnState(Boolean stateOkToSave) {
        if (bConfigOkToSaveState != stateOkToSave) {
            ActionMenuItemView mi = findViewById(R.id.action_save);
            if (stateOkToSave) {
                //here ok, gradle has lintOptions {disable 'RestrictedApi'} option added
                mi.setIcon(getDrawableVersionSafe(R.drawable.save_btn_ok_60));
            } else {
                mi.setIcon(getDrawableVersionSafe(R.drawable.save_btn_60));
            }
            dt.logV("setSaveBtnState", "stateOkToSave", stateOkToSave);
            bConfigOkToSaveState = stateOkToSave;
        }
    }

    private Boolean configHasErrors(View v, Boolean showDialog) {
        Boolean bHasErrors = false;
        StringBuilder infoSb = new StringBuilder();

        final TextView tvName = findViewById(R.id.sensorNameTv);
        if (tvName.getText() == res.getString(R.string.sensor_name_text_place_holder)) {
            addInfoString(infoSb, getString(R.string.sensor_name_text_place_holder));
            bHasErrors = true;
        } else {
            sensorName = tvName.getText().toString();
            sensorName = sensorName.substring(res.getString(R.string.sensor_name_text).length() + 1);
        }
        final TextView tvValue = findViewById(R.id.sensorValueTv);
        if (tvValue.getText() == res.getString(R.string.sensor_value_text_place_holder)) {
            addInfoString(infoSb, getString(R.string.sensor_value_text_place_holder));
            bHasErrors = true;
        } else {
            sensorValue = tvValue.getText().toString();
            sensorValue = sensorValue.substring(res.getString(R.string.sensor_value_text).length() + 1);
        }

        final EditText etUpdateInterval = findViewById(R.id.updateIntervalEditText);
        if (etUpdateInterval.getText().toString().equals("0") || etUpdateInterval.getText().toString().equals("")) {
            addInfoString(infoSb, getString(R.string.config_activity_error_msg_1));
            bHasErrors = true;
        } else {
            try {
                updateInterval = Long.parseLong(etUpdateInterval.getText().toString());
            } catch (Exception e) {
                bHasErrors = true;
                addInfoString(infoSb, getString(R.string.config_activity_error_msg_2));
                DevTools.logE(TAG, e);
            }
        }
        setSaveBtnState(!bHasErrors);
        if (bHasErrors && showDialog) {
            showInfoDialog(v, infoSb.toString());
        }
        return bHasErrors;
    }

    protected void getResponseAndUpdateNameList() {
        final ListView namesListView = findViewById(R.id.namesList);
        final DisplayJSONNames jsonTask = new DisplayJSONNames(namesListView, this);
        final TextView urlField = findViewById(R.id.nameUrlValueEdit);

        //clears JSON names list (sets an empty list)
        resetName();
        resetValue();
        TwoTvArrayAdapter twoTvArrayAdapter = new TwoTvArrayAdapter(this,
                new String[0], new String[0]);
        namesListView.setAdapter(twoTvArrayAdapter);

        //shows infinity loader animation drawable
        AnimationDrawable infinityLoaderAnimation;
        ImageView infinityLoaderView = findViewById(R.id.infinityLoaderView);
        infinityLoaderView.setImageDrawable(getDrawableVersionSafe(R.drawable.infinity_loader));
        infinityLoaderAnimation = (AnimationDrawable) infinityLoaderView.getDrawable();
        infinityLoaderAnimation.start();

        url = urlField.getText().toString();
        //clears last "/" chars from the url, to ensure that there is no duplicate URLs saved in
        //SharedPreferences with "/" and without "/" at the end
        if (url.length() > 0) {
            for (int i = 0; i < url.length();i++) {
                if (url.substring(url.length() - 1).equals("/")) {
                    url = url.substring(0, url.length() - 1);
                } else {
                    break;
                }
            }
        }
        hideSoftInput();
        jsonTask.execute(url);
    }

    /**
     * Saves configuration and finishes activity
     * */
    protected void saveAndFinnish(View v) {
        int mAppWidgetId = 0;
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        //if there are no errors finishes activity as system expects and updates widget view
        if (!configHasErrors(v, true)) {
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
            }

            //save settings
            //using file
//            FileDb.deleteDbFile(v.getContext());
            FileDb.saveEntry(v.getContext(),
                    String.valueOf(mAppWidgetId),
                    sensorName,
                    sensorValue,
                    url,
                    String.valueOf(updateInterval),
                    "[]", "[]");

            DevTools.log(TAG, "FileDb.readDbFile", FileDb.readDbFile(v.getContext()));
//            FileDb.readDbFile(v.getContext());
//            DevTools.log(TAG, FileDb.getEntry(v.getContext(), mAppWidgetId));

//            SimpleDb sdb = SensGraphWidgetProvider.settings;
//            if (sdb.createEntry(mAppWidgetId)) {
//                sdb.setField(0, sensorName);
//                sdb.setField(1, sensorValue);
//                sdb.setField(2, url);
//                position 3 is for widget pendingIntent
//                sdb.setField(4, updateInterval);
//                sdb.setField(5, new ArrayList()); //used to save values for graph
//                sdb.setField(6, new ArrayList()); //used to save values for debugger
//                dt.logV(TAG, "save settings updateInterval", updateInterval, "sensorName", sensorName,
//                        "sensorValue", sensorValue, "url", url);
//            }

            //manual first widget update using SensGraphWidgetProvider class
            SensGraphWidgetProvider sensgraphWidgetProvider = new SensGraphWidgetProvider();
            sensgraphWidgetProvider.onUpdate(v.getContext(),
                    AppWidgetManager.getInstance(v.getContext()),
                    new int[] { mAppWidgetId }
            );
            DevTools.log(TAG, "sensgraphWidgetProvider", sensgraphWidgetProvider);

            //finnish activity
            Intent resultIntent = new Intent();
            resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            resultIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    public void addInfoString(StringBuilder sb, String str) {
        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append(str);
    }

    /**
     * Shows help dialog
     * */
    protected void showHelp(View v) {
        StringBuilder sb = new StringBuilder();
        sb.append(getString(R.string.help_info_text_1));
        sb.append("\n");
        sb.append(getString(R.string.help_info_text_2));
        sb.append("\n");
        sb.append(getString(R.string.help_info_text_3));
        sb.append("\n");
        sb.append(getString(R.string.help_info_text_4));
        sb.append("\n");
        sb.append(getString(R.string.help_info_text_5));
        sb.append("\n");
        sb.append(getString(R.string.help_info_text_6));
        showInfoDialog(v, sb.toString());
    }

    /**
     * Executes action DONE:
     *  - Hides softInput (keyboard)
     *  - Updates JSON elements list from Http response
     */
    protected void hideSoftInputAndUpdateList() {
        hideSoftInput();
        getResponseAndUpdateNameList();
    }

    /**
     * Hides soft input
     */
    private void hideSoftInput() {
        View view = getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    protected void showInfoDialog(View v, String msg) {
        try {
            new AlertDialog.Builder(v.getContext())
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setTitle("   " + APP_NAME + " Info")
                    .setMessage(msg)
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }

                    })
//            .setNegativeButton("No", null)
                    .show();
        } catch (android.view.WindowManager.BadTokenException e) {
            //used to eliminate app crash exception if config activity was closed immediately
            //after refresh action (async task) was run and not finished
            //In other words, handles exception if activity has been closed before showing dialog
            dt.logV(TAG, e);
        }
    }

    static Boolean equalsNullSafe(Object obj1, Object obj2) {
        Boolean result;
        result = obj1 == null && obj2 == null;
        if (!result) {
            result = obj1 != null && obj1.equals(obj2);
        }
        return result;
    }

//    private

    /**
     * Return Color int as per Android SDK version
     * */
    @SuppressWarnings("deprecation")
    public final int getColorVersionSafe(int id) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 23 and newer
            return getColor(id);
        } else {
            return res.getColor(id);
        }
    }

    /**
     * Return Drawable as per Android SDK version
     * */
    @SuppressWarnings("deprecation")
    protected Drawable getDrawableVersionSafe(int viewId) {
        Drawable drawable;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) { // 21 and newer
            drawable = getDrawable(viewId);
        } else { // 20 and older
            drawable = res.getDrawable(viewId);
        }
        return drawable;
    }

    private class DisplayJSONNames extends AsyncTask<String, Integer, String> {
        private ListView namesListView;
        private JSONWorker jWorker;
        private Activity context;

        DisplayJSONNames(ListView view, Activity context){
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
            //stops loading animation
            ImageView infinityLoaderView = findViewById(R.id.infinityLoaderView);
            AnimationDrawable infinityLoaderAnimation;
            infinityLoaderAnimation = (AnimationDrawable) infinityLoaderView.getDrawable();
            infinityLoaderAnimation.stop();
            infinityLoaderView.setImageDrawable(null);

            String errStr = jWorker.parseJSONFromResponse(context, result);
            if (!errStr.equals("")) { //error
                //user info
                View v = findViewById(R.id.activity_sens_graph_configure);
                showInfoDialog(v, errStr);
                setSaveBtnState(false);
            } else { //no error
                jWorker.makeNamesValuesLists();
                TwoTvArrayAdapter twoTvArrayAdapter = new TwoTvArrayAdapter(context,
                        jWorker.namesList.toArray(new String[0]),
                        jWorker.valuesList.toArray(new String[0]));
                namesListView.setAdapter(twoTvArrayAdapter);

                namesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    /**
                     * 0.2 Clicked list element sets first unset variable
                     */
                    public void onItemClick(AdapterView parent, View v, int position, long id) {
                        String nameClicked;
                        TextView tvClicked = v.findViewById(R.id.namesListTextView);

                        nameClicked = tvClicked.getText().toString();
                        nameClicked = clearControlChars(nameClicked);
                        if (sensorName == null) { //if not set
                            if (!equalsNullSafe(nameClicked, sensorValue)) { //same element for name and value is not allowed
                                sensorName = nameClicked;
                                final TextView tv = findViewById(R.id.sensorNameTv);
                                tv.setText(res.getString(R.string.sensor_name_text) + " " + sensorName);
                                tv.setTextColor(getColorVersionSafe(R.color.greenLight));
                            }
                        } else if (sensorValue == null) {
                            if (!equalsNullSafe(nameClicked, sensorName)) { //same element is not allowed
                                sensorValue = nameClicked;
                                final TextView tv = findViewById(R.id.sensorValueTv);
                                tv.setText(res.getString(R.string.sensor_value_text) + " " + sensorValue);
                                tv.setTextColor(getColorVersionSafe(R.color.greenLight));
                            }
                        }
                        configHasErrors(v, false);
                    }

                    private String clearControlChars(String strToProcess) {
                        return strToProcess.replaceAll("\0 +", "").replace("\n", ""); //single null char and all spaces afterwards and \n
                    }
                });
                //updates url field with saved url without ending "/" symbols, if it was corrected
                //and fixes focus after urlField text change (to not focus on first element editText)
                final TextView urlField = findViewById(R.id.nameUrlValueEdit);
                if (!url.equals(urlField.getText())) {
                    urlField.setText(url);
                    namesListView.requestFocus();
                }

            }
        }
    }
}
