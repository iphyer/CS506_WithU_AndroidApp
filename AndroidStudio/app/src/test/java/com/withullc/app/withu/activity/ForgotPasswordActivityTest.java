package com.withullc.app.withu.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.EditText;

import com.withullc.app.withu.ForgotPassword;
import com.withullc.app.withu.Login;
import com.withullc.app.withu.R;
import com.withullc.app.withu.utils.BaseRobolectricTest;
import com.withullc.app.withu.utils.TestUtils.MockUser;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.robolectric.Robolectric;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowToast;
import org.robolectric.shadows.httpclient.FakeHttp;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by shantanusinghal on 31/10/17.
 */

public class ForgotPasswordActivityTest extends BaseRobolectricTest {

    private ForgotPassword forgotPassActivity;
    private EditText email_input;
    private View email_label;
    private View forgotPassButton;

    private final CharSequence SUCCESS_MESSAGE = getApplication().getApplicationContext().getText(R.string.forgot_password_success);
    private final CharSequence FAILURE_MESSAGE = getApplication().getApplicationContext().getText(R.string.forgot_password_failure);
    private final CharSequence INVALID_EMAIL_MESSAGE = getApplication().getApplicationContext().getText(R.string.forgot_password_invalid);

    @Before
    public void setUp() throws Exception {
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);
        // Build Activity
        forgotPassActivity = Robolectric.buildActivity(ForgotPassword.class).create().get();
        // Collect Views
        email_input = forgotPassActivity.findViewById(R.id.editText);
        email_label = forgotPassActivity.findViewById(R.id.textView5);
        forgotPassButton = forgotPassActivity.findViewById(R.id.button4);
    }

    @Test
    public void itShouldHaveTheRequiredViews() throws Exception {
        // When - Created
        // Then
        assertThat(email_input.getVisibility(),         equalTo(View.VISIBLE));
        assertThat(email_label.getVisibility(),         equalTo(View.VISIBLE));
        assertThat(forgotPassButton.getVisibility(),    equalTo(View.VISIBLE));
    }

    @Ignore
    @Test
    public void clickingSubmit_shouldStartLoginActivity_ifValidEmail() throws Exception {
        // Given - Valid Email
        email_input.setText(MockUser.email);

        // When
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                forgotPassButton.performClick();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // And
        ShadowApplication.runBackgroundTasks();
        Thread.sleep(10000);
        Robolectric.flushBackgroundThreadScheduler();

        // Then
        Intent expectedIntent = new Intent(forgotPassActivity, Login.class);
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        assertThat(expectedIntent.getComponent(), is(actual.getComponent()));
        assertThat(expectedIntent.getExtras().size(), is(1));
        assertThat(expectedIntent.getExtras().getString("toast"), is(SUCCESS_MESSAGE));
        // TODO [shantanu] add email sent assertion once functionality is extracted to Validation Service
    }

    @Ignore
    @Test
    public void clickingSubmit_shouldNotify_ifProblemSendingEmail() throws Exception {
        // Given - Valid Email
        email_input.setText("not_a_valid@email.com");

        // When
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                forgotPassButton.performClick();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // And
        ShadowApplication.runBackgroundTasks();
        Thread.sleep(10000);
        Robolectric.flushBackgroundThreadScheduler();

        // Then
        assertThat( ShadowToast.getTextOfLatestToast(), equalTo(FAILURE_MESSAGE));
    }

    @Test
    public void clickingSubmit_shouldNotify_ifInvalidEmail() throws Exception {
        // Given - Invalid Email
        email_input.setText("invalid_email");

        // When
        forgotPassButton.performClick();

        // Then
        assertThat( ShadowToast.getTextOfLatestToast(), equalTo(INVALID_EMAIL_MESSAGE));

        // And
        // Given - On-Wisc Email
        email_input.setText("not_a_wisc@email.com");

        // When
        forgotPassButton.performClick();

        // Then
        assertThat( ShadowToast.getTextOfLatestToast(), equalTo(INVALID_EMAIL_MESSAGE));
    }

}
