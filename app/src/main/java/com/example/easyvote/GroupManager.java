package com.example.easyvote;

import android.content.Context;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GroupManager {

    private FirebaseFirestore fStore;
    private FirebaseAuth fAuth;
    private Context context;
    String username;  // used in joingroup  and create group methods


    private CollectionReference groupColRef;
    String currentUserID;
    int successCount = 0;  // this variable for ensure that all the document are fetched from the collection in the getUserGroups method

    public GroupManager(Context context) {
        this.context = context;
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentUserID = fAuth.getUid();
        groupColRef = fStore.collection("Groups");
    }

    public void createGroup(String groupName, OnGroupCreationListner listner){


        if(!NetworkUtil.isNetworkAvailable(context)){
            listner.OnNetworkFaliure();
        }


        String CustomID = CustomIdGenerator.generateCustomId(8);
        DocumentReference newDocRef = groupColRef.document(CustomID);   // newDocRef is created for each group

        Map<String, Object> groupData = new HashMap<>();
        groupData.put("Group_Name",groupName);
        groupData.put("AdminID",currentUserID);

        newDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
              if(task.isSuccessful()){
                  DocumentSnapshot document = task.getResult();
                  if(document.exists()){

                      createGroup(groupName,listner);
                  }else{

                      DocumentReference userDocRef = fStore.collection("users").document(currentUserID);

                      userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                          @Override
                          public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                              if(task.isSuccessful()){
                                  DocumentSnapshot document = task.getResult();
                                  if(document.exists()){
                                      username = document.getString("username");

                                  }else{
                                      listner.OnGroupCreationFailed();
                                  }
                              }else {
                                  listner.OnGroupCreationFailed();
                              }
                          }
                      });


                      newDocRef.set(groupData).addOnCompleteListener(new OnCompleteListener<Void>() {
                          @Override
                          public void onComplete(@NonNull Task<Void> task) {
                              if(task.isSuccessful()) {
                                  // Saving Group date

                                  DocumentReference groupDatacolRef = newDocRef.collection("members").document(currentUserID);

                                  Map<String, Object> groupUser = new HashMap<>();
                                  groupUser.put("username", username);
                                  String GroupID = newDocRef.getId();

                                  groupDatacolRef.set(groupUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                      @Override
                                      public void onComplete(@NonNull Task<Void> task) {
                                          if (task.isSuccessful()) {
                                              CollectionReference UserGroupColRef = fStore.collection("User_participation").document(currentUserID).collection("Groups");
                                              DocumentReference g_doc = UserGroupColRef.document(GroupID);
                                              Map<String, Object> userdata = new HashMap<>();
                                              userdata.put("Group_Name", groupName);
                                              g_doc.set(userdata).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                  @Override
                                                  public void onComplete(@NonNull Task<Void> task) {
                                                      if (task.isSuccessful()) {
                                                          listner.OnGroupCreated();
                                                      } else {
                                                          listner.OnGroupCreationFailed();
                                                      }
                                                  }
                                              });
                                          } else {
                                              listner.OnGroupCreationFailed();
                                          }

                                      }
                                  });


                              }else {
                                  listner.OnGroupCreationFailed();
                              }
                          }
                      });
                  }
              }else {
                  listner.OnGroupCreationFailed();
              }
            }
        });

    }


    public void joinGroup(String GroupID, OnGroupJoinListner listner) {

        if(!NetworkUtil.isNetworkAvailable(context)){
            listner.OnNetworkFaliure();
            return;
        }

        DocumentReference groupDocRef = groupColRef.document(GroupID);
        DocumentReference memberRef = groupDocRef.collection("members").document(currentUserID);
        DocumentReference userDocRef = fStore.collection("users").document(currentUserID);

        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> userTask) {
                if (userTask.isSuccessful()) {
                    DocumentSnapshot userResult = userTask.getResult();
                    if (userResult != null && userResult.exists()) {
                        username = userResult.getString("username");

                        groupDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> groupTask) {
                                if (groupTask.isSuccessful()) {
                                    DocumentSnapshot groupResult = groupTask.getResult();
                                    if (groupResult != null && groupResult.exists()) {
                                        memberRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<DocumentSnapshot> memberTask) {
                                                if (memberTask.isSuccessful()) {
                                                    DocumentSnapshot memberResult = memberTask.getResult();
                                                    if (memberResult != null && memberResult.exists()) {
                                                        listner.OnAlradyGroupMember();
                                                    } else {
                                                        String groupName = groupResult.getString("Group_Name");

                                                        Map<String, Object> newUser = new HashMap<>();
                                                        newUser.put("username", username);
                                                        memberRef.set(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> memberSetTask) {
                                                                if (memberSetTask.isSuccessful()) {
                                                                    CollectionReference UserGroupColRef = fStore.collection("User_participation")
                                                                            .document(currentUserID)
                                                                            .collection("Groups");
                                                                    DocumentReference docref = UserGroupColRef.document(GroupID);
                                                                    Map<String, Object> userData = new HashMap<>();
                                                                    userData.put("Group_Name", groupName);

                                                                    docref.set(userData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                        @Override
                                                                        public void onComplete(@NonNull Task<Void> docSetTask) {
                                                                            if (docSetTask.isSuccessful()) {
                                                                                listner.OnGroupJoined();
                                                                            } else {
                                                                                listner.OnGroupJoinFailed();
                                                                            }
                                                                        }
                                                                    });
                                                                } else {
                                                                    listner.OnGroupJoinFailed();
                                                                }
                                                            }
                                                        });
                                                    }
                                                } else {
                                                    listner.OnGroupJoinFailed();
                                                }
                                            }
                                        });
                                    } else {
                                        listner.OnGroupNotExists();
                                    }
                                } else {
                                    listner.OnGroupJoinFailed();
                                }
                            }
                        });
                    } else {
                        listner.OnGroupJoinFailed();
                    }
                } else {
                    listner.OnGroupJoinFailed();
                }
            }
        });
    }

    public void getUserGroups(final OnGroupsLoadedListener listener) {
        ArrayList<String> gNames = new ArrayList<String>();
        ArrayList<String> documentid = new ArrayList<String>();
        ArrayList<String> adminName = new ArrayList<>();
        CollectionReference userGroupColRef = fStore.collection("User_participation").document(currentUserID).collection("Groups");
        userGroupColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> userGroupTask) {
                if (userGroupTask.isSuccessful()) {
                    if (userGroupTask.getResult().isEmpty()) {
                        listener.emptyGroups();
                    }
                    int totalDocuments = userGroupTask.getResult().size();

                    for (QueryDocumentSnapshot document : userGroupTask.getResult()) {
                        // Accessing the groups and admins data
                        String documentId = document.getId();
                        Map<String, Object> data = document.getData();

                        String groupName = (String) data.get("Group_Name");
                        gNames.add(groupName);
                        documentid.add(documentId);
                        DocumentReference groupdocRef = fStore.collection("Groups").document(documentId);
                        groupdocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> groupDocTask) {
                                if (groupDocTask.isSuccessful() && groupDocTask.getResult() != null && groupDocTask.getResult().exists()) {
                                    String id = groupDocTask.getResult().getString("AdminID");
                                    DocumentReference usersdocRef = fStore.collection("users").document(id);
                                    usersdocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> userDocTask) {
                                            if (userDocTask.isSuccessful() && userDocTask.getResult() != null && userDocTask.getResult().exists()) {
                                                successCount++;
                                                adminName.add(userDocTask.getResult().getString("username"));
                                                if (successCount == totalDocuments) {
                                                    listener.onGroupsLoaded(gNames, documentid, adminName);
                                                }
                                            } else {
                                                listener.onGroupsLoadFailed();
                                            }
                                        }
                                    });
                                } else {
                                    listener.onGroupsLoadFailed();
                                }
                            }
                        });
                    }
                } else {
                    listener.onGroupsLoadFailed();
                }
            }
        });
    }




    public void AdminMemberDirect(String groupId, GroupPollManager.OnDrectionListner listner){

        DocumentReference groupdocRef = fStore.collection("Groups").document(groupId);
        groupdocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String adminId = documentSnapshot.getString("AdminID");

                    if(adminId.equals(currentUserID)){
                        listner.OnAdminDirection();

                    } else  {
                        listner.OnMemberDirection();
                    }

                }else{
                    listner.OnDirectionFailed();
                }
            }
        });


    }



    public void deleteGroups(String groupId, OnGroupDeleteListner listner){

        if(!NetworkUtil.isNetworkAvailable(context))

        if (groupId == null || groupId.isEmpty()) {
            listner.onDeleteFailed(); // Handle the case when groupId is null or empty
            return;
        }
        DocumentReference groupdocRef = fStore.collection("Groups").document(groupId);

        CollectionReference memberColRef = fStore.collection("Groups").document(groupId).collection("members");
        memberColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().isEmpty()){
                        listner.onDeleteFailed();

                    }else{
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String userId = document.getId();
                        DocumentReference usersdocRef = fStore.collection("User_participation").document(userId).collection("Groups").document(groupId);
                        usersdocRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){

                                }else {
                                    listner.onDeleteFailed();
                                }
                            }
                        });

                    }
                    groupdocRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                listner.onDeleted();
                            }else{
                                listner.onDeleteFailed();
                            }
                        }
                    });

                    }
            }else{
                listner.onDeleteFailed();}
            }
        });


    }

    public void groupAdminDirection(String groupId, GroupPollManager.OnDrectionListner listner){

        DocumentReference groupDocRef = fStore.collection("Groups").document(groupId);
        groupDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String adminId = task.getResult().get("AdminID").toString();
                    if(adminId.equals(currentUserID)){
                        listner.OnAdminDirection();
                    }else{
                        listner.OnMemberDirection();
                    }

                }else{
                    listner.OnDirectionFailed();
                }
            }
        });


    }

    public void leaveGroup(String groupId, onGroupLeaveListner listner ){
        DocumentReference groupDocRef = fStore.collection("Groups").document(groupId).collection("members").document(currentUserID);
        DocumentReference userPaticipantDocRef = fStore.collection("User_participation").document(currentUserID).collection("Groups").document(groupId);
        groupDocRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    userPaticipantDocRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                listner.onLeaveSuccess();
                            }else{
                                listner.onLeaveFailed();
                            }
                        }
                    });
                }else{
                    listner.onLeaveFailed();
                }
            }
        });

    }




    public interface OnGroupCreationListner{

        void OnGroupCreated();
        void OnGroupCreationFailed();
        void OnNetworkFaliure();

    }

    public interface OnGroupJoinListner{
        void OnGroupJoined();
        void OnGroupJoinFailed();

        void OnGroupNotExists();
        void OnAlradyGroupMember();
        void OnNetworkFaliure();
    }

    // Define an interface for the callback
    public interface OnGroupsLoadedListener {
        void onGroupsLoaded(ArrayList<String> groups, ArrayList<String> documentid, ArrayList<String> AdminName);
        void onGroupsLoadFailed();
        void emptyGroups();
    }

    public interface OnGroupDeleteListner {
        void onDeleted();
        void onDeleteFailed();
    }

    public interface onGroupLeaveListner{
        void onLeaveSuccess();
        void onLeaveFailed();
    }

}


