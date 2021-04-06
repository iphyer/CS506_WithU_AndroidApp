package com.withullc.app.withu;

import android.animation.LayoutTransition;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.github.javiersantos.materialstyleddialogs.enums.Style;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Places;
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

import java.util.ArrayList;

public class UserWalkActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private final String TAG = UserMain.class.getSimpleName();

    // Services
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private UserService userService = UserService.getInstance();
    private FusedLocationProviderClient mFusedLocationProviderClient;

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
    private Marker mLastKnownLocationMarker;
    private Marker mWalkerMaleMarker;
    private Marker mWalkerFemaleMarker;
    private Marker mDestinationMarker;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Location Permission Flag
    private boolean mLocationPermissionGranted = false;

    // State
    private Walk walk;
    private String uid;
    private User currUser;
    private LatLng reqOriginLatLng;
    private LatLng reqDestLatLng;
    private LatLng walkerMaleCurrLatLng;
    private LatLng walkerFemaleCurrLatLng;
    private String walkerFemaleDist;
    private String walkerFemaleDurn;
    private String walkerMaleDist;
    private String walkerMaleDurn;

    // View handles
    private DrawerLayout vSideMenu;
    private ImageButton vShowButton;
    private ImageButton vHideButton;
    private TextView vTextDesc;
    private ViewGroup vContactsContainer;
    private ActionBarDrawerToggle vMenuToggle;
    private SupportMapFragment vMapFragment;
    private ViewGroup vMaleContactCardView;
    private ViewGroup vFemaleContactCardView;
    private Button vNavigateButton;
    private WalkService walkService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_user_walk);

        // Get user data from Intent
        uid = getIntent().getStringExtra("uid");
        walk = getIntent().getExtras().getParcelable("walk");
        currUser = (User) getIntent().getSerializableExtra("currUser");
        reqOriginLatLng = new LatLng((double) getIntent().getSerializableExtra("req_lat"), (double) getIntent().getSerializableExtra("req_long"));
        reqDestLatLng = new LatLng((double) getIntent().getSerializableExtra("dest_lat"), (double) getIntent().getSerializableExtra("dest_long"));
        walkerMaleCurrLatLng = new LatLng((double) getIntent().getSerializableExtra("w_m_lat"), (double) getIntent().getSerializableExtra("w_m_long"));
        walkerFemaleCurrLatLng = new LatLng((double) getIntent().getSerializableExtra("w_f_lat"), (double) getIntent().getSerializableExtra("w_f_long"));
        walkerFemaleDist = (String) getIntent().getSerializableExtra("w_f_dist");
        walkerFemaleDurn = (String) getIntent().getSerializableExtra("w_f_durn");
        walkerMaleDist = (String) getIntent().getSerializableExtra("w_m_dist");
        walkerMaleDurn = (String) getIntent().getSerializableExtra("w_m_durn");

        walkService = WalkService.getInstance();

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        vMapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.userWalk_map);
        vMapFragment.getMapAsync(this);

        // Create view handles
        vTextDesc = findViewById(R.id.userWait_desc);
        vNavigateButton = findViewById(R.id.userWalk_button_navigate);
        vShowButton = findViewById(R.id.userWalk_action_show);
        vHideButton = findViewById(R.id.userWalk_action_hide);
        vContactsContainer = findViewById(R.id.userWalk_contacts_container);
        vSideMenu = findViewById(R.id.userWalk_SideMenu);
        vMenuToggle = new ActionBarDrawerToggle(this, vSideMenu, R.string.open, R.string.close);

        // show button to show cards
        vHideButton.setVisibility(View.GONE);
        vShowButton.setVisibility(View.VISIBLE);

        // Draw side menu
        vSideMenu.addDrawerListener(vMenuToggle);
        vMenuToggle.syncState();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

    }

    public void showContactCard(View view) {
        // toggle buttons
        vShowButton.setVisibility(View.GONE);

        // constructs views
        vMaleContactCardView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.user_waiting_card_male, vContactsContainer, false);

        vMaleContactCardView.findViewById(R.id.userWalk_action_call).setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
        vMaleContactCardView.findViewById(R.id.card_container).setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));
        ((TextView) vMaleContactCardView.findViewById(R.id.userWait_walker_desc)).setText(getResources().getString(R.string.user_waiting_contact_desc, "John Doe", "M", "31"));

        vFemaleContactCardView = (ViewGroup) LayoutInflater.from(this).inflate(
                R.layout.user_waiting_card_male, vContactsContainer, false);

        vFemaleContactCardView.findViewById(R.id.userWalk_action_call).setBackgroundColor(getResources().getColor(R.color.colorAccentLight));
        vFemaleContactCardView.findViewById(R.id.card_container).setBackgroundColor(getResources().getColor(R.color.colorAccentLight));
        ((TextView) vFemaleContactCardView.findViewById(R.id.userWait_walker_desc)).setText(getResources().getString(R.string.user_waiting_contact_desc, "Rosy Roe", "F", "28"));

        // add contact cards to container
        final UserWalkActivity that = this;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                vContactsContainer.addView(vMaleContactCardView, 0);
                Animation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, (float)0.0, Animation.RELATIVE_TO_SELF, (float)0.0);
                animation.setDuration(500);
                vMaleContactCardView.startAnimation(animation);
            }
        }, 75);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                vContactsContainer.addView(vFemaleContactCardView, 1);
                Animation animation = new ScaleAnimation(1.0f, 1.0f, 0.0f, 1.0f, Animation.RELATIVE_TO_SELF, (float)0.0, Animation.RELATIVE_TO_SELF, (float)0.0);
                animation.setDuration(500);
                vFemaleContactCardView.startAnimation(animation);
                vHideButton.setVisibility(View.VISIBLE);
                updatePadding();
            }
        }, 150);

    }

    public void hideContactCards(View view) {
        // enable appearing animation
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            vContactsContainer.getLayoutTransition()
                    .enableTransitionType(LayoutTransition.CHANGE_DISAPPEARING);
        }

        // toggle buttons
        vHideButton.setVisibility(View.GONE);

        // remove all cards from container
        final UserWalkActivity that = this;
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                vContactsContainer.removeViewAt(1);
                Animation animation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, (float)0.0, Animation.RELATIVE_TO_SELF, (float)0.0);
                animation.setDuration(250);
                vFemaleContactCardView.startAnimation(animation);
            }
        }, 75);
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                vContactsContainer.removeViewAt(0);
                Animation animation = new ScaleAnimation(1.0f, 1.0f, 1.0f, 0.0f, Animation.RELATIVE_TO_SELF, (float)0.0, Animation.RELATIVE_TO_SELF, (float)0.0);
                animation.setDuration(500);
                vMaleContactCardView.startAnimation(animation);
                vShowButton.setVisibility(View.VISIBLE);
                vContactsContainer.removeAllViews();
                updatePadding();
            }
        }, 150);


    }
    /**Called when the user taps the Your trip history button */
    public void yourTripHistory(MenuItem item) {
        Intent intent = new Intent(this, TripHistoryActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("currUser", currUser);
        intent.putExtra("hideSideMenu", true);
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
        getLogoutDialog().show();
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
        getCancellationDialog().show();
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

        // TODO shantanu add geo fence and trigger notifications
        // Add marker for original request location and both walker's initial location
        addOriginMarker();
        updateMaleWalkerMarker();
        updateFemaleWalkerMarker();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // center the camera accordingly
        recenterCamera();

        // Add user location change listener
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

        // TODO sam add listners for location of both walkers - call updateFemaleWalkerMarker() and updateMaleWalkerMarker() in callbacks
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "User location change triggered.");

        mLastKnownLocation = location;

        // TODO shantanu check that we shouldn't be needing this
        // updateUserCurrMarker();

        //move map camera accordingly
        recenterCamera();
    }

    private void recenterCamera(LatLng... points) {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        builder.include(mWalkerMaleMarker.getPosition());
        builder.include(mWalkerFemaleMarker.getPosition());
        builder.include(mLastKnownLocationMarker.getPosition());
        for (LatLng point : points) builder.include(point);
        LatLngBounds bounds = builder.build();

        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        // offset from edges of the map 10% of screen
        int padding = dpToPx(8);

        CameraUpdate cu = CameraUpdateFactory
                .newLatLngBounds(bounds, (int) (width * 0.90), (int) (height * 0.45), padding);

        mMap.moveCamera(cu);
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
        if (vMenuToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (item.getItemId() == R.id.userWalk_action_cancel) {
            getCancellationDialog().show();
        } else if (item.getItemId() == R.id.userWalk_action_report) {
            getReportDialog().show();
        } else if (item.getItemId() == R.id.userWalk_action_dev_start) {
            onWalkersArriving();
        } else if (item.getItemId() == R.id.userWalk_action_dev_stop) {
            onDestinationReached();
        }

        return super.onOptionsItemSelected(item);
    }

    public void launchNavigation(View view) {
        Uri gmmIntentUri = Uri.parse(getNavigationUrl());
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
        mapIntent.setPackage("com.google.android.apps.maps");
        startActivity(mapIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_walk_menu, menu);
        return true;
    }

    /* Supporting methods */

    private void onWalkersArriving() {
        new RouteTask(new RouteTask.TaskListener() {
            @Override
            public void onFinished(final PolylineOptions lineOptions, String duration, String distance) {
                // update the UI
                getSupportActionBar().setTitle(getResources().getText(R.string.user_waiting_starting_title));
                vTextDesc.setText(getResources().getText(R.string.user_waiting_starting_description));
                vNavigateButton.setVisibility(View.VISIBLE);

                // Drawing polyline in the Google Map for the i-th route
                mMap.addPolyline(lineOptions);

                // add destination marker
                addDestinationMarker();

                // re-position camera to show all markers
                recenterCamera(new ArrayList<LatLng>() {{
                    add(reqDestLatLng);
                    addAll(lineOptions.getPoints());
                }}.toArray(new LatLng[0]));

                // take a snapshot of the route and pass it to the dialog
                getArrivedDialog().show();
            }
        }).execute(reqOriginLatLng, reqDestLatLng);
    }

    private void onDestinationReached() {
        // update the UI
        getSupportActionBar().setTitle(getResources().getText(R.string.user_waiting_reached_title));
        vTextDesc.setText(getResources().getText(R.string.user_waiting_reached_description));

        // prompt for walker ratings
        getRatingDialog().show();
    }

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

    private MaterialStyledDialog getCancellationDialog() {
        final UserWalkActivity that = this;
        return getBaseDialog()
                .setTitle(getResources().getString(R.string.user_waiting_dialog_cancel_title))
                .setDescription(getResources().getString(R.string.user_waiting_dialog_cancel_description))
                .setPositiveText(getResources().getString(R.string.user_waiting_dialog_cancel_action_positive))
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG, "Cancel Dialog - Positive Response (No - Dismiss)");
                        dialog.dismiss();
                    }
                })
                .setNegativeText(getResources().getString(R.string.user_waiting_dialog_cancel_action_negative))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG, "Cancel Dialog - Negative Response (Yes - Cancel)");
                        WalkService.getInstance().cancel(walk);
                        goToHome(that);
                    }
                })
                .build();
    }

    private MaterialStyledDialog getLogoutDialog() {
        final UserWalkActivity that = this;
        return getBaseDialog()
                .setTitle(getResources().getString(R.string.user_waiting_dialog_logout_title))
                .setDescription(getResources().getString(R.string.user_waiting_dialog_logout_description))
                .setNeutralText(getResources().getString(R.string.user_waiting_dialog_logout_action_neutral))
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG, "Logout Dialog - Neutral Response (Nevermind - Dismiss)");
                        dialog.dismiss();
                    }
                })
                .setNegativeText(getResources().getString(R.string.user_waiting_dialog_logout_action_negative))
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        Log.d(TAG, "Logout Dialog - Negative Response (Cancel and Logout)");
                        Intent intent = new Intent(that, Login.class);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                            finishAffinity();
                        }
                        WalkService.getInstance().cancel(walk);
                        startActivity(intent);
                        FirebaseAuth.getInstance().signOut();
                        finish();
                    }
                })
                .build();
    }

    private MaterialStyledDialog getArrivedDialog() {
        final UserWalkActivity that = this;

        return getBaseDialog()
            .setStyle(Style.HEADER_WITH_ICON)
            .setIcon(getResources().getDrawable(R.drawable.ic_directions_walk_black_32dp))
            .setHeaderColor(R.color.colorAccentLight)
            .setCancelable(true)
            .setScrollable(true)
            .setTitle(getResources().getString(R.string.user_waiting_dialog_arrived_title))
            .setDescription(getResources().getString(R.string.user_waiting_dialog_arrived_description))
            .setNeutralText(getResources().getString(R.string.user_waiting_dialog_arrived_action_neutral))
            .onNeutral(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Log.d(TAG, "Arrival Dialog - Neutral Response (Dismiss)");
                    dialog.dismiss();
                }
            })
            .setPositiveText(getResources().getString(R.string.user_waiting_dialog_arrived_action_positive))
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Log.d(TAG, "Arrival Dialog - Positive Response (Launch Google Map)");
                    launchNavigation(null);
                }
            })
            .setNegativeText(getResources().getString(R.string.user_waiting_dialog_arrived_action_negative))
            .onNegative(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Log.d(TAG, "Arrival Dialog - Negative Response (Call)");
                    dialog.dismiss();
                    getCallDialog().show();
                }
            })
            .build();
    }

    private MaterialStyledDialog getRatingDialog() {
        final UserWalkActivity that = this;
        View ratingLayout = LayoutInflater.from(this).inflate(R.layout.user_walk_rate_dialog, null);
        final RatingBar ratingBar = ratingLayout.findViewById(R.id.userWalk_rateDialog_ratingBar);
        final TextView message = ratingLayout.findViewById(R.id.userWalk_rateDialog_message);
        ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
                message.setVisibility(View.GONE);
            }
        });

        return getBaseDialog()
            .setStyle(Style.HEADER_WITH_ICON)
            .setIcon(getResources().getDrawable(R.drawable.ic_beenhere_black_24dp))
            .setHeaderColor(R.color.colorPrimaryLight)
            .setCancelable(false)
            .setTitle(getResources().getString(R.string.user_waiting_dialog_reached_title))
            .setDescription(getResources().getString(R.string.user_waiting_dialog_reached_description))
            .setCustomView(ratingLayout)
            .setPositiveText(getResources().getString(R.string.user_waiting_dialog_reached_action_positive))
            .autoDismiss(false)
            .onPositive(new MaterialDialog.SingleButtonCallback() {
                @Override
                public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                    Log.d(TAG, "Reached Dialog - Positive Response (Submit)");
                    // TODO sam create call
                    // WalkService.getInstance().review(walk, ratingBar.getRating());
                    // TODO sam place these in callacks
                    if(ratingBar.getRating() > 0) {
                        dialog.dismiss();
                        WalkService.getInstance().rate(walk, (double) ratingBar.getRating());
                        goToHome(that);
                    } else {
                        Animation shake = AnimationUtils.loadAnimation(that, R.anim.shake);
                        message.setVisibility(View.VISIBLE);
                        message.startAnimation(shake);
                    }
                }
            })
            .build();
    }

    private void goToHome(UserWalkActivity that) {
        Intent intent = new Intent(that, UserMain.class);
        intent.putExtra("uid", uid);
        intent.putExtra("currUser", currUser);
        startActivity(intent);
        finish();
    }

    @NonNull
    private String getNavigationUrl() {
        return String.format("google.navigation:q=%s,%s&mode=w", reqDestLatLng.latitude, reqDestLatLng.longitude);
    }

    private MaterialStyledDialog getCallDialog() {
        // setup intent for dialing
        final Intent intent = new Intent(Intent.ACTION_DIAL);
        View callPreviewLayout = LayoutInflater.from(this).inflate(R.layout.user_walk_call_dialog, null);
        TextView p1Desc = callPreviewLayout.findViewById(R.id.userWalk_call_dialog_p1_desc);
        TextView p2Desc = callPreviewLayout.findViewById(R.id.userWalk_call_dialog_p2_desc);

        // TODO jason or sam build use name from walker object
        p1Desc.setText(getResources().getString(R.string.user_waiting_dialog_call_action_desc, "Rosy Roe"));
        p2Desc.setText(getResources().getString(R.string.user_waiting_dialog_call_action_desc, "John Doe"));

        callPreviewLayout.findViewById(R.id.userWalk_call_dialog_p1_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO jason or sam build use phone number from walker object
                intent.setData(Uri.parse("tel:" + "6089600027"));
                startActivity(intent);
            }
        });

        callPreviewLayout.findViewById(R.id.userWalk_call_dialog_p2_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO jason or sam build use phone number from walker object
                intent.setData(Uri.parse("tel:" + "6089600027"));
                startActivity(intent);
            }
        });

        // TODO jason or sam build walker object
        return getBaseDialog()
            .setStyle(Style.HEADER_WITH_ICON)
            .setIcon(getResources().getDrawable(R.drawable.ic_phone_forwarded_black_24dp))
            .setHeaderColor(R.color.colorPrimaryDark)
            .setScrollable(true)
            .setCancelable(true)
            .setTitle(getResources().getString(R.string.user_waiting_dialog_call_title))
            .setDescription(getResources().getString(R.string.user_waiting_dialog_call_desc))
            .setCustomView(callPreviewLayout, 25, 0, 25, 25)
            .build();
    }

    private MaterialStyledDialog getReportDialog() {
        // setup intent for dialing
        final Intent intent = new Intent(Intent.ACTION_DIAL);
        View callPreviewLayout = LayoutInflater.from(this).inflate(R.layout.user_walk_report_dialog, null);
        TextView p1Desc = callPreviewLayout.findViewById(R.id.userWalk_report_dialog_p1_desc);
        p1Desc.setText(getResources().getString(R.string.user_report_dialog_call_action));

        callPreviewLayout.findViewById(R.id.userWalk_report_dialog_p1_container).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                intent.setData(Uri.parse("tel:" + "6089600029"));
                startActivity(intent);
            }
        });

        return getBaseDialog()
            .setStyle(Style.HEADER_WITH_TITLE)
            .setIcon(getResources().getDrawable(R.drawable.ic_phone_forwarded_black_24dp))
            .setHeaderColor(R.color.colorPrimaryLight)
            .setScrollable(true)
            .setCancelable(true)
            .setTitle(getResources().getString(R.string.user_report_dialog_call_title))
            .setDescription(getResources().getString(R.string.user_report_dialog_call_desc))
            .setCustomView(callPreviewLayout, 25, 0, 25, 25)
            .build();
    }

    private MaterialStyledDialog.Builder getBaseDialog() {
        return new MaterialStyledDialog.Builder(this)
                .setIcon(getResources().getDrawable(R.drawable.ic_pair))
                .setHeaderColor(R.color.colorPrimaryDark)
                .withIconAnimation(false)
                .withDialogAnimation(false);
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

                updatePadding();

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
                            recenterCamera();
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

    private void addOriginMarker() {
        MarkerOptions marker = new MarkerOptions();
        marker.position(reqOriginLatLng);
        marker.title(getApplicationContext().getString(R.string.user_waiting_marker_info));
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

        mLastKnownLocationMarker = mMap.addMarker(marker);
        mLastKnownLocationMarker.showInfoWindow();
    }

    private void addDestinationMarker() {
        MarkerOptions marker = new MarkerOptions();
        marker.position(reqDestLatLng);
        marker.title(getApplicationContext().getString(R.string.user_waiting_marker_dest));
        marker.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));

        mDestinationMarker = mMap.addMarker(marker);
        mDestinationMarker.showInfoWindow();
    }

    private void updatePadding() {
        int vContactsContainerHeight = vContactsContainer.getHeight();
        int topPadding = vTextDesc.getHeight() + dpToPx(8) + vContactsContainerHeight == 0 ? vContactsContainerHeight + dpToPx(16) : dpToPx(8);
        mMap.setPadding(0, topPadding, 0, 0);
    }

    private void updateMaleWalkerMarker() {
        if (mWalkerFemaleMarker != null) {
            mWalkerFemaleMarker.remove();
        }
        // build current location markers
        MarkerOptions maleMarkerOptions = new MarkerOptions();
        maleMarkerOptions.position(walkerMaleCurrLatLng);
        maleMarkerOptions.title(getApplicationContext().getString(R.string.user_waiting_marker_estimate_title, walkerMaleDist));
        maleMarkerOptions.snippet(getApplicationContext().getString(R.string.user_waiting_marker_estimate_snippet, walkerMaleDurn));
        maleMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        // add location markers
        mWalkerMaleMarker = mMap.addMarker(maleMarkerOptions);
        // add location markers
        mWalkerMaleMarker = mMap.addMarker(maleMarkerOptions);
        // enable info boxes
        mWalkerMaleMarker.showInfoWindow();
    }

    private void updateFemaleWalkerMarker() {
        if (mWalkerMaleMarker != null) {
            mWalkerMaleMarker.remove();
        }
        // build current location markers
        MarkerOptions femaleMarkerOptions = new MarkerOptions();
        femaleMarkerOptions.position(walkerFemaleCurrLatLng);
        femaleMarkerOptions.title(getApplicationContext().getString(R.string.user_waiting_marker_estimate_title, walkerFemaleDist));
        femaleMarkerOptions.snippet(getApplicationContext().getString(R.string.user_waiting_marker_estimate_snippet, walkerFemaleDurn));
        femaleMarkerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_MAGENTA));
        // add location markers
        mWalkerFemaleMarker = mMap.addMarker(femaleMarkerOptions);
        // add location markers
        mWalkerFemaleMarker = mMap.addMarker(femaleMarkerOptions);
        // enable info boxes
        mWalkerFemaleMarker.showInfoWindow();
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }
}
