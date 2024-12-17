package com.example.easyvote;

import androidx.appcompat.app.AppCompatActivity;



import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import android.widget.Toolbar;

import com.example.easyvote.adaptors.PollListAdaptor;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CreatePolls extends AppCompatActivity {


    EditText descriptionInput;
    EditText inputOption;
    View addOption;
    View create;
    String description;
    ListView listView;
    Toolbar toolbar;
    ArrayList<String> options;
    ArrayList <String>optionIndex;
    ArrayList<Integer> age;
    String optionindex;
    TextView closingdate;
    Calendar calendar;
    CustomWaitingDialogBox customWaitingDialogBox;
    Map<String, Object> poll = new HashMap<>();
    Map<String,Object> results = new HashMap<>();

    private boolean isDateTimeSelected = false;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_polls);



        Intent intent = getIntent();
        String source = intent.getStringExtra("SOURCE_ACTIVITY");



        options = new ArrayList<>();
        optionIndex = new ArrayList<>();
        age = new ArrayList<>();
        descriptionInput = findViewById(R.id.description);
        inputOption = findViewById(R.id.option);
        addOption = findViewById(R.id.addbtn);
        create = findViewById(R.id.create);
        listView = findViewById(R.id.optionlist);
        optionindex = Integer.toString(0);
        calendar = Calendar.getInstance();
        closingdate = findViewById(R.id.closing_date);
        toolbar = findViewById(R.id.create_page_toolbar);
        setActionBar(toolbar);
        customWaitingDialogBox = new CustomWaitingDialogBox(CreatePolls.this);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        // adding the options into the list below (UI list below te option input field)
        PollListAdaptor pollListAdaptor = new PollListAdaptor(CreatePolls.this,options ,optionIndex);
        listView.setAdapter(pollListAdaptor);

        addOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(TextUtils.isEmpty(descriptionInput.getText())){
                    descriptionInput.setError("Add Description first");
                    return;
                }
                if(TextUtils.isEmpty(inputOption.getText())){
                    inputOption.setError("Option can't be empty");
                    return;
                }
                int intValue = Integer.parseInt(optionindex); // Convert String to int
                intValue++; // Increment the int value
                optionindex = String.valueOf(intValue);
                options.add(inputOption.getText().toString());
                optionIndex.add(optionindex);
                pollListAdaptor.notifyDataSetChanged();
                inputOption.setText("");

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                options.remove(position);
                pollListAdaptor.notifyDataSetChanged();
                return true;
            }
        });

        // create the poll inside the data base when user press the create button
        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isDateTimeSelected){
                    Toast.makeText(CreatePolls.this,"Closing date is needed",Toast.LENGTH_SHORT).show();
                    return;
                }

                if(TextUtils.isEmpty(descriptionInput.getText())){
                    descriptionInput.setError("Description can't be Empty");
                    return;
                }
                if(options.size()<2){
                    Toast.makeText(CreatePolls.this,"At least two options needed",Toast.LENGTH_SHORT).show();
                }
                else{

                    customWaitingDialogBox.showDialog("Creating...",false);

                    description = descriptionInput.getText().toString();

                    poll.put("description", description);

                    // Add options as individual fields
                    for (int i = 0; i < options.size(); i++) {
                        poll.put("option" + (i + 1), options.get(i));
                        results.put("option" + (i + 1)+" Male", 0);
                        results.put("option" + (i + 1)+" Female", 0);
                    }

                    poll.put("number of options",Integer.toString(options.size()));
                    results.put("age",age);

                    if(source != null && source.equals("single")){
                        PollManager pollManager = new PollManager(CreatePolls.this);
                        pollManager.createPoll(poll, results, new PollManager.onPollCreateListner() {
                            @Override
                            public void onPollCreated(String ID) {
                                Toast.makeText(CreatePolls.this,ID + "created with ID",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CreatePolls.this, HomePage.class);
                                customWaitingDialogBox.hideDialog();
                                startActivity(intent);
                            }

                            @Override
                            public void onPollCreationFaliure() {
                                Toast.makeText(CreatePolls.this,"Failed",Toast.LENGTH_SHORT).show();
                                customWaitingDialogBox.hideDialog();

                            }
                        });
                    } else if (source != null && source.equals("group")) {

                        String groupID = intent.getStringExtra("GroupID");

                        GroupPollManager groupPollManager = new GroupPollManager(CreatePolls.this);
                        groupPollManager.createPoll(poll, results, groupID, new PollManager.onPollCreateListner() {
                            @Override
                            public void onPollCreated(String ID) {
                                Toast.makeText(CreatePolls.this,ID + "created with ID",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(CreatePolls.this, GroupInside.class);
                                intent.putExtra("DocumentId",groupID);
                                customWaitingDialogBox.hideDialog();
                                startActivity(intent);
                            }

                            @Override
                            public void onPollCreationFaliure() {
                                Toast.makeText(CreatePolls.this,"Failed",Toast.LENGTH_SHORT).show();
                                customWaitingDialogBox.hideDialog();
                            }
                        });
                    }
                    else{
                        Toast.makeText(CreatePolls.this,"Failed",Toast.LENGTH_SHORT).show();

                    }


                }

            }
        });

        closingdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateTimePickerDialog();
            }
        });

    }

    private void showDateTimePickerDialog() {
        int year_current = calendar.get(Calendar.YEAR);
        int month_current = calendar.get(Calendar.MONTH);
        int date_current = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                int year_select = year;
                int month_select = month;
                int date_select = dayOfMonth;

                TimePickerDialog timePickerDialog = new TimePickerDialog(CreatePolls.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        // Your code when a time is selected
                        Calendar selectedDateTime = Calendar.getInstance();
                        selectedDateTime.set(year_select, month_select, date_select, hourOfDay, minute);

                        if (selectedDateTime.compareTo(calendar) > 0 || selectedDateTime.equals(calendar)) {
                            // The selected date and time is today or in the future
                            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
                            String selectedDateTimeString = dateFormat.format(selectedDateTime.getTime());
                            closingdate.setText(year_select+"/"+month_select+"/"+date_select +" at "+hourOfDay+":"+minute);
                            isDateTimeSelected = true;
                            poll.put("Close",selectedDateTimeString);

                        } else {
                            // The selected date and time is in the past
                            Toast.makeText(CreatePolls.this,"Selected date and time is passes",Toast.LENGTH_SHORT).show();

                        }
                    }
                }, hour, minute, true);
                timePickerDialog.show();
            }
        }, year_current, month_current, date_current);

        datePickerDialog.show();
    }


}