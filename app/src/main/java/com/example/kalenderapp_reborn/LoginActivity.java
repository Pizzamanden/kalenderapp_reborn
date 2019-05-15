package com.example.kalenderapp_reborn;

import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kalenderapp_reborn.dataobjects.LoginQuery;
import com.example.kalenderapp_reborn.dataobjects.TokenValidation;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;
import com.google.gson.Gson;

public class LoginActivity extends AppCompatActivity implements SessionManager.SessionManagerHttpResponse, HttpRequestBuilder.HttpRequestResponse{

    private static final String TAG = "LoginActivity";
    private static final String HTTP_PERFORM_USER_LOGIN = "user_login_validation";

    private TextInputLayout editTextLayout_email, editTextLayout_password;
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
        editTextLayout_email = findViewById(R.id.editTextLayout_email);
        editTextLayout_password = findViewById(R.id.editTextLayout_password);
        editTextLayout_password.setPasswordVisibilityToggleEnabled(true);
        contentRoot = findViewById(R.id.content_root);
        loadingPanel = findViewById(R.id.loadingPanel);
        contentReady = onLoading();

        TextView forgotPassword = findViewById(R.id.textView_forgotPassword);
        forgotPassword.setText(forgotPassword.getText() + " (WIP)");

        // TODO Client side pre-submit error handling, and also post-submit, but pre-servercall error handling
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
        String email = editTextLayout_email.getEditText().getText().toString();
        String password = editTextLayout_password.getEditText().getText().toString();

        LoginQuery loginFormula = new LoginQuery(email, password);
        String json = new Gson().toJson(loginFormula);

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(this, this, "http://www.folderol.dk/")
                .postBuilder("login_user", json, HTTP_PERFORM_USER_LOGIN);
        requestBuilder.makeHttpRequest();
    }

    private void onLoginStatusResponse(final String jsonResponse){
        LoginQuery loginQuery = new Gson().fromJson(jsonResponse, LoginQuery.class);
        TokenValidation validatedToken = loginQuery.getTokenValidation();

        // Show toast with information about problems
        Toast.makeText(LoginActivity.this, validatedToken.getValidationMessage(),
                Toast.LENGTH_LONG).show();

        if(validatedToken.getValidationStatus() == 0){
            // Success!
            Log.d(TAG, "onTokenStatusResponse: Login successful, starting activity now");
            sessionManager.writeSuccessPrefs(validatedToken.getJsonWebToken());
            sessionManager.startMainActivity();
        } else if(validatedToken.getValidationStatus() > 0) {
            // User not found, password dosent match, etc.
            // TODO pop a toast or something here, some kind of "no bueno" needs to appear to the user
            contentReady = onReady();
        }
    }

    @Override
    public void onTokenStatusResponse(final int responseCode, final String responseString) {
        // TODO show user some kind of error (on error ofc)
        if(responseCode == 0){
            // Success!
            Toast.makeText(LoginActivity.this, responseString,
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "onTokenStatusResponse: Token validated, starting activity now");
            Log.d(TAG, "onTokenStatusResponse: " + sessionManager.getToken());
            sessionManager.startMainActivity();
        } else {
            // Some kind of failure
            Toast.makeText(LoginActivity.this, responseString,
                    Toast.LENGTH_LONG).show();
            Log.d(TAG, "onTokenStatusResponse: Error: " + responseString);
            Log.d(TAG, "onTokenStatusResponse: Code: " + responseCode);
            contentReady = onReady();
        }
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
    public void onHttpRequestResponse(int responseCode, String responseJson, String requestName) {
        if(requestName.equals(HTTP_PERFORM_USER_LOGIN)){
            onLoginStatusResponse(responseJson);
        }
    }
}
