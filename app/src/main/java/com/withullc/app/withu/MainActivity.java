package com.withullc.app.withu;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.withullc.app.withu.model.dataObjects.User;
import com.withullc.app.withu.model.service.UserService;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "MainActivity";

    private Button loginButton;
    private Button registerButton;

    private UserService userService = UserService.getInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            // User is signed-in
            Log.d(TAG, "Initiating auto-login");
            final MainActivity that = this;
            userService.retrieveUserOnce(firebaseUser.getUid(), new UserService.RetrievalHandler() {
                @Override
                public void retrieved(User user) {
                    if (user != null) {
                        Log.d(TAG, "User retrieved.");
                        Intent intent = new Intent(that, UserMain.class);
                        intent.putExtra("uid", firebaseUser.getUid());
                        intent.putExtra("currUser", user);
                        startActivity(intent);
                        finish();
                    } else {
                        Log.d(TAG, "Failed to retrieve user.");
                    }
                }
            });
        } else {

            setContentView(R.layout.activity_splash);

            loginButton = findViewById(R.id.main_login_button);
            registerButton = findViewById(R.id.main_register_button);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Create channel to show notifications.
                String channelId = getString(R.string.default_notification_channel_id);
                String channelName = getString(R.string.default_notification_channel_name);
                NotificationManager notificationManager =
                        getSystemService(NotificationManager.class);
                notificationManager.createNotificationChannel(new NotificationChannel(channelId,
                        channelName, NotificationManager.IMPORTANCE_DEFAULT));
            }

            // If a notification message is tapped, any data accompanying the notification
            // message is available in the intent extras. In this sample the launcher
            // intent is fired when the notification is tapped, so any accompanying data would
            // be handled here. If you want a different intent fired, set the click_action
            // field of the notification message to the desired intent. The launcher intent
            // is used when no click_action is specified.
            //
            // Handle possible data accompanying notification message.
            // [START handle_data_extras]
            if (getIntent().getExtras() != null) {
                for (String key : getIntent().getExtras().keySet()) {
                    Object value = getIntent().getExtras().get(key);
                    Log.d(TAG, "Key: " + key + " Value: " + value);
                }
            }
            // [END handle_data_extras]
        }
    }

    public void navigateToLogin(View view) {
        Log.d(TAG, "Login button clicked");
        startActivity(new Intent(this, Login.class));
    }

    public void navigateToRegister(View view) {
        Log.d(TAG, "Registration button clicked");
        startActivity(new Intent(this, Register.class));
    }


}
