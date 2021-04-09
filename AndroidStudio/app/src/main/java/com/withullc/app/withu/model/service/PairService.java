package com.withullc.app.withu.model.service;

import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.withullc.app.withu.model.dataObjects.Pair;
import com.withullc.app.withu.model.dataObjects.User;
import com.withullc.app.withu.model.dataObjects.Walk;
import com.withullc.app.withu.model.dataObjects.Walker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Service class for accessing and modifying the pairs table in the database
 */

public class PairService {
    private static final PairService PAIR_REF = new PairService();
    private DatabaseReference pairRef;
    private DatabaseReference userRef;
    private UserService userService = UserService.getInstance();

    private PairService() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        pairRef = dbRef.child("pairs");
        userRef = dbRef.child("users");
    }
    public static PairService getInstance() {
        return PAIR_REF;
    }

    public interface PairHandler {
        void paired(User requester, User pairedUser);
        void candidateFound(User candidate);
    }

    private void createPair(Pair pair){
        pairRef.push().setValue(pair);
    }


//    private void findClosestWalker (String currUserID) {
//
//        userService.retrieveUser(currUserID, new UserService.RetrievalHandler() {
//            @Override
//            public void retrieved(User user) {
//                if (user != null) {
//                    Log.d("Retrieved", "User retrieved");
//
//
//                    userRef.orderByChild("location").startAt(user.getLocation().get("longitude") + .01).limitToFirst(1).addValueEventListener(new ValueEventListener() {
//                        @Override
//                        public void onDataChange(DataSnapshot dataSnapshot) {
//
//
//                            //DATASNAPSHOT.GETVALUE() is a HASHMAP of STRING, HASHMAP<STRING, USER>>
//                            Log.d("FOUND USER", dataSnapshot.getKey());
//                            Log.d("FOUND USER", dataSnapshot.toString());
//                        }
//
//                        @Override
//                        public void onCancelled(DatabaseError databaseError) {
//
//                        }
//
//                    });
//                } else {
//                    Log.d("RetrieveFail", "Failed to retrieve user.");
//                }
//            }
//        });
//
//    }

    private void findPotentialPartners(String uid, User requester, final PairHandler handler) {

        List<User> potentialWalkers = new ArrayList<>();

        // Create a new Pair in the pairs table containing just the requesting Walker's id
        Pair unfinishedPair = new Pair(uid, null, null);
        createPair(unfinishedPair);

        // Query for walkers who are not yet paired and of the opposite sex
        final String oppoSex = requester.getGender().equalsIgnoreCase("Male") ? "Female" : "Male";
        Query soloWalkersQuery = userRef.orderByChild("isActive").equalTo(true);
        soloWalkersQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot dbUser : dataSnapshot.getChildren()) {
                    User potentialCandidate = dbUser.getValue(User.class);
                    if(potentialCandidate.getGender().equalsIgnoreCase(oppoSex)) {
                        handler.candidateFound(potentialCandidate);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}