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

public class GroupPollManager {


    Context context;
    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    String currentUserID;

    public GroupPollManager(Context context){
        this.context = context;
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        currentUserID = fAuth.getUid();
    }

    protected void createPoll(Map<String, Object> data, Map<String,Object> results, String groupID, PollManager.onPollCreateListner listner){


        String CustomID = CustomIdGenerator.generateCustomId(8);
        DocumentReference newDocRef = fstore.collection("polls").document(CustomID);
        DocumentReference votesdocRef = newDocRef.collection("Results").document("Votes");

        DocumentReference userDocRef = fstore.collection("users").document(currentUserID);
        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    String name = task.getResult().getString("username").toString();

                    DocumentReference groupPollRef = fstore.collection("Groups")
                            .document(groupID).collection("polls").document(CustomID);
                    String description1 = data.get("description").toString();
                    String closingDate = data.get("Close").toString();
                    Map<String, Object> description = new HashMap<>();
                    description.put("description",description1);
                    description.put("Status","Active");
                    description.put("Close",closingDate);
                    description.put("creater",name);
                    data.put("AdminID",currentUserID);
                    newDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot document = task.getResult();
                                if(document.exists()){

                                    createPoll(data,results,groupID,listner);
                                }
                                else{
                                    newDocRef.set(data).addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful()){
                                                votesdocRef.set(results).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if(task.isSuccessful()){
                                                            groupPollRef.set(description).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    if (task.isSuccessful()){
                                                                        listner.onPollCreated(CustomID);

                                                                    }else {
                                                                        listner.onPollCreationFaliure();
                                                                    }
                                                                }
                                                            });
                                                        }
                                                        else {
                                                            listner.onPollCreationFaliure();
                                                        }
                                                    }
                                                });


                                            }else {
                                                listner.onPollCreationFaliure();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });

                }else{
                    listner.onPollCreationFaliure();
                }
            }
        });



    }


    public void getGroupPolls(String GroupId, onPollLoadListner listner) {

        ArrayList<String> descriptionActivePolls = new ArrayList<String>();
        ArrayList<String> documentidActive = new ArrayList<String>();
        ArrayList<String> closingDatesActive = new ArrayList<>();
        ArrayList<Boolean> status = new ArrayList<>();
        ArrayList<String> creaters = new ArrayList<>();

        CollectionReference GroupPollColRef = fstore.collection("Groups").document(GroupId).collection("polls");
        GroupPollColRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                if (e != null) {
                    listner.onPollLoadingFailed();
                    return;
                }
                if (queryDocumentSnapshots != null) {
                    descriptionActivePolls.clear();
                    documentidActive.clear();
                    closingDatesActive.clear();
                    status.clear();
                    creaters.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // Accessing the user polls
                        String documentId = document.getId();
                        Map<String, Object> data = document.getData();
                        String Status = (String) data.get("Status");
                        String description = (String) data.get("description");
                        String closingDate = data.get("Close").toString();
                        String creater = data.get("creater").toString();
                        descriptionActivePolls.add(description);
                        documentidActive.add(documentId);
                        creaters.add(creater);
                        if (Status != null) {
                            if (Status.equals("Active")) {
                                String dueDate = TimeDifferenceCalculator.calculateTimeDifference(closingDate);
                                closingDatesActive.add(dueDate);
                                status.add(true);
                            } else {
                                closingDatesActive.add("Closed");
                                status.add(false);
                            }
                        }
                    }
                    listner.onPollLoaded(descriptionActivePolls, documentidActive, closingDatesActive, status, creaters);
                }else{
                    listner.onEmptyPolls();
                }
            }
        });
    }


    public void pollAdminMemberDirect(String pollId, String groupId, OnDrectionListner listner){

        DocumentReference pollDocRef = fstore.collection("polls").document(pollId);
        DocumentReference groupDocRef = fstore.collection("Groups").document(groupId);
        pollDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String pollAdminId = documentSnapshot.getString("AdminID");

                    groupDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                String adminId = task.getResult().get("AdminID").toString();
                                if(adminId.equals(currentUserID) || pollAdminId.equals(currentUserID) ){
                                    listner.OnAdminDirection();

                                } else  {
                                    listner.OnMemberDirection();
                                }
                            }else{
                                listner.OnDirectionFailed();
                            }
                        }
                    });




                }else{
                    listner.OnDirectionFailed();
                }
            }
        });


    }




    interface OnDrectionListner{
        void OnAdminDirection();
        void OnMemberDirection();
        void OnDirectionFailed();
    }

    interface  onPollLoadListner{
        void onPollLoaded(ArrayList<String> pollDescription, ArrayList<String> pollID, ArrayList<String> closingDate, ArrayList<Boolean> status, ArrayList<String > creaters );
       void onPollLoadingFailed();
       void onEmptyPolls();
    }



}
