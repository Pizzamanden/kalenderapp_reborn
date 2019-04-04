package com.example.kalenderapp_reborn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.kalenderapp_reborn.supportclasses.SessionManager;

import org.joda.time.DateTime;

public class LoginActivity extends AppCompatActivity{

    private static final String TAG = "LoginActivity";

    private EditText editText_email, editText_password;
    //private SessionManager sessionManager = new SessionManager(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText_email = findViewById(R.id.editText_email);
        editText_password = findViewById(R.id.editText_password);
        DateTime dateTime = new DateTime();
        Log.d(TAG, "onCreate: " + dateTime);
        Log.d(TAG, "onCreate: " + dateTime.toString());
        //sessionManager.setSessionManagerListener(this);
    }

    public void gotoSignup(View view){
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
    }

    public void doLogin(View view){

    }


    private void startMainActivity(){
        // This starts the main calendar activity
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }
}
