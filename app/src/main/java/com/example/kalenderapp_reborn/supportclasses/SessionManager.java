package com.example.kalenderapp_reborn.supportclasses;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.kalenderapp_reborn.dataobjects.TokenValidation;
import com.google.gson.Gson;

import org.joda.time.DateTime;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SessionManager{

    private static final String TAG = "SessionManager";

    private static final String tokenSharedPreferences = "TOKEN_SHARED_PREFS";
    private static final String tokenKey = "TOKEN_AS_STRING";
    private static final String tokenInvalidation = "TOKEN_INVALIDATION";
    private static final String userID = "USER_ID";
    private static final String tokenLastValidated = "TOKEN_LAST_VALIDATED";
    // Shared prefs
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private SessionManagerHttpResponse mCallback;


    public SessionManager(Activity activity){
        sharedPref = activity.getSharedPreferences(tokenSharedPreferences, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }



    /**
     * Step one in validating a user
     * Should check prefs for a token
     * This method can be run to instantly find out if user should be logged-in
     * If this returns null, a login should be forced on the user
     *
     * @return Json web token as string, or null if non-existing
     */
    public String getToken(){
        return sharedPref.getString(tokenKey, null);
    }
    public int getUserID(){
        return sharedPref.getInt(userID, -1);
    }
    public String getLastValidation(){
        return sharedPref.getString(tokenLastValidated, null);
    }



    /**
     * This method is for writing prefs
     * either because a token was found non-valid
     * or a login has never been done
     *
     * This method should only in relations with an http call
     *
     * @param jsonWebToken
     * @return boolean as true if successful
     */
    private boolean writePrefs(String jsonWebToken, boolean invalidate){
        DateTime dateTime = new DateTime();
        editor.putString(tokenKey, jsonWebToken);
        editor.putString(tokenLastValidated, dateTime.toString());
        editor.putBoolean(tokenInvalidation, invalidate);
        return editor.commit();
    }

    public void runTokenValidation(){
        if(this.localTokenValidation()){
            // Local validation success
            // Now send token to server to validate

            TokenValidation tokenValidation = new TokenValidation(getLastValidation(), getToken(), getUserID());
            String json = new Gson().toJson(tokenValidation);

            // Make call
            httpCall("verify_token", json);
        } else {
            // Local validation failed because:
            // No token
            // Token was marked as invalid

            // This is not the place to make the validation, but to act on already done validation (see localTokenValidation)

            mCallback.onHttpResponse("Breaking news: Local token marked as invalid", null);

            // A login should be forced on user
            // TODO implement this
        }
    }

    private boolean localTokenValidation(){
        // Token should be saved as string in Prefs
        // Along with the token, a boolean should be saved to quick-invalidate a token
        if(getToken() == null){
            // A token was not found, short circuit validation
            return false;
        } else if(sharedPref.getBoolean(tokenInvalidation,false)){
            // An invalidator was found, and it was set to true
            // Because default (not found) is false, and false means not-invalidated
            return false;
        } else {
            // Because a token was found, and it was not null, plus an invalidator was not found, or not set to true,
            // we then need to run server validation on token-legitness
            return true;
        }
    }

    private void httpCall(String requestName, String jsonToSend){
        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuilder to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder(requestName, jsonToSend);
        // Make call on client with request
        Log.d(TAG, "httpCall: making call");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "httpCall: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    Log.d(TAG, "httpCall: 200");

                    // Send response to onRequestResponse to process response
                    onRequestResponse(myResponse);
                }
            }
        });
    }

    private void onRequestResponse(String httpResponse){
        // This method is a in-between when the request gets a response, and before anything else touches it, to see what server response was
        // If server response was negative, a false boolean should be set on tokenInvalidation
        // If server response was positive, the new returned token should be put in old tokens place, and tokenInvalidation should be set to true
        // This way, if a token was non-valid, and user does not login after being put in login screen, it will be easy to read login status


        // Find out how httpResponse is formatted
        // httpResponse;
        // Write prefs with according data
        /*if(writePrefs()){

        }*/
        // Then via interface
        String responseCode = "no bueno";

        mCallback.onHttpResponse(responseCode, httpResponse);
    }

    public interface SessionManagerHttpResponse {
        void onHttpResponse(String responseCode, String jsonResponse);
    }

    public void setSessionManagerListener(SessionManagerHttpResponse callback) {
        this.mCallback = callback;
    }
}