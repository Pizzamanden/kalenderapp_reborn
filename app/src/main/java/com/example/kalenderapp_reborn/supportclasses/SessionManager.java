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

    private static final String TOKEN_SHARED_PREFERENCES = "TOKEN_SHARED_PREFS";
    private static final String TOKEN_KEY = "TOKEN_AS_STRING";
    private static final String TOKEN_INVALIDATION = "TOKEN_INVALIDATION";
    private static final String USER_ID = "USER_ID";
    private static final String TOKEN_LAST_VALIDATED = "TOKEN_LAST_VALIDATED";
    // Shared prefs
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private SessionManagerHttpResponse mCallback;


    public SessionManager(Context context){
        sharedPref = context.getSharedPreferences(TOKEN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    /*

    Response codes to send other parts of code:
    0: success, token valid and sent to sharedprefs
    1: server issue, token fetch not available
    2: failure, token marked as invalid


     */


    // SETTERS
    private boolean setToken(String jsonWebToken){
        editor.putString(TOKEN_KEY, jsonWebToken);
        return editor.commit();
    }

    private boolean setUserID(int userID){
        editor.putInt(USER_ID, userID);
        return editor.commit();
    }

    private boolean setLastValidation(String lastValidationAsString){
        editor.putString(TOKEN_LAST_VALIDATED, lastValidationAsString);
        return editor.commit();
    }

    private boolean setInvalidator(boolean invalidatorValue){
        editor.putBoolean(TOKEN_INVALIDATION, invalidatorValue);
        return editor.commit();
    }

    // GETTERS

    private String getToken(){
        return sharedPref.getString(TOKEN_KEY, null);
    }

    private int getUserID(){
        return sharedPref.getInt(USER_ID, -1);
    }

    private String getLastValidation(){
        return sharedPref.getString(TOKEN_LAST_VALIDATED, null);
    }

    public boolean getInvalidator(){
        return sharedPref.getBoolean(TOKEN_INVALIDATION, false);
    }


    // TODO make user ID redundant, and only use value stored in web token, since i should be able to trust those
    /**
     * This method is for writing prefs
     * either because a token was found non-valid
     * or a login has never been done
     *
     * This method should only in relations with an SUCCESSFUL server side validation
     *
     * @param jsonWebToken Web token as "plaintext" string
     * @param userID The ID of the user. Since nothing in a web token is safe, just store this as plaintext. Should be redundant in the end
     * @return boolean as true if commit to editor was successful
     */
    private boolean writeSuccessPrefs(String jsonWebToken, int userID){
        DateTime dateTime = new DateTime();

        // Justification of commit, and not apply:
        // VERY small prefs, only used with 5-6 lines max, and one per app installation
        // Commit gives me security, that 1 web call is all i need
        // (Other than the token-based login i make

        if(!setToken(jsonWebToken)){
            // Error
            return false;
        } else if(!setInvalidator(false)){
            // Also error
            return false;
        } else if(!setLastValidation(dateTime.toString())){
            // Another error
            return false;
        } else if(!setUserID(userID)){
            // This should be fin... no wait, error again -_-
            return false;
        } else {
            // Success!
            return true;
        }
    }

    /**
     * This method writes in the prefs
     *
     * This method should only in relations with an FAILED server side validation
     * It should then set a date for last validation datetime, and mark token as invalid
     *
     * @return boolean as true if commit to editor was successful
     */
    private boolean writeFailurePrefs(){
        DateTime dateTime = new DateTime();
        if(!setInvalidator(false)){
            // Error
            return false;
        } else if(!setLastValidation(dateTime.toString())){
            // Also error
            return false;
        } else {
            // Success!
            return true;
        }
    }

    public void runTokenValidation(){
        if(clientSideTokenValidation()){
            // Local validation success
            // Now send token to server to validate

            TokenValidation tokenValidation = new TokenValidation(getLastValidation(), getToken(), getUserID());
            String json = new Gson().toJson(tokenValidation);

            // Make call
            serverSideTokenValidation("verify_token", json);
        } else {
            // Local validation failed because:
            // No token
            // Token was marked as invalid

            // This is not the place to make the validation, but to act on already done validation (see clientSideTokenValidation)

            mCallback.onTokenStatusResponse(2, "Breaking news: Local token marked as invalid");

            // A login should be forced on user
            // TODO implement this
        }
    }

    /**
     *
     * @return Returns true upon verified no invalidator is active, and some token is stored on the device
     */
    private boolean clientSideTokenValidation(){
        // Token should be saved as string in Prefs

        // this should return true, to mark token as found and maybe valid

        // Along with the token, a boolean should be saved to quick-invalidate a token
        // Quick invalidate does nothing for security, it only makes me NOT make a server call (for validity) when client was kicked off, but didn't log in again after
        if(getToken() == null){
            // A token was not found, short circuit validation
            return false;
        } else if(sharedPref.getBoolean(TOKEN_INVALIDATION,false)){
            // An invalidator was found, and it was set to true
            // Because default (not found) is false, and false means not-invalidated
            // To be true, it MUST be set, and MUST be true
            return false;
        } else {
            // Because a token was found, and it was not null, plus an invalidator was not found, or not set to true,
            // we then need to run server validation on token-legitness
            return true;
        }
    }



    private void serverSideTokenValidation(String requestName, String jsonToSend){
        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuilder to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder(requestName, jsonToSend);
        // Make call on client with request
        Log.d(TAG, "serverSideTokenValidation: making call");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "serverSideTokenValidation: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    Log.d(TAG, "serverSideTokenValidation: 200");

                    // Send response to onRequestResponse to process response
                    onRequestResponse(myResponse);
                }
            }
        });
    }

    private void onRequestResponse(String httpResponse){
        // This method is a in-between when the request gets a response, and before anything else touches it, to see what server response was
        // If server response was negative, a false boolean should be set on TOKEN_INVALIDATION
        // If server response was positive, the new returned token should be put in old tokens place, and TOKEN_INVALIDATION should be set to true
        // This way, if a token was non-valid, and user does not login after being put in login screen, it will be easy to read login status


        // Map response to TokenValidation with Gson
        TokenValidation tokenValidation = new Gson().fromJson(httpResponse, TokenValidation.class);
        Log.d(TAG, "onRequestResponse: " + tokenValidation.getJsonWebToken());
        Log.d(TAG, "onRequestResponse: " + tokenValidation.getUserID());
        Log.d(TAG, "onRequestResponse: " + tokenValidation.getValidationStatus());

        //mCallback.onTokenStatusResponse();
    }

    public interface SessionManagerHttpResponse {
        void onTokenStatusResponse(int responseCode, String responseString);
    }

    public void setSessionManagerListener(SessionManagerHttpResponse callback) {
        this.mCallback = callback;
    }
}
