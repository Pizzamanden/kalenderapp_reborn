package com.example.kalenderapp_reborn;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

import com.example.kalenderapp_reborn.dataobjects.LoginFormula;
import com.example.kalenderapp_reborn.dataobjects.TokenValidation;
import com.example.kalenderapp_reborn.fragments.CounterDialog;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;
import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements SessionManager.SessionManagerHttpResponse{

    private static final String TAG = "LoginActivity";

    private EditText editText_email, editText_password;
    private Toolbar toolbar;
    private ScrollView contentRoot;
    private RelativeLayout loadingPanel;
    private SessionManager sessionManager;

    private boolean contentReady = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Views
        editText_email = findViewById(R.id.editText_email);
        editText_password = findViewById(R.id.editText_password);
        contentRoot = findViewById(R.id.content_root);
        loadingPanel = findViewById(R.id.loadingPanel);
        contentReady = onLoading();


        sessionManager = new SessionManager(this);
        sessionManager.setSessionManagerListener(this);

        sessionManager.runTokenValidation();
    }

    public void gotoSignup(View view){
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
    }

    public void doLogin(View view){
        // Start doing a login, with the information from the views.
        // Set state as loading
        contentReady = onLoading();

        LoginFormula loginFormula = new LoginFormula(editText_email.getText().toString(), editText_password.getText().toString());
        String json = new Gson().toJson(loginFormula);

        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuilder to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder("login_user", json);
        // Make call on client with request
        Log.d(TAG, "doLogin: making call");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "doLogin: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            onLoginStatusResponse(myResponse);
                        }
                    });
                }
            }
        });
    }

    private void onLoginStatusResponse(String jsonResponse){
        TokenValidation validatedToken = new Gson().fromJson(jsonResponse, TokenValidation.class);
        if(validatedToken.getValidationStatus() == 0){
            // Success!
            Log.d(TAG, "onTokenStatusResponse: Login successful, starting activity now");
            sessionManager.writeSuccessPrefs(validatedToken.getJsonWebToken(), validatedToken.getUserID());
            startMainActivity();
        } else if(validatedToken.getValidationStatus() > 0) {
            // User not found, password dosent match, etc.
            // TODO pop a toast or something here, some kind of "no bueno" needs to appear to the user
            contentReady = onReady();
        }
    }

    @Override
    public void onTokenStatusResponse(int responseCode, String responseText) {
        // TODO show user some kind of error (on error ofc)
        if(responseCode == 0){
            // Success!
            Log.d(TAG, "onTokenStatusResponse: Token validated, starting activity now");
            startMainActivity();
        } else {
            // Some kind of failure
            Log.d(TAG, "onTokenStatusResponse: Error: " + responseText);
            Log.d(TAG, "onTokenStatusResponse: Code: " + responseCode);
            contentReady = onReady();
        }
    }

    private void startMainActivity(){
        // This starts the main calendar activity
        Intent i = new Intent(this, MainActivity.class);
        startActivity(i);
    }

    public boolean onLoading() {
        // Hides content, and shows placeholders/loading icons
        Handler handler = new Handler();
        loadingPanel.setVisibility(View.VISIBLE);
        if(toolbar != null){
            toolbar.setVisibility(View.GONE);
        }
        contentRoot.setVisibility(View.GONE);
        Log.d(TAG, "onLoading: Loadstatus");




        return false;
    }

    public boolean onReady() {
        // Shows content, and hides placeholders/loading icons
        loadingPanel.setVisibility(View.GONE);
        if(toolbar != null){
            toolbar.setVisibility(View.VISIBLE);
        }
        contentRoot.setVisibility(View.VISIBLE);
        Log.d(TAG, "onReady: Loadstatus");
        return true;
    }







}
