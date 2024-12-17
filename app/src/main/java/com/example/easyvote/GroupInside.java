package com.example.easyvote;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.easyvote.adaptors.GroupPollListAdaptor;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class GroupInside extends AppCompatActivity {


    TextView textView;
    View createbtn;
    ListView pollList;
    FirebaseAuth fAuth;
    String currentUserID;
    Toolbar toolbar;

    boolean admin = false;
    String GroupId;
    String groupname;
    GroupManager groupManager;
    CustomWaitingDialogBox customWaitingDialogBox;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_inside);


        Intent intent = getIntent();
        GroupId = intent.getStringExtra("DocumentId");
        groupname = intent.getStringExtra("GroupName");
        groupManager =  new GroupManager(GroupInside.this);

        customWaitingDialogBox = new CustomWaitingDialogBox(GroupInside.this);
        customWaitingDialogBox.showDialog("Loading...", false);
        createbtn = findViewById(R.id.create_group_poll);
        fAuth = FirebaseAuth.getInstance();
        pollList = findViewById(R.id.poll_list);
        textView=findViewById(R.id.empty_group_polls_text);
        toolbar = findViewById(R.id.group_toolbar);
        currentUserID = fAuth.getUid();
        setSupportActionBar(toolbar);
        toolbar.setTitle(groupname);
        toolbar.setSubtitle("ID : "+GroupId);




        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });


        GroupPollManager groupPollManager = new GroupPollManager(GroupInside.this);
        groupPollManager.getGroupPolls(GroupId, new GroupPollManager.onPollLoadListner() {




            @Override
            public void onPollLoaded(ArrayList<String> pollDescription, ArrayList<String> pollID, ArrayList<String> closingDate, ArrayList<Boolean> status, ArrayList<String> creaters) {

                GroupPollListAdaptor groupPollListAdaptor = new GroupPollListAdaptor(closingDate,pollDescription,status,creaters,GroupInside.this);
                pollList.setAdapter(groupPollListAdaptor);
                groupPollListAdaptor.notifyDataSetChanged();


                pollList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                        String clickedPollId = pollID.get(position);
                        groupPollManager.pollAdminMemberDirect(clickedPollId, GroupId,new GroupPollManager.OnDrectionListner() {
                            @Override
                            public void OnAdminDirection() {
                                Intent intent = new Intent(GroupInside.this, PollResultsAdmin.class);
                                intent.putExtra("PollID", clickedPollId);
                                intent.putExtra("groupID",GroupId);
                                intent.putExtra("Status",status.get(position));
                                startActivity(intent);
                            }

                            @Override
                            public void OnMemberDirection() {
                                Intent intent = new Intent(GroupInside.this, VotingPage.class);
                                intent.putExtra("id", clickedPollId);
                                startActivity(intent);
                            }

                            @Override
                            public void OnDirectionFailed() {
                                Toast.makeText(GroupInside.this,"Poll Open Failed",Toast.LENGTH_SHORT).show();
                            }
                        });

                    }
                });
            }
            @Override
            public void onPollLoadingFailed() {

            }

            @Override
            public void onEmptyPolls() {

            }


        });


        createbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent(GroupInside.this, CreatePolls.class);
                intent1.putExtra("SOURCE_ACTIVITY","group");
                intent1.putExtra("GroupID",GroupId);
                startActivity(intent1);
            }
        });




    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu,menu);


        groupManager.groupAdminDirection(GroupId, new GroupPollManager.OnDrectionListner() {
            @Override
            public void OnAdminDirection() {
                MenuItem menuItem = menu.findItem(R.id.group_participant_action);
                if (menuItem != null) {
                    menuItem.setIcon(R.drawable.delete);
                    menuItem.setTitle("Delete");
                }
                admin = true;
                customWaitingDialogBox.hideDialog();

            }

            @Override
            public void OnMemberDirection() {
                MenuItem menuItem = menu.findItem(R.id.group_participant_action);
                if (menuItem != null) {
                    menuItem.setIcon(R.drawable.logout);
                    menuItem.setTitle("Leave Group");
                }
                admin = false;
                customWaitingDialogBox.hideDialog();
            }

            @Override
            public void OnDirectionFailed() {
                Toast.makeText(GroupInside.this,"Loading Failed. Please Reload the Page",Toast.LENGTH_SHORT).show();
                customWaitingDialogBox.hideDialog();
            }
        });




        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();
        switch (id){
            case R.id.group_participant_action:
                if(admin){
                    openDeleteDialogBox();
                }else{
                    openLeaveDialogBox();
                }
                break;
            case R.id.share_group_id:
                shareGroupInfo(GroupId,groupname);
                break;
        }


        return true;
    }


    private void deleteGroup(){
        groupManager.AdminMemberDirect(GroupId, new GroupPollManager.OnDrectionListner() {
            @Override
            public void OnAdminDirection() {
                groupManager.deleteGroups(GroupId, new GroupManager.OnGroupDeleteListner() {
                    @Override
                    public void onDeleted() {
                        Toast.makeText(GroupInside.this,"Succesfully deleted", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(GroupInside.this,HomePage.class);
                        startActivity(intent);
                    }

                    @Override
                    public void onDeleteFailed() {
                        Toast.makeText(GroupInside.this,"Delete failed", Toast.LENGTH_SHORT).show();

                    }
                });
            }

            @Override
            public void OnMemberDirection() {
                Toast.makeText(GroupInside.this,"Delete failed", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void OnDirectionFailed() {
                Toast.makeText(GroupInside.this,"try again",Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void shareGroupInfo(String groupId, String groupName) {
        String message = "Join our group on EasyVote!\n\nGroup ID: " + groupId + "\nGroup Name: " + groupName;

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(shareIntent, "Share Group Info"));
    }

    private void openDeleteDialogBox(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(GroupInside.this);
        builder1.setTitle("Confirm Action")
                .setMessage("Are you sure want to delete this Group")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        deleteGroup();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }

    private void openLeaveDialogBox(){
        AlertDialog.Builder builder1 = new AlertDialog.Builder(GroupInside.this);
        builder1.setTitle("Confirm Action")
                .setMessage("Are you sure want to leave")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        groupManager.leaveGroup(GroupId, new GroupManager.onGroupLeaveListner() {
                            @Override
                            public void onLeaveSuccess() {
                                Toast.makeText(GroupInside.this,"Success",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(GroupInside.this,HomePage.class);
                                startActivity(intent);
                            }

                            @Override
                            public void onLeaveFailed() {
                                Toast.makeText(GroupInside.this,"Failed",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();

    }



}
