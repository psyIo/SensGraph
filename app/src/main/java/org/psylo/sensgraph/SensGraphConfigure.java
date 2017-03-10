package org.psylo.sensgraph;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.widget.CheckedTextView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;


public class SensGraphConfigure extends AppCompatActivity {

    static final String TAG = "SensGraphConfAct";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        int mAppWidgetId = 0;
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        setContentView(R.layout.activity_sensgraph_configure);
//        CheckedTextView jsonNamesCheckedView = (CheckedTextView) findViewById(R.id.jsonNamesCheckedView);
//        char[] sa = new char[10];
//        sa[0] = 65;
//        sa[1] = 66;
//        sa[2] = 67;
//
//        jsonNamesCheckedView.setText(sa,1,1);

        List<String> myStringArray = new ArrayList<String>();
        myStringArray.add("Vienas");
        myStringArray.add("Du");

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                R.layout.names_list_item_layout, myStringArray);
        ListView listView = (ListView) findViewById(R.id.namesList);
        listView.setAdapter(adapter);

        final Button saveButton = (Button) findViewById(R.id.save_settings_btn);
        saveButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int mAppWidgetId = 0;
                //dev+
                Intent intent = getIntent();
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    mAppWidgetId = extras.getInt(
                            AppWidgetManager.EXTRA_APPWIDGET_ID,
                            AppWidgetManager.INVALID_APPWIDGET_ID);
                }
                Log.v(TAG, "mAppWidgetId: " + Integer.toString(mAppWidgetId));
                //dev-
//                Context context = v.getContext();
//                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
//                RemoteViews views = new RemoteViews(context.getPackageName(),
//                        R.layout.simple_widget);

//                if (extras != null) {
//                    mAppWidgetId = extras.getInt(
//                            AppWidgetManager.EXTRA_APPWIDGET_ID,
//                            AppWidgetManager.INVALID_APPWIDGET_ID);
//                }
//                appWidgetManager.updateAppWidget(mAppWidgetId, views);

                Intent resultIntent = new Intent();
                resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
                setResult(RESULT_OK, resultIntent);
                Log.v(TAG,"Before finnish()"); //dev
                finish();
            }
        });

    }
}
