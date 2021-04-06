package com.withullc.app.withu;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;

/**
 * Created by shantanusinghal on 31/10/17.
 */

public class TestWithUApplication extends WithUApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        if(FirebaseApp.getApps(this).isEmpty()) {
            FirebaseOptions options = new FirebaseOptions.Builder()
                .setApplicationId("AIzaSyCErEOx1NQtVryX4vSR_gG9VYvUTwP5sRo") // Required for Analytics.
                .setApiKey("AIzaSyA2_eWCSgYOVP2cJ1cS7rU91wvYnBiLiMk") // Required for Auth.
                .setDatabaseUrl("https://withu-5062017.firebaseio.com") // Required for RTDB.
                .build();
            FirebaseApp.initializeApp(this, options);
        }
    }



}
