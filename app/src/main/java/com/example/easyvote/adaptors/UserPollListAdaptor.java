package com.example.easyvote.adaptors;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.easyvote.R;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class UserPollListAdaptor extends BaseAdapter {

    ArrayList<String> dueDates;
    ArrayList<String> descriptions;

    ArrayList<Boolean> status;
    ArrayList<String> pollId;
    LayoutInflater inflater;

    Context context;


    public UserPollListAdaptor(Context context,ArrayList<String> dueDates, ArrayList<String> descriptions, ArrayList<String> pollId, ArrayList<Boolean> status) {

        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        this.dueDates = dueDates;
        this.descriptions = descriptions;
        this.status = status;
        this.pollId = pollId;
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

        convertView = inflater.inflate(R.layout.user_poll_list_layout, null);
        TextView description = (TextView) convertView.findViewById(R.id.user_poll_list_description);
        TextView pollIdTxt = (TextView) convertView.findViewById(R.id.user_poll_list_poll_id);
        TextView duedate = (TextView) convertView.findViewById(R.id.user_poll_list_due_date_text);
        View timer = (View) convertView.findViewById(R.id.timer);
        description.setText(descriptions.get(position));
        pollIdTxt.setText(pollId.get(position));
        duedate.setText(dueDates.get(position));

        if(status.get(position)){

        }else{
            timer.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }
}
