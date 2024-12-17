package com.example.easyvote;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.widget.Toolbar;


import com.example.easyvote.adaptors.SharedNewPollListAdaptor;

import java.util.ArrayList;

public class SharedPolls extends AppCompatActivity {

    ListView newPollList;
    TextView newPollEmpty;
    Toolbar toolbar;
    PollManager pollManager;

    CustomWaitingDialogBox customWaitingDialogBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shared_polls);

        newPollEmpty = findViewById(R.id.empty_new_pollsText);
        newPollList = findViewById(R.id.new_shared_poll_list);
        pollManager = new PollManager(getApplicationContext());
        customWaitingDialogBox = new CustomWaitingDialogBox(SharedPolls.this);
        toolbar = findViewById(R.id.shared_poll_toolbar);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        pollManager.getSharedPoll(new PollManager.onSharedPollLoadListner() {
            @Override
            public void onPollLoaded(ArrayList<String> pollDescription, ArrayList<String> pollID, ArrayList<String> closingdate, ArrayList<Boolean> status) {


                    SharedNewPollListAdaptor sharedNewPollListAdaptor = new SharedNewPollListAdaptor(closingdate,pollDescription,pollID,status,SharedPolls.this);
                    newPollList.setAdapter(sharedNewPollListAdaptor);
                    newPollList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getApplicationContext(),VotingPage.class);
                            intent.putExtra("id", pollID.get(position));
                            startActivity(intent);
                            finish();
                        }
                    });

                    newPollList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                            showDeleteConfirmationDialog(pollID.get(position));
                            return true;
                        }
                    });

            }

            @Override
            public void onPollLoadFailed() {
                Toast.makeText(getApplicationContext(), "loading Failed",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEmptyPolls() {
                newPollEmpty.setVisibility(View.VISIBLE);
                newPollList.setVisibility(View.INVISIBLE);
            }
        });


    }


    private void showDeleteConfirmationDialog(final String pollId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you want to delete this Shared Poll?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        customWaitingDialogBox.showDialog("Deleting...",false);
                        deletePoll(pollId);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {

                    }
                });

        builder.create().show();
    }

    private void deletePoll(String pollId) {
       pollManager.deleteSharedPoll(pollId, new PollManager.onDeleteListner() {
           @Override
           public void onDeleted() {
               customWaitingDialogBox.hideDialog();
               Toast.makeText(SharedPolls.this,"Successfully Deleted", Toast.LENGTH_SHORT).show();
           }

           @Override
           public void ondeleteFailiure() {
                customWaitingDialogBox.hideDialog();
               Toast.makeText(SharedPolls.this,"Failed", Toast.LENGTH_SHORT).show();

           }

           @Override
           public void onNetworkFaliure() {
               DialogUtils.showNetworkErrorDialog(SharedPolls.this);
           }
       });
    }
}