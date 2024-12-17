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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class PollUtil {
    Context context;
    FirebaseFirestore firestore;
    FirebaseAuth fAuth;
    String currentUID;
    public PollUtil( Context context) {
        firestore = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        currentUID = fAuth.getUid();
        this.context = context;
    }

    private final Executor executor = Executors.newSingleThreadExecutor();



    public void updatePollStatus(OnExpireListner listner){


        CollectionReference UserpollColRef = firestore.collection("User_participation").document(currentUID).collection("polls");

        UserpollColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().isEmpty()){

                    }else {


                        for (QueryDocumentSnapshot document : task.getResult()) {

                            String documentId = document.getId();
                            DocumentReference statusRef = UserpollColRef.document(documentId);

                           checkExpire(documentId, new DateTimeListener() {
                               @Override
                               public void onDateBeforeCurrent() {
                                   listner.onActive();

                               }
                               @Override
                               public void onDateAfterCurrent() {
                                    statusRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                String status = task.getResult().getString("Status");
                                                if(status.equals("Active")){
                                                    statusRef.update("Status","Closed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                listner.onExpire();
                                                            }else {
                                                                listner.onError();
                                                            }
                                                        }
                                                    });
                                                }else{

                                                }
                                            }else{
                                                listner.onError();
                                            }
                                        }
                                    });

                               }

                               @Override
                               public void onDateComparisonError() {
                                    listner.onError();
                               }
                           });


                        }


                    }

                }


            }
        });

    }




    public void updateSingleGroupPollStatus(String groupId, OnExpireListner listner){

        CollectionReference groupPollRef= firestore.collection("Groups").document(groupId).collection("polls");

        groupPollRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().isEmpty()){

                    }else{
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();
                            DocumentReference statusRef = groupPollRef.document(documentId);
                            checkExpire(documentId, new DateTimeListener() {
                                @Override
                                public void onDateBeforeCurrent() {
                                    listner.onActive();
                                }

                                @Override
                                public void onDateAfterCurrent() {
                                    statusRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                String status = task.getResult().getString("Status");
                                                if(status.equals("Active")){
                                                    statusRef.update("Status","Closed").addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            if(task.isSuccessful()){
                                                                listner.onExpire();
                                                            }else {
                                                                listner.onError();
                                                            }
                                                        }
                                                    });
                                                }
                                            }else {
                                                listner.onError();
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onDateComparisonError() {

                                }
                            });
                        }

                        }
                }else{
                    listner.onError();
                }
            }
        });
    }

 //   public void sharePoll(String pollId, String user)


    public void updateAllGroupPollStatus(OnExpireListner listner2){
        CollectionReference UserGroupColRef = firestore.collection("User_participation").document(currentUID).collection("Groups");

        UserGroupColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().isEmpty()){

                    }else {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String groupId = document.getId();

                            updateSingleGroupPollStatus(groupId, new OnExpireListner() {
                                @Override
                                public void onExpire() {
                                    listner2.onExpire();
                                }

                                @Override
                                public void onActive() {
                                    listner2.onActive();
                                }

                                @Override
                                public void onError() {
                                    listner2.onError();
                                }
                            });
                        }
                    }


                }else{

                }
            }
        });

    }

    public void checkExpire(String pollId, DateTimeListener dateTimeListener) {
        FirebaseFirestore fstore = FirebaseFirestore.getInstance();
        DocumentReference pollDocRef = fstore.collection("polls").document(pollId);

        pollDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    String dateTimeString = documentSnapshot.getString("Close");
                    executor.execute(new Runnable() {
                        @Override
                        public void run() {
                            performComparison(dateTimeString, dateTimeListener);
                        }
                    });
                }
            }
        });
    }

    private static void performComparison(String dateTimeString, DateTimeListener dateTimeListener) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
        try {
            Date date = dateFormat.parse(dateTimeString);
            Calendar storedDateTime = Calendar.getInstance();
            storedDateTime.setTime(date);
            Calendar currentDateTime = Calendar.getInstance();

            if (currentDateTime.before(storedDateTime)) {
                dateTimeListener.onDateBeforeCurrent();
            } else {
                dateTimeListener.onDateAfterCurrent();
            }
        } catch (ParseException e) {
            e.printStackTrace();
            dateTimeListener.onDateComparisonError();
        }
    }


    private static void closeToExpire(String expireDateTime,onCloseToExpireListner listner){
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm", Locale.getDefault());
        try {
            Date date = dateFormat.parse(expireDateTime);
            Calendar storedDateTime = Calendar.getInstance();
            storedDateTime.setTime(date);
            Calendar currentDateTime = Calendar.getInstance();


            long timeDifference = storedDateTime.getTimeInMillis() - currentDateTime.getTimeInMillis();

            // Check if the time difference is less than one hour
            if (timeDifference < 0) {
                listner.onExpired();
            } else if (timeDifference <= 60 * 60 * 1000 && timeDifference >= 60 * 30 * 1000) {
                listner.onLessThanOneHour();
            } else {
                listner.onMoreThanOneHour();
            }

        }catch (Exception e){
            listner.onError();
        }
    }
    public void sharePoll(String userId, String pollId, String description, onShareListner listner){


        String fieldName = "userID";
        String senderId = currentUID;
        firestore.collection("users").whereEqualTo(fieldName,userId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);

                        String userDocID = document.getId();

                        DocumentReference shareDocRef = firestore.collection("User_participation").document(userDocID).collection("Share").document(pollId);
                        Map<String, Object> pollData = new HashMap<>();
                        pollData.put("description", description);
                        pollData.put("Seen", false);
                        pollData.put("SenderId",senderId);

                        shareDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    if(task.getResult().exists()){
                                        listner.onAlreadyShared();
                                    }else{
                                        shareDocRef.set(pollData).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    listner.onSuccessfulShare();
                                                }else{
                                                    listner.onShareFailed();
                                                }
                                            }
                                        });
                                    }
                                }else{
                                    listner.onShareFailed();
                                }
                            }
                        });


                    }else {
                        listner.onUsernNotExists();
                    }

                }else{
                    listner.onShareFailed();

                }
            }
        });


    }

    public void sharedPollClosingNotify(onSharedPollExpireListner listner){
        CollectionReference shareColRef = firestore.collection("User_participation").document(currentUID).collection("Share");

        shareColRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().isEmpty()){
                        listner.onEmptyShare();
                    }else {


                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String documentId = document.getId();
                            Map<String, Object> data = document.getData();
                          //  String Status = (String) data.get("Status");
                            String description = (String) data.get("description");

                            DocumentReference pollDocRef = firestore.collection("polls").document(documentId);

                            pollDocRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if(task.isSuccessful()){
                                        DocumentSnapshot documentSnapshot = task.getResult();
                                        String cloingDate = documentSnapshot.getString("Close");

                                        closeToExpire(cloingDate, new onCloseToExpireListner() {
                                            @Override
                                            public void onLessThanOneHour() {
                                                listner.onCloseToExpire(description);
                                            }
                                            @Override
                                            public void onMoreThanOneHour() {
                                                listner.onNotExpire();
                                            }
                                            @Override
                                            public void onExpired() {
                                                listner.onExpired();
                                            }
                                            @Override
                                            public void onError() {
                                                listner.onFailed();

                                            }
                                        });

                                    }else{
                                        listner.onFailed();
                                    }
                                }
                            });

                        }
                     }
                }else {
                    listner.onFailed();
                }
        }
        });

    }

    public interface DateTimeListener {
        void onDateBeforeCurrent();

        void onDateAfterCurrent();

        void onDateComparisonError();
    }

    public interface OnExpireListner{
        void onExpire();
        void onActive();
        void onError();
    }

    public interface onShareListner{
        void onSuccessfulShare();
        void onAlreadyShared();
        void onShareFailed();
        void onUsernNotExists();
    }

    public interface onSharedPollExpireListner{
        void onCloseToExpire(String description);
        void onExpired();
        void onNotExpire();
        void onEmptyShare();
        void onFailed();
    }

    public interface onCloseToExpireListner{
        void onLessThanOneHour();
        void onMoreThanOneHour();
        void onExpired();
        void onError();
    }
}
