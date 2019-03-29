package com.example.kalenderapp_reborn;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private EditText editText_email, editText_password;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editText_email = findViewById(R.id.editText_email);
        editText_password = findViewById(R.id.editText_password);

        // Check for eligible login existence
        SharedPreferences pref = getApplicationContext().getSharedPreferences("LoginStatus", MODE_PRIVATE);
        if(pref.getString("token_timestamp", null) != null){
            Log.d(TAG, "onCreate: " + "Timestamp found");
            // A timestamp is found, check legit-ness
            validatePrefInfo();
        } else {
            Log.d(TAG, "onCreate: " + "No Timestamp found");
        }

    }

    private void validatePrefInfo(){


        // If it checks out in format and dates and such, check with server
        checkPrefsHttp();
    }

    public void gotoSignup(View view){
        Intent i = new Intent(this, SignupActivity.class);
        startActivity(i);
    }

    public void doLogin(View view){
        Log.d(TAG, "doLogin: " + "Attempt login");
        // Login button is clicked, initiate login

        // Build json string
        String jsonString = "" +
                "\"request\":" +
                "login" +
                "\",\"email\":\"" +
                editText_email.getText() +
                "\", \"password\":\"" +
                editText_password.getText() +
                "\"}";

        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuilder to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder("checkInfo", jsonString);
        // Make call on client with request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "getEventJSON: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Use data to init Recycler View
                            setupPrefs(myResponse);
                        }
                    });
                }
            }
        });
    }

    private void checkPrefsHttp(){
        Log.d(TAG, "checkPrefs: " + "Check prefs");
        // Login button is clicked, initiate login

        // Build json string
        String jsonString = "" +
                "\"request\":" +
                "checkPrefs" +
                "\",\"email\":\"" +
                editText_email.getText() +
                "\", \"password\":\"" +
                editText_password.getText() +
                "\"}";

        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuilder to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder("checkInfo", jsonString);
        // Make call on client with request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "getEventJSON: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    LoginActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Use data to init Recycler View
                            setupPrefs(myResponse);
                        }
                    });
                }
            }
        });
    }

    private void setupPrefs(String httpResponse){

    }
}
