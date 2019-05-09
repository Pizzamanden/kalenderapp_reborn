package com.example.kalenderapp_reborn;

import android.content.Intent;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Toast;

import com.example.kalenderapp_reborn.dataobjects.SignupQuery;
import com.example.kalenderapp_reborn.dataobjects.TokenValidation;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;
import com.google.gson.Gson;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity implements HttpRequestBuilder.HttpRequestResponse, SessionManager.SessionManagerHttpResponse {

    private static final String TAG = "SignupActivity";
    private static final String SIGNUP_USER_REQUEST = "signup_user_request";
    // TODO test regex to full capacity
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"(),:;<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";

    private TextInputLayout editTextLayout_firstname, editTextLayout_email, editTextLayout_password, editTextLayout_passwordRepeat;
    private CheckBox checkBox_rememberMe;
    private Toolbar toolbar;
    private ConstraintLayout contentRoot;
    private RelativeLayout loadingPanel;
    private SessionManager sessionManager;
    private boolean contentReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO signup dosent work 100%, can fail login, but still returns a token???
        setContentView(R.layout.activity_signup);
        editTextLayout_email = findViewById(R.id.editTextLayout_email);
        editTextLayout_firstname = findViewById(R.id.editTextLayout_firstname);
        editTextLayout_password = findViewById(R.id.editTextLayout_password);
        editTextLayout_password.setPasswordVisibilityToggleEnabled(true);
        editTextLayout_passwordRepeat = findViewById(R.id.editTextLayout_passwordRepeat);
        editTextLayout_passwordRepeat.setPasswordVisibilityToggleEnabled(true);
        contentRoot = findViewById(R.id.content_root);
        loadingPanel = findViewById(R.id.loadingPanel);


        editTextLayout_firstname.getEditText().setText("Peter");
        editTextLayout_password.getEditText().setText("password123");
        editTextLayout_passwordRepeat.getEditText().setText("password123");

        checkBox_rememberMe = findViewById(R.id.checkBox_rememberMe);
        sessionManager = new SessionManager(this).setSessionManagerListener(this);
    }

    public void gotoLogin(View view){
        finish();
    }

    public void doSignup(View view){
        contentReady = onLoading();
        String email = editTextLayout_email.getEditText().getText().toString();
        String firstname = editTextLayout_firstname.getEditText().getText().toString();
        String password = editTextLayout_password.getEditText().getText().toString();
        String passwordRepeat = editTextLayout_passwordRepeat.getEditText().getText().toString();
        Log.d(TAG, "doSignup: " + email);
        Log.d(TAG, "doSignup: " + firstname);
        Log.d(TAG, "doSignup: " + password);
        Log.d(TAG, "doSignup: " + passwordRepeat);


        // Client-side validate fields
        if(validateFields(email, firstname, password, passwordRepeat)){
            Log.d(TAG, "doSignup: Verified");
            SignupQuery signupQuery = new SignupQuery(email, firstname, password);
            String json = new Gson().toJson(signupQuery);

            Log.d(TAG, "doSignup: " + json);

            HttpRequestBuilder requestBuilder =
                    new HttpRequestBuilder(this, this, "http://www.folderol.dk/")
                            .postBuilder("signup_user", json, SIGNUP_USER_REQUEST);
            requestBuilder.makeHttpRequest();
        } else {
            Log.d(TAG, "doSignup: No valid :(");
            contentReady = onReady();
        }
    }

    private boolean validateFields(String email, String firstname, String password, String passwordRepeat){
        int nameMinLength = 1;
        int emailMinLength = 4;
        int passwordMinLength = 6;
        Pattern pattern = Pattern.compile(EMAIL_REGEX);
        Matcher matcher = pattern.matcher(email);

        if(email.length() < emailMinLength){
            // Email too short???
            return createErrToast(getResources().getString(R.string.signup_val_email_tooshort));
        } else if(matcher.matches()){
            // Email not valid as email, yell at user
            return createErrToast(getResources().getString(R.string.signup_val_email_notvalid));
        }
        if(firstname.length() < nameMinLength){
            // Name is shorter than 1
            return createErrToast(getResources().getString(R.string.signup_val_firstname_empty));
        }
        if(password.length() == 0){
            // No password entered
            return createErrToast(getResources().getString(R.string.signup_val_password_empty));
        } else if(password.length() < passwordMinLength) {
            // Password too short
            return createErrToast(getResources().getString(R.string.signup_val_password_tooshort));
        } else if(!password.equals(passwordRepeat)) {
            // Passwords dosen't match
            return createErrToast(getResources().getString(R.string.signup_val_password_nomatch));

        }
        return true;
    }

    private boolean createErrToast(String toastText){
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void onSignupResponse(String jsonResponse){
        Log.d(TAG, "onSignupResponse: Fired");
        SignupQuery signupQuery = new Gson().fromJson(jsonResponse, SignupQuery.class);
        SignupQuery.SignupResponse response = signupQuery.getSignupResponse();
        int responseCode = response.getResponseCode();
        String responseString = response.getResponseString();
        Log.d(TAG, "onSignupResponse: " + responseCode);
        Log.d(TAG, "onSignupResponse: " + responseString);
        if(responseCode == 0){
            TokenValidation tokenValidation = response.getTokenValidation();
            if(sessionManager.writeSuccessPrefs(tokenValidation.getJsonWebToken(), tokenValidation.getUserID())){
                sessionManager.runTokenValidation();
            }
        } else {
            // TODO prepare error
            // show error
            contentReady = onReady();
        }
    }

    public boolean onLoading() {
        // Hides content, and shows placeholders/loading icons
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
        Log.d(TAG, "onHttpRequestResponse: Fired");
        Log.d(TAG, "onHttpRequestResponse: " + responseCode);
        if(requestName.equals(SIGNUP_USER_REQUEST)){
            Log.d(TAG, "onHttpRequestResponse: " + responseJson);
            // TODO act on user signed in (ALREADY WORKS)
            // TODO decipher what already works ^
            onSignupResponse(responseJson);
        }
    }

    @Override
    public void onTokenStatusResponse(int responseCode, String responseString) {
        if (responseCode == 0) {
            // Success!
            Log.d(TAG, "onTokenStatusResponse: Token validated, starting activity now");
            Log.d(TAG, "onTokenStatusResponse: " + sessionManager.getUserID());
            Log.d(TAG, "onTokenStatusResponse: " + sessionManager.getToken());
            sessionManager.startMainActivity();
        } else {
            // Some kind of failure
            Log.d(TAG, "onTokenStatusResponse: Error: " + responseString);
            Log.d(TAG, "onTokenStatusResponse: Code: " + responseCode);
            // While the user got a response from their signup, when checking the token again (as i do in login)
            sessionManager.startLoginActivity();
        }
    }
}
