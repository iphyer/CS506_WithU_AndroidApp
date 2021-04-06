package com.withullc.app.withu.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;

import com.withullc.app.withu.ForgotPassword;
import com.withullc.app.withu.Login;
import com.withullc.app.withu.R;
import com.withullc.app.withu.Register;
import com.withullc.app.withu.UserMain;
import com.withullc.app.withu.utils.BaseRobolectricTest;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.httpclient.FakeHttp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by shantanusinghal on 31/10/17.
 */

public class LoginActivityTest extends BaseRobolectricTest {

    private Login loginActivity;
    private EditText email_input;
    private EditText password_input;
    private View login_button;
    private View forgot_password_button;

    @Before
    public void setUp() throws Exception {
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);
        // Build Activity
        loginActivity = Robolectric.buildActivity(Login.class).create().get();
        // Collect Views
        email_input = loginActivity.findViewById(R.id.login_email_input);
        password_input = loginActivity.findViewById(R.id.login_password_input);
        login_button = loginActivity.findViewById(R.id.login_submit_button);
        forgot_password_button = loginActivity.findViewById(R.id.login_forgot_pass_button);
    }

    @Test
    public void itShouldHaveTheRequiredViews() throws Exception {
        // When - Created
        // Then
        assertThat(email_input.getVisibility(),             equalTo(View.VISIBLE));
        assertThat(password_input.getVisibility(),          equalTo(View.VISIBLE));
        assertThat(login_button.getVisibility(),             equalTo(View.VISIBLE));
        assertThat(forgot_password_button.getVisibility(),    equalTo(View.VISIBLE));
    }

    // TODO move to main activity test
    @Ignore
    @Test
    public void clickingRegister_shouldStartRegistrationActivity() throws Exception {
        // When
//        register_link.performClick();

        // Then
        Intent expectedIntent = new Intent(loginActivity, Register.class);
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        assertThat(expectedIntent.getComponent(), is(actual.getComponent()));
    }

    @Test
    public void clickingForgotPassword_shouldStartForgotPasswordActivity() throws Exception {
        // When
        forgot_password_button.performClick();

        // Then
        Intent expectedIntent = new Intent(loginActivity, ForgotPassword.class);
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        assertThat(expectedIntent.getComponent(), is(actual.getComponent()));
    }

    @Ignore
    @Test
    public void clickingLogin_shouldStartUserMainActivity_forUser_withValidCredentials() throws Exception {
        // Given - Valid Credentials
        // TODO [shantanu] change email/pass to default values
        email_input.setText("singhal5@wisc.edu");
        password_input.setText("shantanu");

        // When
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                login_button.performClick();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // And
        ShadowApplication.runBackgroundTasks();
        Thread.sleep(10000);
        Robolectric.flushBackgroundThreadScheduler();

        // Then
        Intent expectedIntent = new Intent(loginActivity, UserMain.class);
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        assertThat(expectedIntent.getComponent(), is(actual.getComponent()));
    }
}
