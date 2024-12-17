package com.example.easyvote;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Transaction;

import org.checkerframework.checker.nullness.qual.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PollManager {

    Context context;
    FirebaseAuth fAuth;
    FirebaseFirestore fstore;
    String currentUserID;
    CollectionReference pollColRef;
    String gender;
    long age;

    public PollManager(Context context){

        this.context = context;
        fAuth = FirebaseAuth.getInstance();
        fstore = FirebaseFirestore.getInstance();
        currentUserID = fAuth.getUid();
        pollColRef = fstore.collection("poll");
    }

    // method for load the poll for the voting page
    public void loadPoll(String pollID,onPollLoadedListner listner){

        DocumentReference documentReference = fstore.collection("polls").document(pollID);
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (documentSnapshot.exists()) {

                        String description = documentSnapshot.getString("description");
                        int numberOfOptions = Integer.parseInt(documentSnapshot.getString("number of options"));
                        ArrayList<String> options = new ArrayList<>();

                        // Retrieve options from individual fields
                        for (int i = 1; i <=numberOfOptions; i++) {
                            String option = documentSnapshot.getString("option" + i);
                            if (option != null) {
                                options.add(option);
                            }}
                        listner.onPollLoaded(options,description);


                    } else {
                        listner.pollNotExist();
                    }
                } else {
                    listner.onPollLoadingFailed();
                }
            }
        });

    }


    public void loadPollVoting(String pollID,onPollLoadedListner listner){
        DocumentReference documentReference = fstore.collection("polls").document(pollID);
        DocumentReference votersDocRef = documentReference.collection("Voters").document(currentUserID);

        votersDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        listner.ReturnVoter();
                    }
                    else{

                        loadPoll(pollID, listner);

                    }
                }else {
                    listner.onPollLoadingFailed();
                }
            }
        });
    }


    protected void createPoll(Map<String, Object> data, Map<String,Object> results, onPollCreateListner listner){


        String CustomID = CustomIdGenerator.generateCustomId(8);
        DocumentReference newDocRef = fstore.collection("polls").document(CustomID);
        DocumentReference votesdocRef = newDocRef.collection("Results").document("Votes");

        DocumentReference userParticipantDocRef = fstore.collection("User_participation")
                .document(currentUserID).collection("polls").document(CustomID);
        String description1 = data.get("description").toString();
        String closingDate = data.get("Close").toString();
        Map<String, Object> description = new HashMap<>();
        description.put("description",description1);
        description.put("Status","Active");
        description.put("Close",closingDate);
        data.put("AdminID",currentUserID);
        newDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){

                        createPoll(data,results,listner);
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
                                                userParticipantDocRef.set(description).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    }


    protected void addVote(String pollID, int selectedOption, onVoteListner listner){
        DocumentReference newDocRef = fstore.collection("polls").document(pollID);
        DocumentReference votesdocRef = newDocRef.collection("Results").document("Votes");
        DocumentReference votersDocRef = newDocRef.collection("Voters").document(currentUserID);
        DocumentReference userDocRef = fstore.collection("users").document(currentUserID);


        userDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(document.exists()){
                        gender = document.getString("gender");
                        age = document.getLong("age");

                    }else {
                        listner.onVoteFaliure();
                    }
                }

            }
        });

        Map<String , Object> userVotes= new HashMap<>();
        userVotes.put("option ", "option"+selectedOption);

        //  transaction to update the vote document (does not occur conflict when multiple users voting at the same time.)
        fstore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                DocumentSnapshot snapshot = transaction.get(votesdocRef);

                // Check if the document exists
                if (snapshot.exists()) {
                    // Update the specific option's vote count
                    Map<String, Object> data = snapshot.getData();
                    if (data != null) {
                        Object VoteCountObj = data.get("option"+selectedOption +" "+gender);
                        int currentVoteCount = ((Number) VoteCountObj).intValue();
                        transaction.update(votesdocRef, "option"+selectedOption+ " "+gender, currentVoteCount + 1);
                        transaction.update(votesdocRef, "age", FieldValue.arrayUnion(age));
                    }else{

                        listner.onVoteFaliure();
                    }
                }else{
                    Log.d("TAG", "Document doesn't exist");
                    listner.onVoteFaliure();
                }
                return null;
            }
        }).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    votersDocRef.set(userVotes).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                listner.onVoteSuccessful();
                            }else {
                                listner.onVoteFaliure();
                            }
                        }
                    });

                }else{
                    Log.d("TAG", "Vote transaction (update) failed");
                    listner.onVoteFaliure();
                }
            }
        });
    }



    public void getUserPolls(onUserPollLoadListner listner){



        ArrayList<String> descriptionActivePolls = new ArrayList<String>();
        ArrayList<String> documentidActive = new ArrayList<String>();
        ArrayList<Boolean> status = new ArrayList<>();
        ArrayList<String> closingDatesActive = new ArrayList<String>();
        CollectionReference UserPollColRef = fstore.collection("User_participation").document(currentUserID).collection("polls");
        UserPollColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().isEmpty()){
                        listner.onEmptyPolls();
                    }else {
                        // int totalDocuments = task.getResult().size();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Accessing the user polls
                            String documentId = document.getId();
                            Map<String, Object> data = document.getData();
                            String Status = (String) data.get("Status");
                            String description = (String) data.get("description");
                            String closingDate = (String) data.get("Close");
                            if(Status != null){
                                if(Status.equals("Active")) {
                                    descriptionActivePolls.add(description);
                                    documentidActive.add(documentId);
                                    String dueDate = TimeDifferenceCalculator.calculateTimeDifference(closingDate);
                                    closingDatesActive.add(dueDate);
                                    status.add(true);
                                }else{
                                    descriptionActivePolls.add(description);
                                    documentidActive.add(documentId);
                                    closingDatesActive.add("Closed");
                                    status.add(false);

                                }
                            }

                        }

                        listner.onPollLoaded(descriptionActivePolls, documentidActive,closingDatesActive,status);

                    }

                }
                else{
                    listner.onPollLoadingFailed();
                }
            }
        });

    }



    public void getPollResults(String pollId, onPollResultListner listner){
        DocumentReference pollDocRef = fstore.collection("polls").document(pollId);
        DocumentReference votesDocRef = pollDocRef.collection("Results").document("Votes");

        pollDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot documentSnapshot = task.getResult();
                    int numberOfOptions = Integer.parseInt(documentSnapshot.getString("number of options"));
                    votesDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                DocumentSnapshot results = task.getResult();

                                ArrayList<Long> male = new ArrayList<>();
                                ArrayList<Long> female = new ArrayList<>();
                                ArrayList<Long> ages = new ArrayList<>();

                                List<Long> age = (List<Long>) results.get("age");
                                if(age != null){
                                    ages.addAll(age);

                                }

                                // Retrieve options from individual fields
                                for (int i = 1; i <=numberOfOptions; i++) {
                                    Long Male = results.getLong("option" + i+" Male");
                                    Long Female = results.getLong("option" + i+" Female");
                                    if (Male != null && Female != null) {
                                        male.add(Male);
                                        female.add(Female);
                                    }else{
                                        Toast.makeText(context,"null",Toast.LENGTH_SHORT).show();
                                        listner.getResultFailed();
                                    }

                                }
                                listner.getResultSuccesful(male,female,ages,numberOfOptions);

                            }else{
                                //error getting the document
                                listner.getResultFailed();

                            }
                        }
                    });
                }
                else{
                    //error gettng the document or does  not exists
                    listner.getResultFailed();
                }
            }
        });

    }

    public void deleteUserPoll(String pollID ,boolean type,String groupId, onDeleteListner listner){

        if(!NetworkUtil.isNetworkAvailable(context)){
            listner.onNetworkFaliure();
            return;
        }
        DocumentReference pollDocRef = fstore.collection("polls").document(pollID);
        DocumentReference UserPollColRef = fstore.collection("User_participation").document(currentUserID).collection("polls").document(pollID);

        if(type){  // delete user polls
            UserPollColRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        pollDocRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    listner.onDeleted();
                                }else {
                                    listner.ondeleteFailiure();
                                }
                            }
                        });
                    }else{
                        listner.ondeleteFailiure();
                    }
                }
            });
        }
        else{ // delete group polls
            DocumentReference groupPollRef = fstore.collection("Groups").document(groupId).collection("polls").document(pollID);

            groupPollRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        pollDocRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    listner.onDeleted();
                                }else{
                                    listner.ondeleteFailiure();
                                }
                            }
                        });
                    }else {
                        listner.ondeleteFailiure();
                    }
                }
            });
        }
    }



    public void closePoll(String pollID, onPollCloseListner listner){

        if(!NetworkUtil.isNetworkAvailable(context)){
            listner.onNetworkfaliure();
            return;
        }

        DocumentReference pollDocRef = fstore.collection("polls").document(pollID);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
        String formattedDate = dateFormat.format(Calendar.getInstance().getTime());

        pollDocRef.update("Close",formattedDate).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    listner.onPollClosed();

                }else{
                    listner.onPollCloseFailled();
                }
            }
        });
    }


    public void getSharedPoll(onSharedPollLoadListner listner) {
        CollectionReference sharedPollColRef = fstore.collection("User_participation").document(currentUserID).collection("Share");
        ArrayList<String> Polldescription = new ArrayList<>();
        ArrayList<String> PollId = new ArrayList<>();
        ArrayList<Boolean> status = new ArrayList<>();
        ArrayList<String> dueDate = new ArrayList<>();

        sharedPollColRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                Polldescription.clear();
                PollId.clear();
                status.clear();
                dueDate.clear();
                status.clear();
                if (e != null) {
                    listner.onPollLoadFailed();
                    return;
                }

                if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String documentId = document.getId();
                        Map<String, Object> data = document.getData();
                        Boolean Status = (Boolean) data.get("Seen");
                        String description = (String) data.get("description");
                        status.add(Status);
                        Polldescription.add(description);
                        PollId.add(documentId);

                        if (Status != null && !Status) {
                            sharedPollColRef.document(documentId).update("Seen", true)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (!task.isSuccessful()) {
                                                listner.onPollLoadFailed();
                                            }
                                        }
                                    });
                        }
                    }

                    for (String documentId : PollId) {
                        DocumentReference pollDocRef = fstore.collection("polls").document(documentId);
                        pollDocRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(@Nullable DocumentSnapshot pollDocumentSnapshot, @Nullable FirebaseFirestoreException pollException) {
                                if (pollException != null) {
                                    listner.onPollLoadFailed();
                                    return;
                                }

                                if (pollDocumentSnapshot != null && pollDocumentSnapshot.exists()) {
                                    String closeAt = pollDocumentSnapshot.getString("Close");
                                    String dueDates = TimeDifferenceCalculator.calculateTimeDifference(closeAt);
                                    dueDate.add(dueDates);
                                    if (PollId.size() == dueDate.size()) {
                                        listner.onPollLoaded(Polldescription, PollId, dueDate, status);
                                    }
                                } else {
                                    listner.onPollLoadFailed();
                                }
                            }
                        });
                    }
                } else {
                    listner.onEmptyPolls();
                }
            }
        });
    }

    public void deleteSharedPoll(String pollId, onDeleteListner listner){

        if(! NetworkUtil.isNetworkAvailable(context)){
            listner.onNetworkFaliure();
            return;
        }

        DocumentReference sharedPolldocRef = fstore.collection("User_participation").document(currentUserID).collection("Share").document(pollId);
        sharedPolldocRef.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    listner.onDeleted();
                }else{
                    listner.ondeleteFailiure();
                }
            }
        });
    }


    public interface onPollLoadedListner{
        void onPollLoaded(ArrayList<String> options, String description);
        void onPollLoadingFailed();

        void pollNotExist();
        void ReturnVoter();
    }

    public interface onPollCreateListner{
        void onPollCreated(String ID);
        void onPollCreationFaliure();
    }

    public interface onVoteListner{
        void onVoteSuccessful();
        void onVoteFaliure();
    }

    public interface onUserPollLoadListner{
        void onPollLoaded(ArrayList<String> ActivePollDescription,
                          ArrayList<String> ActivePollId,
                          ArrayList<String> dueDates,
                          ArrayList<Boolean> status);
        void onPollLoadingFailed();
        void onEmptyPolls();

    }

    public interface onPollResultListner{
        void getResultSuccesful(ArrayList<Long> resultsMale,ArrayList<Long> resultsFemale, ArrayList<Long> ages, int numberOfOptions);
        void getResultFailed();
    }

    public interface onDeleteListner{
        void onDeleted();
        void ondeleteFailiure();
        void onNetworkFaliure();
    }

    public interface onPollCloseListner{
        void onPollClosed();
        void onPollCloseFailled();
        void onNetworkfaliure();
    }

    public interface onSharedPollLoadListner{
        void onPollLoaded(ArrayList<String> pollDescription, ArrayList<String> pollID, ArrayList<String> closingDate, ArrayList<Boolean> status );
        void onPollLoadFailed();
        void onEmptyPolls();
    }
}



