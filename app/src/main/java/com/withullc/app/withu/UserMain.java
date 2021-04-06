package com.withullc.app.withu;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.WanderingCubes;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.withullc.app.withu.model.dataObjects.User;
import com.withullc.app.withu.model.dataObjects.Walk;
import com.withullc.app.withu.model.service.UserService;
import com.withullc.app.withu.model.service.WalkService;
import com.withullc.app.withu.utils.RouteTask;

import java.util.HashMap;
import java.util.Map;


public class UserMain extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TAG = UserMain.class.getSimpleName();

    // Services
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private UserService userService = UserService.getInstance();
    private WalkService walkService = WalkService.getInstance();
    private FusedLocationProviderClient mFusedLocationProviderClient;

    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;



    // A default location (Madison, WI) and default zoom to use when location permission is
    // not granted.
    private final int DEFAULT_ZOOM = 15;
    public static final LatLng mDefaultLocation_MADISON = new LatLng(43.0731, -89.4012);

    // Request codes
    private final int TO_PLACE_PICKER_REQUEST = 1002;
    private final int FROM_PLACE_PICKER_REQUEST = 1001;
    private final int RESOLVE_CONNECTION_REQUEST_CODE = 1000;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;
    private Place mDestination;
    private Marker mLastKnownLocationMarker;
    private Marker mDestinationMarker;

    //Details about where the users destination and origin
    private String destName;
    private String origName;
    private Place currentPlace;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Location Permission Flag
    private boolean mLocationPermissionGranted = false;

    // Transient state variables
    private String uid;
    private User currUser;

    // View handles
    private DrawerLayout sideMenu;
    private ActionBarDrawerToggle menuToggle;
    private TextView vDestination;
    private Button vRequestWalk;
    private SupportMapFragment mapFragment;
    private View userWalk;
    private ProgressBar progressBar;
    private View spinnerContainer;

    //Start Time
    private long sTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_user_main);

        // Get user data from Intent
        uid = getIntent().getStringExtra("uid");
        currUser = (User) getIntent().getSerializableExtra("currUser");

        // TODO ? this field isn't getting populated
        // if (!currUser.getEmail().contains("user")) FirebaseMessaging.getInstance().subscribeToTopic("WALKERS");

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        //Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Build the map.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Create view handles
        vRequestWalk = findViewById(R.id.user_button_request);
        vDestination = findViewById(R.id.userMain_destination);
        sideMenu = findViewById(R.id.userMain_SideMenu);
        menuToggle = new ActionBarDrawerToggle(this, sideMenu, R.string.open, R.string.close);
        progressBar = findViewById(R.id.user_main_spinner);
        spinnerContainer = findViewById(R.id.user_main_spinner_container);

        // Disable request button
        disableRequestButton();

        // Draw side menu
        sideMenu.addDrawerListener(menuToggle);
        menuToggle.syncState();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void requestWalk(View view) {
        startSpinner();
        final UserMain that = this;
        @SuppressLint("MissingPermission") PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                .getCurrentPlace(mGoogleApiClient, null);
        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
            @Override
            public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                if(likelyPlaces.getCount() > 0) {
                    PlaceLikelihood top = likelyPlaces.get(0);
                    origName = getNameFrom(top.getPlace());
                    Log.d(TAG, String.format("Picking Origin as '%s' with likelihood: %g", getNameFrom(top.getPlace()), top.getLikelihood()));
                } else {
                    origName = String.format("Unnamed Place, Madison(%.2f,%.2f)", mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
                    Log.d(TAG, String.format("Couldn't find origin place, reverting to lat-long name"));
                }
                likelyPlaces.release();

                //takes the best chance where the user is and assigns that to the location
                //also takes the last known location and sets the lat and log to the origin
                Location org = new Location(origName);
                org.setLatitude(mLastKnownLocation.getLatitude());
                org.setLongitude(mLastKnownLocation.getLongitude());

                //makes a new location object for the destination
                //also takes the last known location and sets the lat and log to the destination
                Location dest = new Location(destName);
                dest.setLatitude(mDestination.getLatLng().latitude);
                dest.setLongitude(mDestination.getLatLng().longitude);

                Walk walk = new Walk (org, dest, "5B0QZhadIUhwCM18nNvTbpW07ch1", "UzAupWR8IGQLmLSac1dSEcKij2S2", uid);
                walkService.createWalk(walk);

                Intent intent = new Intent(that, UserWalkActivity.class);
                intent.putExtra("uid", uid);
                intent.putExtra("walk", walk);
                intent.putExtra("currUser", currUser);
                intent.putExtra("req_lat", mLastKnownLocation.getLatitude());
                intent.putExtra("req_long", mLastKnownLocation.getLongitude());
                intent.putExtra("dest_lat", mDestination.getLatLng().latitude);
                intent.putExtra("dest_long", mDestination.getLatLng().longitude);
                intent.putExtra("w_m_lat", 43.0701);
                intent.putExtra("w_m_long", -89.4002);
                intent.putExtra("w_m_dist", ".5 mi");
                intent.putExtra("w_m_durn", "7 mins");
                intent.putExtra("w_f_lat", 43.0711);
                intent.putExtra("w_f_long", -89.4000);
                intent.putExtra("w_f_dist", ".75 mi");
                intent.putExtra("w_f_durn", "10 mins");

                // stop loading spinner
                stopSpinner();

                // finish and move to new activity
                startActivity(intent);
                finish();
            }
        });
    }

    public void pickDestination(View view) {
        if (mLocationPermissionGranted) {
            try {
                PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                Intent intent = builder.build(this);

                startActivityForResult(intent, TO_PLACE_PICKER_REQUEST);
            } catch (Exception e) {
                Log.d(TAG, e.getMessage());
            }
        }
    }

    /** Called when the user selects destination */
    private void setDestination(final Place place) {
        vDestination.setText(getNameFrom(place));

        // Remove old markers and add a marker for source
        mMap.clear();
        updateOriginMarker();

        // Draw source to destination route
        new RouteTask(new RouteTask.TaskListener() {
            @Override
            public void onFinished(PolylineOptions lineOptions, String duration, String distance) {
                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);

                // add destination marker
                mDestination = place;
                updateDestinationMarkerAt(place, duration, distance);

                // re-position camera to show both markers
                recenterCamera();

                // Enable request button
                enableRequestButton();
            }
        }).execute(new LatLng(mLastKnownLocation.getLatitude(),
                mLastKnownLocation.getLongitude()), place.getLatLng());
    }

    /**Called when the user taps the Your trip history button */
    public void yourTripHistory(MenuItem item) {
        Intent intent = new Intent(this, TripHistoryActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("currUser", currUser);
        startActivity(intent);
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

    /** Called when the user taps the Logout button */
    public void logout(MenuItem item) {
        // Do something in response to button
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
        FirebaseAuth.getInstance().signOut();
        finish();
    }


    /** Called when the user taps the Switch to Walker button */
    public void switchToWalker(MenuItem item) {
        // Do something in response to button
        if (!currUser.getWalker()) {
            Toast.makeText(this, "You are not a walker", Toast.LENGTH_SHORT).show();
        } else {
            Intent intent = new Intent(this, WalkerMain.class);
            intent.putExtra("uid", uid);
            intent.putExtra("currUser", currUser);
            startActivity(intent);
            finish();
        }
    }

    /*
        Standard Google Play Services lifecycle callbacks
    */

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "Google Map loaded.");

        mMap = googleMap;

        // Connect to Google Play API
        buildGoogleApiClient();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.d(TAG, "Connected to Google API.");

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        // Add location change listener
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Location change triggered.");

        mLastKnownLocation = location;

        updateOriginMarker();

        //update the current location for the current user
        userService.updateLocation(uid, mLastKnownLocation.getLatitude(),mLastKnownLocation.getLongitude());
        Map<String, Double> updatedLoc = new HashMap<String,Double>();
        updatedLoc.put("latitude", mLastKnownLocation.getLatitude());
        updatedLoc.put("longitude", mLastKnownLocation.getLongitude());
        currUser.setLocation(updatedLoc);


        //move map camera
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), DEFAULT_ZOOM));
    }

    private void recenterCamera() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mDestinationMarker.getPosition());
        builder.include(mLastKnownLocationMarker.getPosition());
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        // offset from edges of the map 10% of screen
        int padding = dpToPx(8);

        CameraUpdate cu = CameraUpdateFactory
                .newLatLngBounds(bounds, (int) (width * 0.90), (int) (height * 0.45), padding);

        mMap.animateCamera(cu);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // This code is passed when the user has resolved his
            // Google Play Services connection issues.
            case RESOLVE_CONNECTION_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    mGoogleApiClient.connect();
                }
                break;

            case FROM_PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    // TODO handle update

                }
                break;

            case TO_PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    destName = getNameFrom(place);
                    Log.d(TAG, "Destination is: " + destName);
                    setDestination(place);
                }
                break;

        }
    }

    @NonNull
    private String getNameFrom(Place place) {
        String name = place.getName().toString();
        String address = place.getAddress().toString();
        return name.contains("°") ? address : name + "; " + address;
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "Connection to Google API was suspended somehow!");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult result) {
        Log.d(TAG, "Connection failed with result code: " + result.getErrorCode());
        if (result.hasResolution()) {
            try {
                result.startResolutionForResult(this, RESOLVE_CONNECTION_REQUEST_CODE);
            } catch (IntentSender.SendIntentException e) {
                // TODO shantanu Notify user about service outage or something...
            }
        } else {
            GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
            apiAvailability.getErrorDialog(this, result.getErrorCode(), 0).show();
        }
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menuToggle.onOptionsItemSelected(item)) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Supporting methods */

    private void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .build();
        mGoogleApiClient.connect();
    }

    /**
     * Request location permission, so that we can get the location of the
     * device. The result of the permission request is handled by a callback,
     * onRequestPermissionsResult.
     */
    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                UiSettings uiSettings = mMap.getUiSettings();

                mMap.setPadding(0, vDestination.getHeight() + dpToPx(30), 0, 0);

                uiSettings.setIndoorLevelPickerEnabled(false);
                uiSettings.setMapToolbarEnabled(false);
                uiSettings.setZoomControlsEnabled(true);
                uiSettings.setZoomGesturesEnabled(true);
                uiSettings.setScrollGesturesEnabled(true);
                uiSettings.setRotateGesturesEnabled(true);
                uiSettings.setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            updateOriginMarker();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation_MADISON, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateOriginMarker() {
        if (mLastKnownLocationMarker != null) {
            mLastKnownLocationMarker.remove();
        }

        //Place current location marker
        LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(getApplicationContext().getString(R.string.user_main_marker_from));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mLastKnownLocationMarker = mMap.addMarker(markerOptions);

        mLastKnownLocationMarker.showInfoWindow();
    }

    private void updateDestinationMarkerAt(Place place, String duration, String distance) {
        if (mDestinationMarker != null) {
            mDestinationMarker.remove();
        }

        //Place current location marker
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(place.getLatLng());
        markerOptions.title(getApplicationContext().getString(R.string.user_main_marker_to));
        markerOptions.snippet(getApplicationContext().getString(R.string.user_main_marker_to_estimate, distance, duration));
        markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

        mDestinationMarker = mMap.addMarker(markerOptions);

        mDestinationMarker.showInfoWindow();
    }

    private void enableRequestButton() {
        vRequestWalk.setClickable(true);
        vRequestWalk.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        vRequestWalk.setTextColor(Color.WHITE);

    }

    private void disableRequestButton() {
        vRequestWalk.setClickable(false);
        vRequestWalk.setBackgroundColor(getResources().getColor(R.color.disabledButton));
        vRequestWalk.setTextColor(Color.DKGRAY);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    private void startSpinner() {
        spinnerContainer.setVisibility(View.VISIBLE);
        progressBar.setIndeterminateDrawable(new WanderingCubes());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void stopSpinner() {
        spinnerContainer.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
