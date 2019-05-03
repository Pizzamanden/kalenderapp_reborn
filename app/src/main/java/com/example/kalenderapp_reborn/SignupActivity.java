package com.example.kalenderapp_reborn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.example.kalenderapp_reborn.dataobjects.SignupQuery;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.google.gson.Gson;

public class SignupActivity extends AppCompatActivity implements HttpRequestBuilder.HttpRequestResponse {

    private static final String TAG = "SignupActivity";
    private static final String SIGNUP_USER_REQUEST = "signup_user_request";

    EditText editText_firstname, editText_email, editText_password, editText_passwordRepeat;
    CheckBox checkBox_rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editText_firstname = findViewById(R.id.editText_firstname);
        editText_email = findViewById(R.id.editText_email);
        editText_password = findViewById(R.id.editText_password);
        editText_passwordRepeat = findViewById(R.id.editText_passwordRepeat);
        checkBox_rememberMe = findViewById(R.id.checkBox_rememberMe);
    }

    public void gotoLogin(View view){
        finish();
    }

    public void doSignup(View view){
        String email = editText_email.getText().toString();
        String firstname = editText_firstname.getText().toString();
        String password = editText_password.getText().toString();
        String passwordRepeat = editText_passwordRepeat.getText().toString();


        // Client-side validate fields
        if(validateFields(email, firstname, password, passwordRepeat)){
            Log.d(TAG, "doSignup: Verified");
        }

        SignupQuery signupQuery = new SignupQuery(email, firstname, password);
        String json = new Gson().toJson(signupQuery);

        Log.d(TAG, "doSignup: " + json);

        HttpRequestBuilder requestBuilder =
                new HttpRequestBuilder(this, this, "http://www.folderol.dk/")
                .postBuilder("signup_user", json, SIGNUP_USER_REQUEST);
        requestBuilder.makeHttpRequest();
    }

    private boolean validateFields(String email, String firstname, String password, String passwordRepeat){
        int nameMinLength = 1;
        int emailMinLength = 1;
        int passwordMinLength = 1;

        if(email.length() < emailMinLength){
            // Email invalid
            // TODO run email validation
            return createErrToast(getResources().getString(R.string.signup_val_email_tooshort));
        }
        if(firstname.length() < nameMinLength){
            // Name is shorter than 1
            return createErrToast(getResources().getString(R.string.signup_val_firstname_empty));
        }
        if(!password.equals(passwordRepeat)) {
            // Passwords dosen't match
            return createErrToast(getResources().getString(R.string.signup_val_password_nomatch));
        } else {
            if (password.length() < passwordMinLength) {
                // Password too short
                return createErrToast(getResources().getString(R.string.signup_val_password_tooshort));
            }
        }
        return true;
    }

    private boolean createErrToast(String toastText){
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onHttpRequestResponse(int responseCode, String responseJson, String requestName) {
        Log.d(TAG, "onHttpRequestResponse: Fired");
        Log.d(TAG, "onHttpRequestResponse: " + responseCode);
        if(requestName.equals(SIGNUP_USER_REQUEST)){
            Log.d(TAG, "onHttpRequestResponse: " + responseJson);
            // TODO act on user signed in (ALREADY WORKS)
        }
    }
}
