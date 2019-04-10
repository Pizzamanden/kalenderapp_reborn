package com.example.kalenderapp_reborn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.example.kalenderapp_reborn.fragments.CounterDialog;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;

import org.joda.time.DateTime;

public class LoginActivity extends AppCompatActivity implements SessionManager.SessionManagerHttpResponse{

    private static final String TAG = "LoginActivity";

    private EditText editText_email, editText_password;
    private SessionManager sessionManager = new SessionManager(this);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText_email = findViewById(R.id.editText_email);
        editText_password = findViewById(R.id.editText_password);
        DateTime dateTime = new DateTime();
        Log.d(TAG, "onCreate: " + dateTime);
        Log.d(TAG, "onCreate: " + dateTime.toString());
        sessionManager.setSessionManagerListener(this);
    }

    public void gotoSignup(View view){
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
    }

    public void doLogin(View view){
        // Start doing a login, with the information from the views.
    }

    private void startMainActivity(){
        // This starts the main calendar activity
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onHttpResponse(String responseCode, String jsonResponse) {

    }








}
