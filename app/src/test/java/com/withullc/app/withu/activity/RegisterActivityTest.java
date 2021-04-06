package com.withullc.app.withu.activity;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;

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

import static com.withullc.app.withu.utils.TestUtils.MockUser;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * Created by shantanusinghal on 31/10/17.
 */

public class RegisterActivityTest extends BaseRobolectricTest {

    private Register registerActivity;
    private EditText first_name_input;
    private View first_name_label;
    private EditText last_name_input;
    private View last_name_label;
    private EditText email_input;
    private View email_label;
    private RadioButton male_radio_button;
    private RadioButton female_radio_button;
    private View gender_label;
    private EditText phone_input;
    private View phone_label;
    private EditText password_input;
    private View password_label;
    private EditText confirm_input;
    private View confirm_label;
    private CheckBox is_walker_checkbox;
    private View register_button;

    @Before
    public void setUp() throws Exception {
        FakeHttp.getFakeHttpLayer().interceptHttpRequests(false);
        // Build Activity
        registerActivity = Robolectric.buildActivity(Register.class).create().get();
        // Collect Views
        first_name_input = registerActivity.findViewById(R.id.register_first_name);
        first_name_label = registerActivity.findViewById(R.id.register_label_first_name);
        last_name_input = registerActivity.findViewById(R.id.register_last_name);
        last_name_label = registerActivity.findViewById(R.id.register_label_last_name);
        email_input = registerActivity.findViewById(R.id.register_email_address);
        email_label = registerActivity.findViewById(R.id.register_label_email);
        male_radio_button = registerActivity.findViewById(R.id.register_gender_male);
        female_radio_button = registerActivity.findViewById(R.id.register_gender_female);
        gender_label = registerActivity.findViewById(R.id.register_label_gender);
        phone_input = registerActivity.findViewById(R.id.register_phone_number);
        phone_label = registerActivity.findViewById(R.id.register_label_phone);
        password_input = registerActivity.findViewById(R.id.register_password);
        password_label = registerActivity.findViewById(R.id.register_label_pasword);
        confirm_input = registerActivity.findViewById(R.id.register_confirm_password);
        confirm_label = registerActivity.findViewById(R.id.register_label_confirm_password);
        is_walker_checkbox = registerActivity.findViewById(R.id.register_as_walker);
        register_button = registerActivity.findViewById(R.id.register_register);
    }

    @Test
    public void itShouldHaveTheRequiredViews() throws Exception {
        // When - Created
        // Then
        assertThat(first_name_input.getVisibility(),     equalTo(View.VISIBLE));
        assertThat(first_name_label.getVisibility(),     equalTo(View.VISIBLE));
        assertThat(last_name_input.getVisibility(),      equalTo(View.VISIBLE));
        assertThat(last_name_label.getVisibility(),      equalTo(View.VISIBLE));
        assertThat(email_input.getVisibility(),          equalTo(View.VISIBLE));
        assertThat(email_label.getVisibility(),          equalTo(View.VISIBLE));
        assertThat(male_radio_button.getVisibility(),    equalTo(View.VISIBLE));
        assertThat(female_radio_button.getVisibility(),  equalTo(View.VISIBLE));
        assertThat(gender_label.getVisibility(),         equalTo(View.VISIBLE));
        assertThat(phone_input.getVisibility(),          equalTo(View.VISIBLE));
        assertThat(phone_label.getVisibility(),          equalTo(View.VISIBLE));
        assertThat(password_input.getVisibility(),       equalTo(View.VISIBLE));
        assertThat(password_label.getVisibility(),       equalTo(View.VISIBLE));
        assertThat(confirm_input.getVisibility(),        equalTo(View.VISIBLE));
        assertThat(confirm_label.getVisibility(),        equalTo(View.VISIBLE));
        assertThat(is_walker_checkbox.getVisibility(),   equalTo(View.VISIBLE));
        assertThat(register_button.getVisibility(),      equalTo(View.VISIBLE));
    }

    @Ignore
    @Test
    public void clickingRegister_shouldStartUserMainActivity_forUser_ifFormValid() throws Exception {
        // Given - Valid Form
        first_name_input.setText(MockUser.fName);
        last_name_input.setText(MockUser.lName);
        email_input.setText(MockUser.email);
        male_radio_button.setChecked(MockUser.isMale);
        female_radio_button.setChecked(MockUser.isFemale);
        phone_input.setText(MockUser.phone);
        password_input.setText(MockUser.password);
        confirm_input.setText(MockUser.password);
        is_walker_checkbox.setChecked(MockUser.isWalker);

        // When
        new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                register_button.performClick();
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        // And
        ShadowApplication.runBackgroundTasks();
        Thread.sleep(10000);
        Robolectric.flushBackgroundThreadScheduler();

        // Then
        Intent expectedIntent = new Intent(registerActivity, UserMain.class);
        Intent actual = ShadowApplication.getInstance().getNextStartedActivity();
        assertThat(expectedIntent.getComponent(), is(actual.getComponent()));
    }

}
