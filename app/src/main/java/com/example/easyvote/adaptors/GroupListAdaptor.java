package com.example.easyvote.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

import com.example.easyvote.R;

import java.util.ArrayList;

public class GroupListAdaptor extends BaseAdapter {

    ArrayList<String> groupnames;
    ArrayList<String> adminNames;
    LayoutInflater inflater;
    Context context;

    public GroupListAdaptor(Context context,ArrayList<String> groupnames, ArrayList<String> adminNames) {

        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }

        this.groupnames = groupnames;
        this.adminNames = adminNames;
        this.inflater = inflater;
        this.context = context;
        inflater=LayoutInflater.from(context);

    }


    @Override
    public int getCount() {
        return groupnames.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = inflater.inflate(R.layout.group_list_layout, null);
        TextView textView = (TextView) convertView.findViewById(R.id.group_name_list_view);
        TextView adminName = (TextView) convertView.findViewById(R.id.admin_name);
        textView.setText(groupnames.get(position));
        adminName.setText("Admin : " + adminNames.get(position));
        convertView.setBackground(ContextCompat.getDrawable(context, R.drawable.group_list_item_background));

        return convertView;
    }
}
