package com.example.easyvote;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.easyvote.adaptors.UserPollListAdaptor;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


public class HomeFragment extends Fragment {


    TextView  lastUpdateText;
   // Button vote;
      View create, voteBtn;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    ListView listView;
    TextView emptyPolls;
    ProgressBar progressBar;





    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);


       // vote = view.findViewById(R.id.vote);
        create = view.findViewById(R.id.create_btn);
        lastUpdateText = view.findViewById(R.id.last_update_text);
        listView = view.findViewById(R.id.poll_list);
        listView.setVisibility(View.GONE);
        emptyPolls = view.findViewById(R.id.empty_polls);
        progressBar = view.findViewById(R.id.progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        voteBtn = view.findViewById(R.id.vote_btn);



        // update the pollStatus of the database
        PollUtil pollUtil = new PollUtil(getContext());
        pollUtil.updatePollStatus(new PollUtil.OnExpireListner() {
            @Override
            public void onExpire() {

            }

            @Override
            public void onActive() {

            }

            @Override
            public void onError() {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(), "Updating Error", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });



        PollManager pollManager = new PollManager(getContext());
        pollManager.getUserPolls(new PollManager.onUserPollLoadListner() {

            @Override
            public void onPollLoaded(ArrayList<String> ActivePollDescription, ArrayList<String> ActivePollId, ArrayList<String> dueDates, ArrayList<Boolean> status) {

                try{
                    UserPollListAdaptor userPollListAdaptor = new UserPollListAdaptor(getContext(),dueDates,ActivePollDescription,ActivePollId, status);
                    listView.setAdapter(userPollListAdaptor);
                    userPollListAdaptor.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);
                    String time = TimeDifferenceCalculator.getCurrentTimeFormatted();
                    lastUpdateText.setText("last update : "+time);

                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getContext(),PollResultsAdmin.class);
                            intent.putExtra("PollID",ActivePollId.get(position));
                            intent.putExtra("Status",status.get(position));
                            startActivity(intent);
                        }
                    });

                    listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
                        @Override
                        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                            return true;
                        }
                    });

                }catch (IllegalArgumentException e){
                    emptyPolls.setText("Loading Failed. Please Refresh the Page");
                    emptyPolls.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            }


            @Override
            public void onPollLoadingFailed() {
                emptyPolls.setText("Loading Failed");
                emptyPolls.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);

            }

            @Override
            public void onEmptyPolls() {
                emptyPolls.setText("You don't have any polls yet...");
                emptyPolls.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
                String time = TimeDifferenceCalculator.getCurrentTimeFormatted();
                lastUpdateText.setText("last update : "+time);
            }


        });



        voteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                openEditTextPopup();

            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(),CreatePolls.class);
                intent.putExtra("SOURCE_ACTIVITY", "single");

                startActivity(intent);
            }
        });


        return view;
    }



    private void openEditTextPopup() {
        // Create a Dialog
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.vote_btn_dialog_box_layout);
        dialog.setCancelable(false);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Find the views in the popup layout
        final EditText editText = dialog.findViewById(R.id.popup_edit_text);
        Button voteBtn = dialog.findViewById(R.id.popup_button);
        Button cancelBtn = dialog.findViewById(R.id.cancel_btn_vote_dialogbox);

        voteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String id = editText.getText().toString();
                if (TextUtils.isEmpty(id)) {
                    editText.setError("Please Enter Poll Id First");
                    return;
                }

                // Dismiss the dialog
                dialog.dismiss();

                // Start the new activity with the entered ID
                Intent intent = new Intent(getContext(), VotingPage.class);
                intent.putExtra("id", id);
                startActivity(intent);
            }
        });

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        // Show the Dialog
        dialog.show();
    }



}