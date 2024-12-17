package com.example.easyvote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.List;
import android.net.Uri;

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
        customWaitingDialogBox = new CustomWaitingDialogBox(VotingPage.this);

        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(VotingPage.this, Login_Page.class);
            startActivity(intent);
            finish();
        }

        // Get the pollID from the intent
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

        pollManager.loadPollVoting(pollID, new PollManager.onPollLoadedListner() {
            @Override
            public void onPollLoaded(ArrayList<String> options, String descripton) {
                PollUtil pollUtil = new PollUtil(VotingPage.this);
                pollUtil.checkExpire(pollID, new PollUtil.DateTimeListener() {
                    @Override
                    public void onDateBeforeCurrent() {
                        runOnUiThread(() -> {
                            loadRadioButtons(options);
                            description.setText(descripton);
                            Toast.makeText(VotingPage.this, "Loading Successful", Toast.LENGTH_SHORT).show();

                            votebtn.setOnClickListener(v -> {
                                if (radioGroup.getCheckedRadioButtonId() == -1) {
                                    Toast.makeText(VotingPage.this, "Your Option is Not Selected", Toast.LENGTH_SHORT).show();
                                } else {
                                    customWaitingDialogBox.showDialog("Processing...", false);
                                    int radioID = radioGroup.getCheckedRadioButtonId();
                                    int optionIndex = radioGroup.indexOfChild(findViewById(radioID)) + 1; // +1 to convert 0-based index to 1-based option number
                                    pollManager.addVote(pollID, optionIndex, new PollManager.onVoteListner() {
                                        @Override
                                        public void onVoteSuccessful() {
                                            customWaitingDialogBox.hideDialog();
                                            Toast.makeText(VotingPage.this, "Vote Successful", Toast.LENGTH_SHORT).show();
                                            Intent intent1 = new Intent(VotingPage.this, HomePage.class);
                                            startActivity(intent1);
                                        }

                                        @Override
                                        public void onVoteFaliure() {
                                            customWaitingDialogBox.hideDialog();
                                            Toast.makeText(VotingPage.this, "Vote Failed", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            });
                        });
                    }

                    @Override
                    public void onDateAfterCurrent() {
                        runOnUiThread(() -> {
                            Toast.makeText(VotingPage.this, "Poll Closed", Toast.LENGTH_SHORT).show();
                            description.setText("Poll Closed");
                            backbuttonclick();
                        });
                    }

                    @Override
                    public void onDateComparisonError() {
                        runOnUiThread(() -> {
                            Toast.makeText(VotingPage.this, "Error While Loading..", Toast.LENGTH_SHORT).show();
                            description.setText("Something went wrong. Try Again");
                            backbuttonclick();
                        });
                    }
                });
            }

            @Override
            public void onPollLoadingFailed() {
                Toast.makeText(VotingPage.this, "Loading Failed. Try again", Toast.LENGTH_SHORT).show();
                description.setText("Loading Failed");
                backbuttonclick();
            }

            @Override
            public void pollNotExist() {
                Toast.makeText(VotingPage.this, "Poll is not Available", Toast.LENGTH_SHORT).show();
                description.setText("Poll is not Available or Does not Exist. Check the Poll ID");
                backbuttonclick();
            }

            @Override
            public void ReturnVoter() {
                description.setText("Already Completed");
                backbuttonclick();
            }
        });
    }

    private void backbuttonclick() {
        votebtn.setBackgroundResource(R.drawable.back);
        voteText.setText("Go Back");
        votebtn.setOnClickListener(v -> onBackPressed());
    }

    private void loadRadioButtons(ArrayList<String> options) {
        for (int i = 0; i < options.size(); i++) {
            RadioButton radioButton = new RadioButton(VotingPage.this);
            radioButton.setId(i); // Set a unique ID for each radio button
            radioButton.setText(options.get(i));
            radioButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
            radioButton.setTypeface(null, Typeface.BOLD_ITALIC);
            radioGroup.addView(radioButton);
        }
    }

    // Helper method to handle the vote submission
    public void addVote(String pollID, int optionIndex, final PollManager.onVoteListner listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference pollRef = db.collection("polls").document(pollID);

        // Fetch current poll data
        pollRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    List<Long> votes = (List<Long>) document.get("votes");
                    if (votes != null) {
                        // Increment the vote count for the selected option
                        votes.set(optionIndex - 1, votes.get(optionIndex - 1) + 1);

                        // Update the votes in Firestore
                        pollRef.update("votes", votes).addOnCompleteListener(updateTask -> {
                            if (updateTask.isSuccessful()) {
                                listener.onVoteSuccessful();
                            } else {
                                listener.onVoteFaliure();
                            }
                        });
                    } else {
                        listener.onVoteFaliure();
                    }
                } else {
                    listener.onVoteFaliure();
                }
            } else {
                listener.onVoteFaliure();
            }
        });
    }
}
