package org.psylo.sensgraph;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Created by psylo on 17.3.26.
 */

public class TwoTvArrayAdapter extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] vals;
    private final String[] names;

    public TwoTvArrayAdapter(Activity context, String[] names, String[] vals) {
        super(context, R.layout.names_list_item_layout, names);
        this.context = context;
        this.vals = vals;
        this.names = names;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.names_list_item_layout, null, true);
        TextView nameTv = (TextView) rowView.findViewById(R.id.namesListTextView);
        TextView valueTv = (TextView) rowView.findViewById(R.id.namesListValueTextView);
        nameTv.setText(names[position]);
        valueTv.setText(vals[position]);

//        imageView.setImageResource(imageId[position]);
        return rowView;
    }
}
