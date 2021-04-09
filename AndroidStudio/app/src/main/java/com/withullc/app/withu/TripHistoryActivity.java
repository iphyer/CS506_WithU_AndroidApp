package com.withullc.app.withu;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.FoldingCube;
import com.google.firebase.auth.FirebaseAuth;
import com.withullc.app.withu.model.dataObjects.User;
import com.withullc.app.withu.model.dataObjects.Walk;
import com.withullc.app.withu.model.service.WalkService;
import com.withullc.app.withu.utils.TripHistoryAdapter;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.List;

public class TripHistoryActivity extends AppCompatActivity {
    private final String TAG = TripHistoryActivity.class.getSimpleName();

    public static final Format TIME = new SimpleDateFormat("dd MMM ''yy 'at' hh:mm aaa");

    // Services
    private WalkService walkService = WalkService.getInstance();

    // Transient state variables
    private String uid;
    private User currUser;

    // View handles
    private View spinnerContainer;
    private View failureContainer;
    private ProgressBar progressBar;
    private ActionBarDrawerToggle menuToggle;
    private DrawerLayout sideMenu;
    private RecyclerView mRecyclerView;
    private TripHistoryAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private boolean hideSideMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trip_history);

        // Get user data from Intent
        uid = getIntent().getStringExtra("uid");
        currUser = (User) getIntent().getSerializableExtra("currUser");
        hideSideMenu = getIntent().getBooleanExtra("hideSideMenu", false);

        // capture view handles
        progressBar = findViewById(R.id.trip_history_spinner);
        failureContainer = findViewById(R.id.tripHistory_failure_container);
        spinnerContainer = findViewById(R.id.trip_history_spinner_container);

        sideMenu = findViewById(R.id.tripHistory_side_menu);
        menuToggle = new ActionBarDrawerToggle(this, sideMenu, R.string.open, R.string.close);

        mRecyclerView = (RecyclerView) findViewById(R.id.trip_history_recycler_view);
        mRecyclerView.setVisibility(View.GONE);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        mAdapter = new TripHistoryAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // Draw side menu
        if(!hideSideMenu) {
            sideMenu.addDrawerListener(menuToggle);
            menuToggle.syncState();
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }

        // load walks
        loadTripHistory();
    }

    /**
     *  Retry fetching trip history
     */
    public void refresh(View view) {
        failureContainer.setVisibility(View.GONE);
        loadTripHistory();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (menuToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    /**Called when the user taps the Your trip history button */
    public void yourTripHistory(MenuItem item) {
        Intent intent = new Intent(this, TripHistoryActivity.class);
        intent.putExtra("uid", uid);
        intent.putExtra("currUser", currUser);
        startActivity(intent);
        finish();
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

    /**
     *  Fetch trip history from Firebase
     */

    private void loadTripHistory() {
        startSpinner();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                walkService.retrieveCompletedWalks(uid, new WalkService.ListRetrievalHandler() {
                    @Override
                    public void onSuccess(List<Walk> walks) {
                        stopSpinner();
                        mAdapter.setWalks(walks);
                        mAdapter.notifyDataSetChanged();
                        mRecyclerView.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onFailure() {
                        stopSpinner();
                        failureContainer.setVisibility(View.VISIBLE);
                    }
                });
            }
        }.execute();
    }

    /**
     *  Disables click on the user screen and show a loading spinner
     */
    private void startSpinner() {
        spinnerContainer.setVisibility(View.VISIBLE);
        progressBar.setIndeterminateDrawable(new FoldingCube());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }


    /**
     *  Hides the loading spinner and re-enables touch responses
     */
    private void stopSpinner() {
        spinnerContainer.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

}
