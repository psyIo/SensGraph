package org.psylo.sensgraph;

import android.content.Intent;
import android.os.IBinder;
import android.widget.RemoteViewsService;

/**
 * Created by psylo on 17.7.4.
 * RemoteViewsService used to update ListView using RemoteViewsListProvider
 */

public class UpdateWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new RemoteViewsListProvider(this.getApplicationContext(), intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    public UpdateWidgetService() {
        super();
    }
}
