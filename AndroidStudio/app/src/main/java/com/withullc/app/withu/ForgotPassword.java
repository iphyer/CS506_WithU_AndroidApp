package com.withullc.app.withu;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
    }

    /**
     * Called when the user taps the Submit button
     * */
    // TODO add waiting spinner
    // TODO move logic to Validation Service
    public void submit(View view) {
        final ForgotPassword that = this;
        String email = ((EditText)findViewById(R.id.editText)).getText().toString().trim();
        if(android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() && email.split("@")[1].equals("wisc.edu")) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("ForgotPassword", "Email sent.");
                                Intent intent = new Intent(that, Login.class);
                                intent.putExtra("toast", getApplicationContext().getText(R.string.forgot_password_success));
                                startActivity(intent);
                                finish();
                            } else {
                                Log.d("ForgotPassword", "Email not sent.");
                                Toast.makeText(that, R.string.forgot_password_failure, Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        } else {
            Log.d("ForgotPassword", "Invalid email.");
            Toast.makeText(that, R.string.forgot_password_invalid, Toast.LENGTH_LONG).show();
        }
    }
}
