package com.example.easyvote.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.easyvote.R;

import java.util.ArrayList;


public class SharedNewPollListAdaptor extends BaseAdapter {

    ArrayList<String> dueDates;
    ArrayList<String> descriptions;
    ArrayList<String> senderName;
    LayoutInflater inflater;
    ArrayList<Boolean> status;

    Context context;

    public SharedNewPollListAdaptor(ArrayList<String> dueDates, ArrayList<String> descriptions, ArrayList<String> senderName, ArrayList<Boolean> status,  Context context) {
        this.dueDates = dueDates;
        this.descriptions = descriptions;
        this.senderName = senderName;
        this.status = status;
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
        convertView = inflater.inflate(R.layout.sahared_new_poll_list_layout, null);
        TextView description = (TextView) convertView.findViewById(R.id.shared_new_poll_list_description);
        TextView senderNameTxt = (TextView) convertView.findViewById(R.id.shared_new_poll_list_sender);
        TextView duedate = (TextView) convertView.findViewById(R.id.shared_new_poll_list_due_date_text);
        LinearLayout background = (LinearLayout) convertView.findViewById(R.id.shared_poll_list_background);
        description.setText(descriptions.get(position));
        senderNameTxt.setText("Poll ID :"+senderName.get(position));
        duedate.setText(dueDates.get(position));

        if(!status.get(position)){
            background.setBackgroundResource(R.drawable.shared_new_poll_list_background);

        }else{
            background.setBackgroundResource(R.drawable.sahred_poll_list_background_old);
        }

        return convertView;
    }
}
