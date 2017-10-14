package org.psylo.sensgraph;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * Two array adapter for a ListView with two textViews
 */

class TwoTvArrayAdapter extends ArrayAdapter<String>{

    private final Activity context;
    private final String[] vals;
    private final String[] names;

    TwoTvArrayAdapter(Activity context, String[] names, String[] vals) {
        super(context, R.layout.names_list_item_layout, names);
        this.context = context;
        this.vals = vals;
        this.names = names;
    }

    private static class ViewHolder {
        private TextView pathTv;
        private TextView valueTv;
    }

    @Override
    public @NonNull View getView(int position, View view, @NonNull ViewGroup parent) {
        ViewHolder mViewHolder;
        if (view == null) {
            LayoutInflater inflater = context.getLayoutInflater();
            view = inflater.inflate(R.layout.names_list_item_layout, null); //here if not null passed works incorectly
            mViewHolder = new ViewHolder();
            mViewHolder.pathTv = view.findViewById(R.id.namesListTextView);
            mViewHolder.valueTv = view.findViewById(R.id.namesListValueTextView);
            view.setTag(mViewHolder);

        } else {
            mViewHolder = (ViewHolder) view.getTag();
        }

        mViewHolder.pathTv.setText(names[position]);
        mViewHolder.valueTv.setText(vals[position]);

        return view;
    }
}
