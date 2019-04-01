package com.example.kalenderapp_reborn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements SessionManager.HttpResponseInterface {

    private static final String TAG = "LoginActivity";

    private EditText editText_email, editText_password;
    private SessionManager sessionManager = new SessionManager();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText_email = findViewById(R.id.editText_email);
        editText_password = findViewById(R.id.editText_password);
        sessionManager.setOnHttpResponseListener(this);
    }

    public void gotoSignup(View view){
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
    }

    public void doLogin(View view){
        sessionManager.testMethod();
    }


    private void startupActivity(){
        // This starts the main calendar activity
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    @Override
    public void onHttpResponse(String jsonResponse) {
        // When HTTP sends a response
        Log.d(TAG, "onHttpResponse: a response!");
    }
}
