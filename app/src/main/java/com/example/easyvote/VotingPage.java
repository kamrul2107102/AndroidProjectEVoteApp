package com.example.easyvote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class VotingPage extends AppCompatActivity {

    RadioGroup radioGroup;
    RadioButton radioButton;
    TextView description, voteText;
    View votebtn;
    String pollID;
    FirebaseAuth fAuth;
    CustomWaitingDialogBox customWaitingDialogBox;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting_page);

        fAuth = FirebaseAuth.getInstance();

        FirebaseUser currentUser = fAuth.getCurrentUser();
        customWaitingDialogBox = new CustomWaitingDialogBox(VotingPage.this);
        if(currentUser == null){
            Intent intent = new Intent(VotingPage.this, Login_Page.class);
            startActivity(intent);
            finish();
        }

        Intent intent = getIntent();

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // The activity is opened from a deep link
            Uri data = intent.getData();
            if (data != null) {
                 pollID = data.getLastPathSegment();
            }
        } else {
            // The activity is opened from within the application
            pollID = intent.getStringExtra("id");

        }



        radioGroup = findViewById(R.id.radio_group);
        description = findViewById(R.id.description);
        votebtn = findViewById(R.id.votebtn);
        voteText = findViewById(R.id.voteText);


       PollManager pollManager = new PollManager(VotingPage.this);

       pollManager.loadPollVoting(pollID,new PollManager.onPollLoadedListner() {
           @Override
           public void onPollLoaded(ArrayList<String> options, String descripton){


               PollUtil pollUtil = new PollUtil(VotingPage.this);
               pollUtil.checkExpire(pollID, new PollUtil.DateTimeListener() {
                   @Override
                   public void onDateBeforeCurrent() {

                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {

                               /*second method for make alert to user when checked a Radio button (if needed)

                              View.OnClickListener radioClickListener = new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       RadioButtonClicked(v);
                                   }
                               };
                               */

                               loadRadioButtons(options);

                               description.setText(descripton);
                               Toast.makeText(VotingPage.this,"Loding Successful",Toast.LENGTH_SHORT).show();

                               votebtn.setOnClickListener(new View.OnClickListener() {
                                   @Override
                                   public void onClick(View v) {
                                       if (radioGroup.getCheckedRadioButtonId() == -1) {
                                           Toast.makeText(VotingPage.this,"Your Option is Not Selected",Toast.LENGTH_SHORT).show();

                                       }
                                       else {
                                           customWaitingDialogBox.showDialog("Processing...", false);
                                           int radioID = radioGroup.getCheckedRadioButtonId();
                                           int optionIndex = radioGroup.indexOfChild(findViewById(radioID)) + 1; // +1 to convert 0-based index to 1-based option number
                                           pollManager.addVote(pollID, optionIndex, new PollManager.onVoteListner() {
                                               @Override
                                               public void onVoteSuccessful() {
                                                   customWaitingDialogBox.hideDialog();
                                                   Toast.makeText(VotingPage.this, "success", Toast.LENGTH_SHORT).show();
                                                   Intent intent1 = new Intent(VotingPage.this, HomePage.class);
                                                   startActivity(intent1);

                                               }

                                               @Override
                                               public void onVoteFaliure() {
                                                   customWaitingDialogBox.hideDialog();
                                                   Toast.makeText(VotingPage.this, "failed", Toast.LENGTH_SHORT).show();

                                               }
                                           });
                                       }
                                   }
                               });

                           }
                       });

                   }

                   @Override
                   public void onDateAfterCurrent() {

                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Toast.makeText(VotingPage.this,"Poll Closed",Toast.LENGTH_SHORT).show();
                               description.setText("Poll Closed");
                               backbuttonclick();
                           }
                       });


                   }

                   @Override
                   public void onDateComparisonError() {

                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               Toast.makeText(VotingPage.this,"Error While Loading..",Toast.LENGTH_SHORT).show();
                               description.setText("Something went wrong. Try Again");
                               backbuttonclick();
                           }
                       });


                   }
               });





           }
           @Override
           public void onPollLoadingFailed() {
               Toast.makeText(VotingPage.this,"Loding Failed. Try again",Toast.LENGTH_SHORT).show();
               description.setText("Loading Failed");
               backbuttonclick();
           }

           @Override
           public void pollNotExist() {
               Toast.makeText(VotingPage.this,"Poll is not Available",Toast.LENGTH_SHORT).show();
                description.setText("Poll is not Available or Does not Exist. check the Poll ID");
                backbuttonclick();

           }

           @Override
           public void ReturnVoter() {
               description.setText("Already Completed");
               backbuttonclick();
           }
       });


    }

    private void backbuttonclick(){
        votebtn.setBackgroundResource(R.drawable.back);
        voteText.setText("Go Back");
        votebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
    public void RadioButtonClicked(View v){
            int radioID = radioGroup.getCheckedRadioButtonId();
            radioButton = findViewById(radioID);
            Toast.makeText(this,radioButton.getText().toString(),Toast.LENGTH_SHORT).show();
    }

    private void loadRadioButtons(ArrayList<String> options){
        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(VotingPage.this);
            radioButton.setId(i); // Set a unique ID for each radio button
            radioButton.setText(options.get(i));
            // Set the text size and style
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20); // Set the text size to 24sp
            radioButton.setTypeface(null, Typeface.BOLD_ITALIC); // Set the text style to italic

          /*  // Add padding to the radio button
            int paddingDp = 5; // Convert 16dp to pixels
            float density = getResources().getDisplayMetrics().density;
            int paddingPixel = (int) (paddingDp * density);
            radioButton.setPadding(paddingPixel, paddingPixel, paddingPixel, paddingPixel);
            RadioGroup.LayoutParams params = new RadioGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER; // Set the gravity to center*/

            //radioButton.setOnClickListener(radioClickListener); // Set the onClickListener
            radioGroup.addView(radioButton); // Add the radio button to the RadioGroup
        }
    }


}