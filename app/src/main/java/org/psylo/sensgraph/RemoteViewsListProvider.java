package org.psylo.sensgraph;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.List;

/**
 * Created by psylo on 17.7.4.
 * RemoteViewsListProvider used to update debugging ListView on widget
 */

class RemoteViewsListProvider implements RemoteViewsService.RemoteViewsFactory {

    private Context context;
    private int appWidgetId;
    private List itemList;
    private final static String TAG = "RemoteViewsListProvider";

    RemoteViewsListProvider(Context context, Intent intent) {
        this.context = context;
        this.appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        this.itemList = intent.getStringArrayListExtra("LIST_VALUES");
//        DevTools.logE(TAG, "intent.getStringArrayListExtra(\"LIST_VALUES\") " + intent.getStringArrayListExtra("LIST_VALUES") + " appWidgetId " +
//            appWidgetId);

    }

    @Override
    public RemoteViews getLoadingView() {
        DevTools.log(TAG, "getLoadingView"); //dev
        return null;
    }

    @Override
    public int getCount() {
        return itemList.size();
    }

    @Override
    public void onDestroy() {
        DevTools.log(TAG, "OnDestroy"); //dev
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public long getItemId(int position) {
        DevTools.log(TAG, "getItemId"); //dev
        return position;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        Object obj = itemList.get(position);
        final RemoteViews remoteView = new RemoteViews(context.getPackageName(), R.layout.single_text_view_for_debug_list);
        remoteView.setTextViewText(R.id.singleTextViewDebug, obj.toString());
//        DevTools.logE(TAG, "getViewAt int position " + String.valueOf(position), "obj in post", obj);
        return remoteView;
    }

    @Override
    public void onDataSetChanged() {
        DevTools.log(TAG, "itemList before change", itemList);
//        this.itemList = savedIntent.getStringArrayListExtra("LIST_VALUES");
    }

    @Override
    public void onCreate() {
//        DevTools.log(TAG, "onCreate"); //dev
    }
}