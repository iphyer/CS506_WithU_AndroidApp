package com.withullc.app.withu.model.service;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.withullc.app.withu.model.dataObjects.User;

import java.util.Map;

//can get rid of if we dont get that list workaround functional

/**
 * Service class for accessing and modifying the walks table in the database
 */

public class UserService {
    private static final UserService USER_REF = new UserService();
    private DatabaseReference userRef;
    private User user = new User();

    public interface RetrievalHandler {
        void retrieved(User user);
    }

    UserService() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        userRef = dbRef.child("users");
    }

    public static UserService getInstance() {
        return USER_REF;
    }

    public void createUser(User newUser, String userId) {

        Log.d("Created User", "STARTED");
        //this works as long as we dont store userID
        userRef.child(userId).setValue(newUser);
}


    // Can be used for both retrieving walkers or retrieving users
    // returns null if the user was not found by the id provided
    public void retrieveUserOnce(String userId, final RetrievalHandler handler) {

        DatabaseReference ref = userRef.child(userId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

               // Creates a user object from the data, and passes it to the data handler
                User retrievedUser = dataSnapshot.getValue(User.class);
                Log.d("NAME", retrievedUser.getName());
                handler.retrieved(retrievedUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    // Can be used for both retrieving walkers or retrieving users
    // returns null if the user was not found by the id provided
    public void retrieveUserSync(String userId, final RetrievalHandler handler) {

        DatabaseReference ref = userRef.child(userId);
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Creates a user object from the data, and passes it to the data handler
                User retrievedUser = dataSnapshot.getValue(User.class);
                Log.d("NAME", retrievedUser.getName());
                handler.retrieved(retrievedUser);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    //Update User Location
    public void updateLocation (String userId, double lat, double lng){

        userRef.child(userId).child("location").child("longitude").setValue(lng);
        userRef.child(userId).child("location").child("latitude").setValue(lat);

    }


    //Update isActive function???
    public void setActive(String userId, boolean active) {
        userRef.child(userId).child("isActive").setValue(active);
    }
}