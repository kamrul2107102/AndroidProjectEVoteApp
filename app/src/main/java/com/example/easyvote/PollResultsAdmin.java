package com.example.easyvote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;


import com.example.easyvote.adaptors.AgeRangeXAxisValueFormatter;
import com.example.easyvote.adaptors.PollListAdaptor;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;
import java.util.List;
public class PollResultsAdmin extends AppCompatActivity {


    ArrayList<String> loadedOptions = new ArrayList<>();
    ProgressBar progressBar;
    TextView descriptionTxt, emptyVotesText, title1, title2;
    ListView optionList;
    String pollId;
    String groupId;
    Toolbar toolbar;
    Switch pollViewEnableSwitch;
    PieChart pieChart ;
    BarChart barChart;
    NestedScrollView scrollView;
    String pollDescription;  // for share function
    boolean type;  //  used to check the poll type (group poll or user poll)
    PollManager pollManager = new PollManager(PollResultsAdmin.this);

    PollUtil pollUtil;
    boolean status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poll_results_admin);

        Intent intent = getIntent();
        String documentId = intent.getStringExtra("PollID");
        status = intent.getBooleanExtra("Status",false);
        groupId = intent.getStringExtra("groupID");
        pollId = documentId;

        if(groupId == null){
            type = true;  //group poll
        }else{
            type = false; // user poll
        }



        progressBar = findViewById(R.id.progress_bar);
        descriptionTxt = findViewById(R.id.description);
        optionList = findViewById(R.id.option_list);
        toolbar = findViewById(R.id.poll_admin_toolbar);
        setSupportActionBar(toolbar);
        pollViewEnableSwitch =findViewById(R.id.show_poll_switch);
        scrollView = findViewById(R.id.scrollViewNested);
        toggleGraphVisibility();
        emptyVotesText = findViewById(R.id.empty_votes_text);
        pieChart = findViewById(R.id.pie_chart);
        barChart = findViewById(R.id.bar_chart);
        title1 = findViewById(R.id.title1);
        title2 = findViewById(R.id.title2);


        pollViewEnableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    descriptionTxt.setVisibility(View.VISIBLE);
                    optionList.setVisibility(View.VISIBLE);
                    toggleGraphVisibility();


                }else{
                    descriptionTxt.setVisibility(View.GONE);
                    optionList.setVisibility(View.GONE);
                    toggleGraphVisibility();

                }
            }
        });





        pollUtil = new PollUtil(PollResultsAdmin.this);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        pollManager.loadPoll(pollId, new PollManager.onPollLoadedListner() {
            @Override
            public void onPollLoaded(ArrayList<String> options, String description) {

                pollDescription = description; // for share function
                loadedOptions.addAll(options);
                ArrayList<String> optionindex = new ArrayList<>();
                for(int i=1; i<= options.size();i++){
                    optionindex.add(String.valueOf(i));
                }

                descriptionTxt.setText(description);
                PollListAdaptor pollListAdaptor = new PollListAdaptor(PollResultsAdmin.this,options,optionindex);
                optionList.setAdapter(pollListAdaptor);


            }

            @Override
            public void onPollLoadingFailed() {

                descriptionTxt.setText("Error while retriving the data. Please try again");
            }

            @Override
            public void pollNotExist() {

            }

            @Override
            public void ReturnVoter() {

            }
        });




        pollManager.getPollResults(pollId, new PollManager.onPollResultListner() {
            @Override
            public void getResultSuccesful(ArrayList<Long> resultsMale, ArrayList<Long> resultsFemale, ArrayList<Long> ages, int numberOfOptions) {




                if (!resultsMale.isEmpty() && !resultsFemale.isEmpty() && !loadedOptions.isEmpty()) {

                    Long male, female;
                    Long total = Long.valueOf(0);
                    for(int i=0;i<numberOfOptions;i++){
                        male = resultsMale.get(i);
                        female = resultsFemale.get(i);
                        total = total+ male+female;
                    }


                    if(total == 0){
                        barChart.setVisibility(View.GONE);
                        pieChart.setVisibility(View.GONE);
                        emptyVotesText.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.GONE);
                        title1.setVisibility(View.GONE);
                        title2.setVisibility(View.GONE);
                        return;
                    }
                    if(resultsMale.size() == numberOfOptions && resultsFemale.size()==numberOfOptions){
                        // setting up pie chart
                        setPieChart(resultsMale,resultsFemale,numberOfOptions);

                      }
                    else {
                        Toast.makeText(PollResultsAdmin.this,"Error Getting Results",Toast.LENGTH_SHORT).show();
                    }

                    // setting up bar chart
                        if(ages != null){
                            setBarChartData(ages);
                        }else{
                            Toast.makeText(PollResultsAdmin.this,"Error Getting Results",Toast.LENGTH_SHORT).show();

                        }


                    // setting up list View
                    optionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            List<PieEntry> genderEntry = new ArrayList<>();
                            if(resultsMale.get(position)+resultsFemale.get(position) !=0){
                            genderEntry.add(new PieEntry(resultsMale.get(position),"male"));
                            genderEntry.add(new PieEntry(resultsFemale.get(position),"Female"));
                            showPieChartAlertDialog(PollResultsAdmin.this,genderEntry, false);
                            }else{
                            showPieChartAlertDialog(PollResultsAdmin.this, genderEntry, true);
                            }
                        }
                    });

                }else{
                    Toast.makeText(PollResultsAdmin.this,"Error getting results",Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void getResultFailed() {
                Toast.makeText(PollResultsAdmin.this,"get results failed",Toast.LENGTH_SHORT).show();

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.delete_toolbar,menu);
        // Find the menu item you want to hide
        MenuItem closePoll = menu.findItem(R.id.close_poll);

        boolean shouldHideMenuItem = status;

                // Set the visibility of the menu item based on the condition
                closePoll.setVisible(shouldHideMenuItem);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.delete_poll:
                AlertDialog.Builder builder = new AlertDialog.Builder(PollResultsAdmin.this);
                builder.setTitle("Confirm Action")
                        .setMessage("Do you want to delete this poll")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                pollManager.deleteUserPoll(pollId, type, groupId, new PollManager.onDeleteListner() {
                                    @Override
                                    public void onDeleted() {
                                        Intent intent = new Intent(PollResultsAdmin.this,HomePage.class);
                                        startActivity(intent);
                                    }

                                    @Override
                                    public void ondeleteFailiure() {
                                        Toast.makeText(PollResultsAdmin.this,"Delete Failed. Try again",Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onNetworkFaliure() {
                                        DialogUtils.showNetworkErrorDialog(PollResultsAdmin.this);
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;
            case R.id.close_poll:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(PollResultsAdmin.this);
                builder1.setTitle("Confirm Action")
                        .setMessage("Are you sure want to close this poll")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                pollManager.closePoll(pollId, new PollManager.onPollCloseListner() {
                                    @Override
                                    public void onPollClosed() {
                                        Toast.makeText(PollResultsAdmin.this,"Poll Closed",Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onPollCloseFailled() {
                                        Toast.makeText(PollResultsAdmin.this,"Failed. try again",Toast.LENGTH_SHORT).show();
                                    }

                                    @Override
                                    public void onNetworkfaliure() {
                                        DialogUtils.showNetworkErrorDialog(PollResultsAdmin.this);
                                    }
                                });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
                break;

            case R.id.share_poll:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(PollResultsAdmin.this);
                View dialogView = getLayoutInflater().inflate(R.layout.share_dialogbox, null);
                builder2.setView(dialogView).
                        setCancelable(false);

                AlertDialog dialog = builder2.create();
                TextView massage = dialogView.findViewById(R.id.result_massage);
                EditText uID = dialogView.findViewById(R.id.UID);
                ProgressBar progressBar2 = dialogView.findViewById(R.id.share_dialog_progress_bar);
                View cancelBtn = dialogView.findViewById(R.id.cancel_share_dialog);

                dialog.show();

                Button shareLinkButton = dialogView.findViewById(R.id.share_link_button);
                shareLinkButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        shareLink();
                        dialog.dismiss();

                    }
                });

                cancelBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                Button shareIdButton = dialogView.findViewById(R.id.share_id_button);
                shareIdButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        massage.setVisibility(View.INVISIBLE);
                        String userId = uID.getText().toString();
                        if(TextUtils.isEmpty(userId)){
                            uID.setError("Enter Receiver UID");
                        }else{
                            progressBar2.setVisibility(View.VISIBLE);
                            pollUtil.sharePoll(userId, pollId, pollDescription, new PollUtil.onShareListner() {
                                @Override
                                public void onSuccessfulShare() {
                                    progressBar2.setVisibility(View.INVISIBLE);
                                    massage.setVisibility(View.VISIBLE);
                                    massage.setTextColor(Color.GREEN);
                                    massage.setText("Successful");
                                    uID.setText("");
                                }

                                @Override
                                public void onAlreadyShared() {
                                    progressBar2.setVisibility(View.INVISIBLE);
                                    massage.setVisibility(View.VISIBLE);
                                    massage.setTextColor(Color.GREEN);
                                    massage.setText("Already Shared");
                                }

                                @Override
                                public void onShareFailed() {
                                    progressBar2.setVisibility(View.INVISIBLE);
                                    massage.setVisibility(View.VISIBLE);
                                    massage.setTextColor(Color.RED);
                                    massage.setText("Try Again");
                                }

                                @Override
                                public void onUsernNotExists() {
                                    progressBar2.setVisibility(View.INVISIBLE);
                                    massage.setVisibility(View.VISIBLE);
                                    massage.setTextColor(Color.RED);
                                    massage.setText("Incorrect UID");
                                }
                            });

                        }
                    }
                });

                dialog.show();

                break;

        }
        return true;
    }



    private void shareLink(){
        String host = "easyvote.polls";
        String scheme = "easyvote";

        Uri deepLink = new Uri.Builder()
                .scheme(scheme)
                .authority(host)
                .path(pollId)
                .build();

        String deepLinkUrl = deepLink.toString();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Check out this poll: " + deepLinkUrl);

        startActivity(Intent.createChooser(shareIntent, "Share Poll"));
    }



    private void showPieChartAlertDialog(Context context, List<PieEntry> entries, boolean empty) {

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.pie_chart_dialogbox_layout, null);

        PieChart pieChart = view.findViewById(R.id.pie_chart_dialog_box);
        TextView emptyText = view.findViewById(R.id.empty_text_pie_chart_dialog_box);
        PieDataSet dataSet = new PieDataSet(entries, "Gender");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData(dataSet);

        if(empty){
            emptyText.setVisibility(View.VISIBLE);
            pieChart.setVisibility(View.GONE);
        }else {

            pieChart.setData(data);
            pieChart.getDescription().setEnabled(false);
            pieChart.animateY(1000);

        }
        builder.setView(view)
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void setBarChartData(ArrayList<Long> agesList) {

        // Define your age ranges
        int[] ageRanges = {0, 10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        int[] ageRangeCounts = new int[ageRanges.length - 1];

        int[] barColors = new int[]{
                ContextCompat.getColor(this, R.color.theme),
        };
        // Count occurrences in each age range
        for (Long age : agesList) {
            for (int i = 0; i < ageRanges.length - 1; i++) {
                if (age >= ageRanges[i] && age < ageRanges[i + 1]) {
                    ageRangeCounts[i]++;
                    break;
                }
            }
        }

        // Populate barEntries with data from ageRangeCounts
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        for (int i = 0; i < ageRangeCounts.length; i++) {
            barEntries.add(new BarEntry(i, ageRangeCounts[i]));
        }

        BarDataSet barDataSet = new BarDataSet(barEntries, "Number of Peoples");
        BarData barData = new BarData(barDataSet);
        barDataSet.setColors(barColors);

        // Customize the x-axis labels
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1);
        xAxis.setDrawGridLines(false); // Hide x-axis grid lines
        xAxis.setCenterAxisLabels(true); // Center align the labels

        barChart.getDescription().setEnabled(false);




        // Set custom labels
        xAxis.setLabelCount(ageRanges.length - 1); // Set the number of labels
        xAxis.setValueFormatter(new AgeRangeXAxisValueFormatter(ageRanges));


        // Customize the appearance of the bar chart if needed
        barChart.setData(barData);
        barChart.animateY(1000);
        barChart.getXAxis().setDrawGridLines(false); // Hide x-axis grid lines
        barChart.invalidate(); // Refresh the chart
    }

    public void setPieChart(ArrayList<Long> resultsMale,ArrayList<Long> resultsFemale, int numberOfOptions){


        List<PieEntry> entries = new ArrayList<>();
        Long male1,female1;


        for(int i=0;i<numberOfOptions;i++){
            male1 = resultsMale.get(i);
            female1 = resultsFemale.get(i);
            float totalVotee = male1+female1;

            entries.add(new PieEntry(totalVotee,loadedOptions.get(i)));
        }


        PieDataSet dataSet = new PieDataSet(entries, "Results");
        dataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        PieData data = new PieData(dataSet);

        dataSet.setDrawValues(true);
        dataSet.setValueTextSize(16f);
        dataSet.setValueTextColor(R.color.theme);

        pieChart.setData(data);
        pieChart.animateY(1000);
        pieChart.getDescription().setEnabled(false);
        pieChart.invalidate();

        progressBar.setVisibility(View.GONE);
        pieChart.setVisibility(View.VISIBLE);
    }

    private void toggleGraphVisibility() {
        // Set the height of the NestedScrollView based on its current state
        scrollView.getLayoutParams().height =
                (scrollView.getLayoutParams().height == ViewGroup.LayoutParams.MATCH_PARENT)
                        ? 0 : ViewGroup.LayoutParams.MATCH_PARENT;

        // Request a layout update to reflect the changes
        scrollView.requestLayout();}


}