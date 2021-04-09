package com.withullc.app.withu;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ybq.android.spinkit.style.DoubleBounce;
import com.google.firebase.auth.FirebaseUser;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.Validator.ValidationListener;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Order;
import com.mobsandgeeks.saripaar.annotation.Password;
import com.withullc.app.withu.model.dataObjects.User;
import com.withullc.app.withu.model.service.UserService;
import com.withullc.app.withu.model.service.ValidationService;

import java.util.List;

import static com.mobsandgeeks.saripaar.Validator.Mode.IMMEDIATE;

public class Register extends AppCompatActivity implements ValidationListener {

    private UserService userService = UserService.getInstance();
    private ValidationService validationService = ValidationService.getInstance();
    private String uid = "";

    @Order(1)
    @NotEmpty
    private EditText firstName;

    @Order(2)
    @NotEmpty
    private EditText lastName;

    @Order(3)
    @NotEmpty
    private EditText email_partial;

    @Order(4)
    @Checked(message = "Select a gender.")
    private RadioGroup genderGroup;

    @Order(5)
    @NotEmpty
    @Length(min = 10, max = 10, message = "Enter exactly 10 digits.")
    private EditText phone;

    @Order(6)
    @Password(min = 6,
            scheme = Password.Scheme.ALPHA_NUMERIC,
            message = "Must be at least 6 digits and alpha-numeric.")
    private EditText password;

    @Order(7)
    @ConfirmPassword
    private EditText confirmPassword;

    private CheckBox isWalker;
    private Validator validator;
    private View spinnerContainer;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firstName = findViewById(R.id.register_first_name);
        lastName = findViewById(R.id.register_last_name);
        email_partial = findViewById(R.id.register_email_address);
        password = findViewById(R.id.register_password);
        confirmPassword = findViewById(R.id.register_confirm_password);
        phone = findViewById(R.id.register_phone_number);
        genderGroup = findViewById(R.id.register_gender_group);
        isWalker = findViewById(R.id.register_as_walker);
        progressBar = findViewById(R.id.register_spinner);
        spinnerContainer = findViewById(R.id.register_spinner_container);

        firstName.requestFocus();

        validator = new Validator(this);
        validator.setValidationListener(this);
        validator.setValidationMode(IMMEDIATE);

        // make link clickable
        ((TextView) findViewById(R.id.register_terms)).setMovementMethod(LinkMovementMethod.getInstance());
    }

    /** Called when the user taps the Register button */
    public void register(View view) {
        Log.d("Register", "User clicked submit.");
        validator.validate();

    }

    @Override
    public void onValidationSucceeded() {
        Log.d("Register", "Validation succeeded.");
        startSpinner();
        final Register that = this;
        final User newUser = buildUser();

        validationService.signUp(newUser, password.getText().toString(), new ValidationService.SignUpHandler() {
            @Override
            public void successCallback(FirebaseUser user) {
                //Creates UserService and adds user to DB
                Log.d("Working", "Entered SignUp");

                // TODO explore auto generated UUID
                // TODO add PictureRef?
                userService.createUser(newUser, user.getUid());
                uid = user.getUid();

                Log.d("Created User",  "FINISHED");
                switchToUserScreen(newUser);
            }

            @Override
            public void failureCallback(FirebaseUser user) {
                stopSpinner();
                Toast.makeText(that, R.string.register_failure, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onValidationFailed(List<ValidationError> errors) {
        Log.d("Register", "Validation failed.");

        for (ValidationError error : errors) {
            View view = error.getView();
            String message = error.getCollatedErrorMessage(this);

            // Display error messages ;)
            if (view instanceof EditText) {
                ((EditText) view).setError(message);
            } else {
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        }

    }

    private User buildUser() {
        String email_full = email_partial.getText().toString() + "@wisc.edu";
        String name = firstName.getText().toString() + " " + lastName.getText().toString();
        String gender = ((RadioButton)findViewById(genderGroup.getCheckedRadioButtonId())).getText().toString();

        Log.d("Register Button", "Name: " + name +
                "\n Gender: " + gender +
                "\n Phone #: " + phone.getText().toString() +
                "\n Email: " + email_full +
                "\n Password: " + password.getText().toString() +
                "\n Confirm Pass: " + confirmPassword.getText().toString() +
                "\n isWalker: " + isWalker.isChecked());

        return new User(email_full, name, null, gender, isWalker.isChecked(), phone.getText().toString());
    }

    private void switchToUserScreen(User newUser) {
            Intent intent = new Intent(this, UserMain.class);
            intent.putExtra("uid", uid);
            intent.putExtra("currUser", newUser);
            startActivity(intent);
            finish();
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
}


