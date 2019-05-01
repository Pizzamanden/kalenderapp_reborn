package com.example.kalenderapp_reborn;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.kalenderapp_reborn.dataobjects.LoginFormula;
import com.example.kalenderapp_reborn.dataobjects.TokenValidation;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;
import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity implements SessionManager.SessionManagerHttpResponse, HttpRequestBuilder.HttpRequestResponse{

    private static final String TAG = "LoginActivity";
    private static final String HTTP_PERFORM_USER_LOGIN = "user_login_validation";

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
        Log.d(TAG, "onCreate: Lifecycle");

        // Views
        editText_email = findViewById(R.id.editText_email);
        editText_password = findViewById(R.id.editText_password);
        contentRoot = findViewById(R.id.content_root);
        loadingPanel = findViewById(R.id.loadingPanel);
        contentReady = onLoading();


        sessionManager = new SessionManager(this).setSessionManagerListener(this);
        sessionManager.runTokenValidation();
    }

    @Override
    protected void onStop() {
        super.onStop();
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

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(this, "http://www.folderol.dk/")
                .postBuilder("login_user", json, HTTP_PERFORM_USER_LOGIN)
                .setHttpResponseListener(this);
        requestBuilder.makeHttpRequest();
    }

    private void onLoginStatusResponse(final String jsonResponse){
        TokenValidation validatedToken = new Gson().fromJson(jsonResponse, TokenValidation.class);

        // Show toast with information about problems
        Toast.makeText(LoginActivity.this, validatedToken.getValidationMessage(),
                Toast.LENGTH_LONG).show();

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
    public void onTokenStatusResponse(final int responseCode, final String responseText) {
        // TODO show user some kind of error (on error ofc)
        if(responseCode == 0){
            // Success!
            Toast.makeText(LoginActivity.this, responseText,
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "onTokenStatusResponse: Token validated, starting activity now");
            Log.d(TAG, "onTokenStatusResponse: " + sessionManager.getUserID());
            Log.d(TAG, "onTokenStatusResponse: " + sessionManager.getToken());
            startMainActivity();
        } else {
            // Some kind of failure
            Toast.makeText(LoginActivity.this, responseText,
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "onTokenStatusResponse: Error: " + responseText);
            Log.d(TAG, "onTokenStatusResponse: Code: " + responseCode);
            contentReady = onReady();
        }
    }

    private void startMainActivity(){
        // This starts the main calendar activity
        Intent i = new Intent(this, CalendarListActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(i);
        finish();
        overridePendingTransition(0,0);
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


    @Override
    public void onHttpRequestResponse(int responseCode, String responseMessage, String requestName) {
        if(requestName.equals(HTTP_PERFORM_USER_LOGIN)){
            onLoginStatusResponse(responseMessage);
        }
    }
}
