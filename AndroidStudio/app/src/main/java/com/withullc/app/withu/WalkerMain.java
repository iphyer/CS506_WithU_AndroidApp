package com.withullc.app.withu;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.withullc.app.withu.model.dataObjects.User;
import com.withullc.app.withu.model.dataObjects.Walk;
import com.withullc.app.withu.model.service.PairService;
import com.withullc.app.withu.model.service.UserService;
import com.withullc.app.withu.model.service.WalkService;

public class WalkerMain extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private DrawerLayout sideMenu;
    private ActionBarDrawerToggle menuToggle;
    private String uid;
    private User currUser;
    private Walk currWalk;


    private UserService userService = UserService.getInstance();
    private PairService pairService = PairService.getInstance();
    private WalkService walkService = WalkService.getInstance();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walker_main);

        uid = getIntent().getStringExtra("uid");
        currUser = (User) getIntent().getSerializableExtra("currUser");

        if(!currUser.getWalkID().equals(""))
        {
            walkService.retrieveWalk(currUser.getWalkID(), new WalkService.RetrievalHandler() {
                @Override
                public void onSuccess(Walk walk) {
                    if (walk != null) {
                        currWalk = walk;
                        Log.d("REQUESTED WALK ID", "Line 54" + currWalk.getWalkId());
                    } else {
                        currWalk = null;
                    }
                }
                @Override
                public void onFailure() {
                    // TODO do something
                }
            });
        }


        // When WalkMain is entered, currUser isActive should be set to true
        currUser.setActive(true);
        userService.setActive(uid, true);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sideMenu = (DrawerLayout) findViewById(R.id.walkerSideMenu);
        menuToggle = new ActionBarDrawerToggle(this, sideMenu, R.string.open, R.string.close);

        sideMenu.addDrawerListener(menuToggle);
        menuToggle.syncState();

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onStop() {
        super.onStop();

        // The user's isActive field should be set to false whenever WalkerMain is stopped.
        userService.setActive(uid, false);
    }

    public void onRestart() {
        super.onRestart();

        // The user's isActive field should be set to true whenever WalkerMain is restarted.
        userService.setActive(uid, true);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menuToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //clears the map of any markers
        mMap.clear();
        LatLng madison = new LatLng(43.0731, -89.4012);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(madison, 15));

        Handler handler = new Handler();


        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.setMyLocationEnabled(true);
        }

        //This handler is set up so it waits until the currUser is set then places
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                //we do have a walk
                if (currWalk != null) {
                    Log.d("REQUESTED", " Line 139, LAT - " + currWalk.getOrig().getLatitude());

                    mMap.addMarker(new MarkerOptions().position(new LatLng(currWalk.getOrig().getLatitude(), currWalk.getOrig().getLongitude())).title("User's Location"));
                    mMap.addMarker(new MarkerOptions().position(new LatLng(currWalk.getDest().getLatitude(), currWalk.getDest().getLongitude())).title("User's Destination")).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                }
            }
        }, 1000);


    }

    /**Called when the user taps the Your trip history button */
    public void yourTripHistory(MenuItem item) {
        /*
        Intent intent = new Intent(this, TripHistoryActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("currUser", currUser);
        startActivity(intent);
        */
        Context context = getApplicationContext();
        CharSequence text = "There is no Trip History";
        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, text, duration);
        toast.show();


    }

    /**Called when the user taps the settings button */
    public void settings(MenuItem item) {
        Intent intent = new Intent(this, Settings.class);
        startActivity(intent);
    }

    /**Called when the user taps the help button */
    public void help(MenuItem item) {
        Intent intent = new Intent(this, Help.class);
        startActivity(intent);
    }

    /** Called when the user clicks the pair button */
    public void pair(MenuItem item) {
        //Switch to the Pairing Screen
        //Intent intent = new Intent(this, Pairing.class);
        //intent.putExtra("uid", uid);
        //intent.putExtra("currUser", currUser);
        //startActivity(intent);
        //finish();
    }

    /** Called when the user taps the Logout button */
    public void logout(MenuItem item) {
        // Do something in response to button
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        finish();
    }

    /** Called when the user taps the Switch to User button */
    public void switchToUser(MenuItem item) {
        // User isActive must be switched to false
        currUser.setActive(false);

        // Switch to UserMain
        Intent intent = new Intent(this, UserMain.class);
        intent.putExtra("uid", uid);
        intent.putExtra("currUser", currUser);
        startActivity(intent);
        finish();
    }
}
