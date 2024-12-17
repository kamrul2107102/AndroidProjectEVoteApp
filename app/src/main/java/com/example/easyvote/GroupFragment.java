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

import com.example.easyvote.adaptors.GroupListAdaptor;

import java.util.ArrayList;

public class GroupFragment extends Fragment {

    ListView listView;
    TextView emptyGrouptext;
    View Create_group_btn, join_btn;
    ProgressBar progressBar;

    GroupManager groupManager;

    CustomWaitingDialogBox customWaitingDialogBox;



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        PollUtil pollUtil = new PollUtil(getContext());
        pollUtil.updateAllGroupPollStatus(new PollUtil.OnExpireListner() {
            @Override
            public void onExpire() {

            }

            @Override
            public void onActive() {

            }

            @Override
            public void onError() {
                Toast.makeText(getContext(),"Loading failed. Please Refresh the Page", Toast.LENGTH_SHORT).show();
            }
        });

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
         View view =inflater.inflate(R.layout.fragment_group, container, false);

         Create_group_btn = view.findViewById(R.id.create_group);
         join_btn = view.findViewById(R.id.join_btn);

         listView = (ListView) view.findViewById(R.id.group_list);
         listView.setVisibility(View.GONE);
         emptyGrouptext = (TextView) view.findViewById(R.id.emptt_groups);
         progressBar = (ProgressBar) view.findViewById(R.id.progress_bar);
         progressBar.setVisibility(View.VISIBLE);
         groupManager = new GroupManager(getContext());
         customWaitingDialogBox = new CustomWaitingDialogBox(getContext());

        //loading groups
        groupManager.getUserGroups(new GroupManager.OnGroupsLoadedListener() {
            @Override
            public void onGroupsLoaded(ArrayList<String> groups, ArrayList<String> documentId, ArrayList<String> adminName) {
                try {
                    progressBar.setVisibility(View.GONE);
                    emptyGrouptext.setVisibility(View.GONE);
                    listView.setVisibility(View.VISIBLE);

                    GroupListAdaptor groupListAdaptor = new GroupListAdaptor(getContext(), groups, adminName);
                    listView.setAdapter(groupListAdaptor);

                    // group list view click handling
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                            Intent intent = new Intent(getContext(), GroupInside.class);
                            intent.putExtra("DocumentId", documentId.get(position));
                            intent.putExtra("GroupName", groups.get(position));
                            startActivity(intent);
                        }
                    });

                } catch (IllegalArgumentException e) {
                    // Handle the exception
                    progressBar.setVisibility(View.GONE);
                    listView.setVisibility(View.GONE);
                    emptyGrouptext.setText("Loading Failed. Refresh the page");
                    emptyGrouptext.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onGroupsLoadFailed() {
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                emptyGrouptext.setText("Loading Failed");
                emptyGrouptext.setVisibility(View.VISIBLE);
            }

            @Override
            public void emptyGroups() {
                progressBar.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                emptyGrouptext.setVisibility(View.VISIBLE);
            }
        });




        // create groups
        Create_group_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String joinButtonText = "Create";
                String editTextHint = "Group Name";
                openEditTextPopup(joinButtonText,editTextHint, true);

            }
        });




        join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String joinButtonText = "Join";
                String editTextHint = "Enter Group ID";
                openEditTextPopup(joinButtonText,editTextHint,false);

            }
        });

         return view;
    }


    private void openEditTextPopup(String actionbuttonText, String editTextHint, boolean actionType) {
        // Create a Dialog

        // actionType = true is for create group and false for join groups

        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.vote_btn_dialog_box_layout);
        dialog.setCancelable(false);

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        // Find the views in the popup layout
        final EditText editText = dialog.findViewById(R.id.popup_edit_text);
        Button actionBtn = dialog.findViewById(R.id.popup_button);
        Button cancelBtn = dialog.findViewById(R.id.cancel_btn_vote_dialogbox);


        actionBtn.setText(actionbuttonText);
        editText.setHint(editTextHint);


        actionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String inputText  = editText.getText().toString();
                if (TextUtils.isEmpty(inputText)) {
                    editText.setError("Empty Text");
                    return;
                }

                if(!actionType){  // join group
                    joinGroup(inputText);
                }

                if(actionType){ // create groups
                    createGroup(inputText);
                }
                dialog.dismiss();




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

    private void joinGroup(String joinId){

        customWaitingDialogBox.showDialog("Joining",false);

        groupManager.joinGroup(joinId, new GroupManager.OnGroupJoinListner(){


            @Override
            public void OnGroupJoined() {
                Toast.makeText(getContext(),"joined",Toast.LENGTH_SHORT).show();
                customWaitingDialogBox.hideDialog();
            }
            @Override
            public void OnGroupJoinFailed() {
                Toast.makeText(getContext(),"Group joining failed",Toast.LENGTH_SHORT).show();
                customWaitingDialogBox.hideDialog();

            }

            @Override
            public void OnGroupNotExists() {
                Toast.makeText(getContext(),"Group Does not exists ",Toast.LENGTH_SHORT).show();
                customWaitingDialogBox.hideDialog();

            }

            @Override
            public void OnAlradyGroupMember() {
                Toast.makeText(getContext(),"Alrady a member of the group ",Toast.LENGTH_SHORT).show();
                customWaitingDialogBox.hideDialog();
            }

            @Override
            public void OnNetworkFaliure() {
                customWaitingDialogBox.hideDialog();
                DialogUtils.showNetworkErrorDialog(getContext());
            }
        });

    }

    private void createGroup(String groupName){

        customWaitingDialogBox.showDialog("Creating...",false);
        groupManager.createGroup(groupName, new GroupManager.OnGroupCreationListner() {
            @Override
            public void OnGroupCreated() {
                Toast.makeText(getContext(),"Group Created",Toast.LENGTH_SHORT).show();
                customWaitingDialogBox.hideDialog();

            }

            @Override
            public void OnGroupCreationFailed() {
                Toast.makeText(getContext(),"Group creation Failed",Toast.LENGTH_SHORT).show();
                customWaitingDialogBox.hideDialog();
            }

            @Override
            public void OnNetworkFaliure() {
                customWaitingDialogBox.hideDialog();
                DialogUtils.showNetworkErrorDialog(getContext());
            }
        });

    }

}