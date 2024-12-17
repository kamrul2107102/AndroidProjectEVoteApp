package com.example.easyvote.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.easyvote.R;

import java.util.ArrayList;

public class GroupPollListAdaptor extends BaseAdapter {


    ArrayList<String> dueDates;
    ArrayList<String> descriptions;

    ArrayList<Boolean> status;

    ArrayList<String> creaters;
    LayoutInflater inflater;

    Context context;

    public GroupPollListAdaptor(ArrayList<String> dueDates, ArrayList<String> descriptions, ArrayList<Boolean> status, ArrayList<String> creaters, Context context) {
        this.dueDates = dueDates;
        this.descriptions = descriptions;
        this.status = status;

        this.creaters = creaters;
        this.context = context;
        inflater=LayoutInflater.from(context);

    }

    @Override
    public int getCount() {
        return descriptions.size();
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

        convertView = inflater.inflate(R.layout.group_poll_list_layout, null);
        TextView description = (TextView) convertView.findViewById(R.id.group_poll_list_description);
        TextView creater = (TextView) convertView.findViewById(R.id.group_poll_list_creater);
        TextView duedate = (TextView) convertView.findViewById(R.id.group_poll_list_due_date_text);
        View timer = (View) convertView.findViewById(R.id.timer1);
        description.setText(descriptions.get(position));
        creater.setText("Created by :"+creaters.get(position));
        duedate.setText(dueDates.get(position));


        if(status.get(position)){

        }else{
            timer.setVisibility(View.INVISIBLE);
        }

        return convertView;

    }
}
