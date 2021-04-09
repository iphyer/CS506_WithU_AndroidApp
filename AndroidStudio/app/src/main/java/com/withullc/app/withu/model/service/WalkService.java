package com.withullc.app.withu.model.service;

import android.location.Location;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.withullc.app.withu.model.dataObjects.Progress;
import com.withullc.app.withu.model.dataObjects.Walk;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Service class for accessing and modifying the walks table in the database
 */

public class WalkService {
    private static final WalkService WALK_REF = new WalkService();
    private DatabaseReference walkRef;
    private Walk walk;

    public interface RetrievalHandler {
        void onSuccess(Walk walk);
        void onFailure();
    }

    public interface ListRetrievalHandler {
        void onSuccess(List<Walk> walk);
        void onFailure();
    }

    WalkService() {
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        walkRef = dbRef.child("walks");
    }

    public static WalkService getInstance() {
        return WALK_REF;
    }

    public void createWalk ( Walk newWalk) {
        long startTime = Calendar.getInstance().getTime().getTime();

        DatabaseReference pushedRef = walkRef.push();

        pushedRef.child("orig").setValue(newWalk.getOrig());
        pushedRef.child("dest").setValue(newWalk.getDest());
        pushedRef.child("walker1Id").setValue(newWalk.getWalker1Id());
        pushedRef.child("walker2Id").setValue(newWalk.getWalker2Id());
        pushedRef.child("userId").setValue(newWalk.getUserId());
        pushedRef.child("startTime").setValue(startTime);
        pushedRef.child("endTime").setValue(0L);
        pushedRef.child("rating").setValue(0d);
        pushedRef.child("progress").setValue(Progress.STARTED.toString());

       String walkID = pushedRef.getKey();

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("users").child(newWalk.getUserId()).child("walkID").setValue(walkID);
        dbRef.child("users").child(newWalk.getWalker1Id()).child("walkID").setValue(walkID);
        dbRef.child("users").child(newWalk.getWalker2Id()).child("walkID").setValue(walkID);

       newWalk.setWalkId(walkID);
        newWalk.setProgress(Progress.STARTED);
        newWalk.setStartTime(startTime);

    }

    // TODO jason plug actual implementation
    public void retrieveCompletedWalks(String uid, final ListRetrievalHandler handler) {

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /**
        LatLng some_place = new LatLng(43.0731, -89.4012);
        LatLng another_place = new LatLng(43.0657, -89.0000);

        final Location orig = new Location("");
        orig.setLatitude(some_place.latitude);
        orig.setLongitude(some_place.longitude);

        //makes a new location object for the destination
        final Location dest = new Location("");
        dest.setLatitude(another_place.latitude);
        dest.setLongitude(another_place.longitude);

        final long start_from_some_place = 1512888673;
        final long start_from_another_place = 1512668673;

        handler.onSuccess(new ArrayList<Walk>(){{
            add(new Walk("123", orig, dest, "W1", "W2", "U1", start_from_some_place, start_from_some_place - (60*30), "completed"));
            add(new Walk("124", dest, orig, "W1", "W2", "U1", start_from_another_place, start_from_another_place - (60*30), "completed"));
            add(new Walk("125", dest, orig, "W1", "W2", "U1", start_from_another_place, start_from_another_place - (60*30), "completed"));
            add(new Walk("126", dest, orig, "W1", "W2", "U1", start_from_another_place, start_from_another_place - (60*30), "completed"));
            add(new Walk("127", dest, orig, "W1", "W2", "U1", start_from_another_place, start_from_another_place - (60*30), "completed"));
        }});

         */

        Query query = walkRef.orderByChild("userId").equalTo(uid);
        Log.d("WALK", "STARTED QUERY");
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Walk> userWalks = new ArrayList<Walk>();
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Location orig = new Location((String)ds.child("orig").child("provider").getValue());
                    Location dest = new Location((String)ds.child("dest").child("provider").getValue());

                    orig.setLongitude((double)ds.child("orig").child("longitude").getValue());
                    orig.setLatitude((double)ds.child("orig").child("latitude").getValue());

                    dest.setLongitude((double)ds.child("dest").child("longitude").getValue());
                    dest.setLatitude((double)ds.child("dest").child("latitude").getValue());


                    Object rating = ds.child("rating").getValue();
                    Walk retrievedWalk = new Walk(ds.getKey(), orig,dest, (String) ds.child("walker1Id").getValue(),
                            (String) ds.child("walker2Id").getValue(), (String) ds.child("userId").getValue(), (Long) ds.child("startTime").getValue(),
                            (Long) ds.child("endTime").getValue(),(String) ds.child("progress").getValue(), (rating == null ? 0d : Double.parseDouble(rating + "")));

                    Log.d("WALK ADDED", "DEST: " + retrievedWalk.getDest().getProvider());
                    userWalks.add(retrievedWalk);
                }

