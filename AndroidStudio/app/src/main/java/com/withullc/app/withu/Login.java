package com.withullc.app.withu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.firebase.auth.FirebaseUser;
import com.withullc.app.withu.model.dataObjects.User;
import com.withullc.app.withu.model.service.UserService;
import com.withullc.app.withu.model.service.ValidationService;

public class Login extends AppCompatActivity {

    private ValidationService validationService = ValidationService.getInstance();
    private UserService userService = UserService.getInstance();
    private String email, password;
    private SharedPreferences loginCredentials;
    private EditText editTextEmail, editTextPassword;
    private CheckBox rememberMeCheckBox;
    private User logInUser;
    private String uid;
    private ProgressBar progressBar;
    private View spinnerContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        boolean rememberMe;

        //Retrieve the email and password EditText IDs
        editTextEmail = findViewById(R.id.login_email_input);
        editTextPassword = findViewById(R.id.login_password_input);
        progressBar = findViewById(R.id.login_spinner);
        spinnerContainer = findViewById(R.id.login_spinner_container);

        // Retrieve the rememberMe checkbox ID
        rememberMeCheckBox = findViewById(R.id.login_rememberMe);

        loginCredentials = getSharedPreferences("credentialPrefs", MODE_PRIVATE);

        // If the user had previously selected to be remembered, automatically fill credentials
        rememberMe = loginCredentials.getBoolean("rememberMe", false);
        if(rememberMe) {
            editTextEmail.setText(loginCredentials.getString("email", ""));
            editTextPassword.setText(loginCredentials.getString("password", ""));
            rememberMeCheckBox.setChecked(true);
        }

        // TODO replace with notification service that creates a messaging queue
        // for now check if intent brings the request to display a toast message
        Bundle extras = getIntent().getExtras();
        if(extras!= null) {
            Toast.makeText(this, extras.getString("toast"), Toast.LENGTH_LONG).show();
        }

    }

    /** Called when the user taps the Log In button */
    public void logIn(View view) {
        Log.d("Log In", "The login button was triggered.");

        // Retrieve email and password from fields
        email = editTextEmail.getText().toString().trim();
        password = editTextPassword.getText().toString().trim();

        // Error if password is not of correct length
        if(password.length() < 6) {
            editTextPassword.setError(getString(R.string.min_password));
        } else if(password.length() > 6 && !email.matches("")) {
            startSpinner();
            //Email and password pass initial screening
            validationService.signIn(email, password, new ValidationService.SignInHandler() {
                @Override
                public void successCallback(FirebaseUser user) {
                    uid = user.getUid();
                    userService.retrieveUserOnce(user.getUid(), new UserService.RetrievalHandler() {
                        @Override
                        public void retrieved(User user) {
                            if (user != null) {
                                Log.d("Retrieved", "User retrieved");
                                logInUser = user;
                                saveCredentials();
                                switchToUserScreen();
                                finish();
                            } else {
                                Log.d("RetrieveFail", "Failed to retrieve user.");
                            }
                        }
                    });
                }
                @Override
                public void failureCallback() {
                    stopSpinner();
                    Toast.makeText(Login.this, getString(R.string.login_failure_authentication), Toast.LENGTH_LONG).show();
                }
            });
        } else {
            // Email and password were not entered correctly
            Toast.makeText(Login.this, getString(R.string.login_failure_parsing), Toast.LENGTH_LONG).show();

        }
    }

    private void startSpinner() {
        spinnerContainer.setVisibility(View.VISIBLE);
        progressBar.setIndeterminateDrawable(new DoubleBounce());
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    private void stopSpinner() {
        spinnerContainer.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }

    /**Saves the credentials of the user */
    public void saveCredentials() {
        SharedPreferences.Editor loginCredentialsEditor;
        loginCredentialsEditor = loginCredentials.edit();
        // Save credentials if the user check the rememberMe box and log in is successful
        if(rememberMeCheckBox.isChecked()) {
            loginCredentialsEditor.putBoolean("rememberMe", true);
            loginCredentialsEditor.putString("email", email);
            loginCredentialsEditor.putString("password", password);
            loginCredentialsEditor.apply();
        } else {
            loginCredentialsEditor.clear();
            loginCredentialsEditor.commit();
        }
    }

    /** Called when the user taps the Register button */
    public void register(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, Register.class);
        startActivity(intent);
    }

    /** Called when the user taps the Forgot Password button */
    public void forgotPassword(View view) {
        // Do something in response to button
        Intent intent = new Intent(this, ForgotPassword.class);
        startActivity(intent);
    }

    private void switchToUserScreen() {
            Intent intent = new Intent(this, UserMain.class);
            intent.putExtra("uid", uid);
            intent.putExtra("currUser", logInUser);
            startActivity(intent);
    }
}