                Collections.sort(userWalks, new Comparator<Walk>() {
                    @Override
                    public int compare(Walk o1, Walk o2) {
                        return Long.valueOf(o2.getStartTime()).compareTo(Long.valueOf(o1.getStartTime()));
                    }
                });

                handler.onSuccess(userWalks);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }


    public void retrieveWalk(String walkId, final RetrievalHandler handler) {

        DatabaseReference ref = walkRef.child(walkId);
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                Location orig = new Location((String)dataSnapshot.child("orig").child("provider").getValue());
                Location dest = new Location((String)dataSnapshot.child("dest").child("provider").getValue());

                orig.setLongitude((double)dataSnapshot.child("orig").child("longitude").getValue());
                orig.setLatitude((double)dataSnapshot.child("orig").child("latitude").getValue());

                dest.setLongitude((double)dataSnapshot.child("dest").child("longitude").getValue());
                dest.setLatitude((double)dataSnapshot.child("dest").child("latitude").getValue());

                Object rating = dataSnapshot.child("rating").getValue();
                Walk retrievedWalk = new Walk(dataSnapshot.getKey(), orig,dest, (String) dataSnapshot.child("walker1Id").getValue(),
                        (String) dataSnapshot.child("walker2Id").getValue(), (String) dataSnapshot.child("userId").getValue(), (Long) dataSnapshot.child("startTime").getValue(),
                        (Long) dataSnapshot.child("endTime").getValue(),(String) dataSnapshot.child("progress").getValue(), (rating == null ? 0d : Double.parseDouble(rating + "")));

                handler.onSuccess(retrievedWalk);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {}
        });
    }

    public void cancel(Walk walk) {
        walk.setProgress(Progress.CANCELLED);
        walkRef.child(walk.getWalkId()).child("progress").setValue(Progress.CANCELLED.toString());

        long endTime = Calendar.getInstance().getTime().getTime();
        walk.setEndTime(endTime);
        walkRef.child(walk.getWalkId()).child("endTime").setValue(endTime);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("users").child(walk.getUserId()).child("walkID").setValue("");
        dbRef.child("users").child(walk.getWalker1Id()).child("walkID").setValue("");
        dbRef.child("users").child(walk.getWalker2Id()).child("walkID").setValue("");

    }

    public void rate(Walk walk, double rating) {
        walk.setRating(rating);
        walkRef.child(walk.getWalkId()).child("rating").setValue(rating);

        walk.setProgress(Progress.COMPLETED);
        walkRef.child(walk.getWalkId()).child("progress").setValue(Progress.COMPLETED.toString());

        long endTime = Calendar.getInstance().getTime().getTime();
        walk.setEndTime(endTime);
        walkRef.child(walk.getWalkId()).child("endTime").setValue(endTime);

        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference();
        dbRef.child("users").child(walk.getUserId()).child("walkID").setValue("");
        dbRef.child("users").child(walk.getWalker1Id()).child("walkID").setValue("");
        dbRef.child("users").child(walk.getWalker2Id()).child("walkID").setValue("");
    }

}
