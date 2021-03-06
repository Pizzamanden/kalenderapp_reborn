package com.example.kalenderapp_reborn.supportclasses;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.example.kalenderapp_reborn.CalendarListActivity;
import com.example.kalenderapp_reborn.LoginActivity;
import com.example.kalenderapp_reborn.dataobjects.TokenValidation;
import com.google.gson.Gson;

import org.joda.time.DateTime;

public class SessionManager implements HttpRequestBuilder.HttpRequestResponse{

    private static final String TAG = "SessionManager";

    public static final String SERVER_SIDE_TOKEN_VALIDATION = "server_side_token_validation";

    private static final String RETURN_MESSAGE_0 = "Success!";
    private static final String RETURN_MESSAGE_1 = "An unexpected error occurred with the server. Please try again later.";
    private static final String RETURN_MESSAGE_2 = "Session expired.";
    private static final String RETURN_MESSAGE_3 = "Please log in.";
    private static final String RETURN_MESSAGE_4 = "Session expired.";
    private static final String RETURN_MESSAGE_5 = "Success!";

    private static final String HTTP_TOKEN_CHECK = "httptokencheck";

    private static final String TOKEN_SHARED_PREFERENCES = "TOKEN_SHARED_PREFS";
    private static final String TOKEN_KEY = "TOKEN_AS_STRING";
    private static final String TOKEN_INVALIDATION = "TOKEN_INVALIDATION";
    private static final String USER_ID = "USER_ID";
    private static final String TOKEN_LAST_VALIDATED = "TOKEN_LAST_VALIDATED";
    // Shared prefs
    private SharedPreferences sharedPref;
    private SharedPreferences.Editor editor;

    private SessionManagerHttpResponse mCallback;

    private Context mContext;


    public SessionManager(Context context){
        mContext = context;
        sharedPref = mContext.getSharedPreferences(TOKEN_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        editor = sharedPref.edit();
    }

    /*

    Response codes to send other parts of code:
    0: success, token valid and sent to sharedprefs
    1: server issue, token fetch not available
    2: failure, token was marked as invalid
    3: failure, no token found
    4: failure, token was marked by server as invalid


     */


    // SETTERS
    private boolean setToken(String jsonWebToken){
        editor.putString(TOKEN_KEY, jsonWebToken);
        return editor.commit();
    }

    private boolean setLastValidation(String lastValidationAsString){
        editor.putString(TOKEN_LAST_VALIDATED, lastValidationAsString);
        return editor.commit();
    }

    private boolean setInvalidator(boolean invalidatorValue){
        editor.putBoolean(TOKEN_INVALIDATION, invalidatorValue);
        Log.d(TAG, "setInvalidator: Set as " + invalidatorValue);
        return editor.commit();
    }

    // GETTERS

    public String getToken(){
        return sharedPref.getString(TOKEN_KEY, null);
    }

    public String getLastValidation(){
        return sharedPref.getString(TOKEN_LAST_VALIDATED, null);
    }

    public boolean getInvalidator(){
        return sharedPref.getBoolean(TOKEN_INVALIDATION, false);
    }

    /**
     * This method is for writing prefs
     * either because a token was found non-valid
     * or a login has never been done
     *
     * This method should only in relations with an SUCCESSFUL server side validation
     *
     * @param jsonWebToken Web token as "plaintext" string
     * @return boolean as true if commit to editor was successful
     */
    public boolean writeSuccessPrefs(String jsonWebToken){
        Log.d(TAG, "writeSuccessPrefs: fired");
        DateTime dateTime = new DateTime();

        // Justification of commit, and not apply:
        // VERY small prefs, only used with 5-6 lines max, and one per app installation
        // Commit gives me security, that 1 web call is all i need
        // (Other than the token-based login im making)

        if(!setToken(jsonWebToken)){
            // Error
            return false;
        } else if(!setInvalidator(false)){
            // Also error
            return false;
        } else if(!setLastValidation(dateTime.toString())){
            // Another error
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
    public boolean writeFailurePrefs(){
        DateTime dateTime = new DateTime();
        if(!setInvalidator(true)){
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
        Log.d(TAG, "runTokenValidation: Fired");
        if(clientSideTokenValidation()){
            Log.d(TAG, "runTokenValidation: success");
            // Local validation success
            // Now send token to server to validate

            TokenValidation tokenValidation = new TokenValidation(getLastValidation(), getToken());
            String json = new Gson().toJson(tokenValidation);

            // Make call
            serverSideTokenValidation("verify_token", json);
        } else {
            Log.d(TAG, "runTokenValidation: failure");
            // Local validation failed because:
            // No token
            // Token was marked as invalid

            // This is not the place to make the validation, but to act on already done validation (see clientSideTokenValidation)
            if(writeFailurePrefs()){
                // Writing prefs success, kick em to login
            } else {
                // Writing prefs failed, kick em to login anyway, but maybe notify some kind of server?
            }
            startLoginActivity();
        }
    }

    /**
     *
     * @return Returns true upon verified no invalidator is active, and some token is stored on the device
     */
    private boolean clientSideTokenValidation(){
        Log.d(TAG, "clientSideTokenValidation: Fired");
        // Token should be saved as string in Prefs

        // this should return true, to mark token as found and maybe valid

        // Along with the token, a boolean should be saved to quick-invalidate a token
        // Quick invalidate does nothing for security, it only makes me NOT make a server call (for validity) when client was kicked off, but didn't log in again after
        if(getToken() == null){
            // A token was not found, short circuit validation
            Log.d(TAG, "clientSideTokenValidation: A token was not found");
            Log.d(TAG, "clientSideTokenValidation: No server connection made");
            // Send response through interface
            runInterfaceOnUi(3, RETURN_MESSAGE_3);
            return false;
        } else if(getInvalidator()){
            // An invalidator was found, and it was set to true
            // Because default (not found) is false, and false means not-invalidated
            // To be true, it MUST be set, and MUST be true
            Log.d(TAG, "clientSideTokenValidation: Token invalidator is active (true), needs new client side login");
            Log.d(TAG, "clientSideTokenValidation: No server connection made");
            runInterfaceOnUi(2, RETURN_MESSAGE_2);
            return false;
        } else {
            Log.d(TAG, "clientSideTokenValidation: success!");
            // Because a token was found, and it was not null, plus an invalidator was not found, or not set to true,
            // we then need to run server validation on token-legitness
            // Send token to server for validation
            return true;
        }
    }



    private void serverSideTokenValidation(String requestName, String jsonToSend){
        Log.d(TAG, "serverSideTokenValidation: Fired");

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(mContext, this, "http://www.folderol.dk/")
                .postBuilder(requestName, jsonToSend, HTTP_TOKEN_CHECK);
        requestBuilder.makeHttpRequest();
    }

    @Override
    public void onHttpRequestResponse(int responseCode, String responseJson, String requestName) {
        if(requestName.equals(HTTP_TOKEN_CHECK)){
            onRequestResponse(responseJson, responseCode);
        }
    }

    private void onRequestResponse(String httpResponse, int httpCode){
        Log.d(TAG, "onRequestResponse: Fired");
        // This method is a in-between when the request gets a response, and before anything else touches it, to see what server response was
        // If server response was negative, a false boolean should be set on TOKEN_INVALIDATION
        // If server response was positive, the new returned token should be put in old tokens place, and TOKEN_INVALIDATION should be set to true
        // This way, if a token was non-valid, and user does not login after being put in login screen, it will be easy to read login status

        if(httpCode != 200 || httpResponse == null){
            // Something went wrong, no response at all, or the code wasn't 200
            Log.d(TAG, "onRequestResponse: Critical error: Http code not 200, instead " + httpCode);
        } else {
            // A usable response was returned

            // Map response to TokenValidation with Gson
            final TokenValidation tokenValidation = new Gson().fromJson(httpResponse, TokenValidation.class);
            Log.d(TAG, "onRequestResponse: " + tokenValidation.getJsonWebToken());
            Log.d(TAG, "onRequestResponse: " + tokenValidation.getValidationStatus());

            // Check for response code
            if(tokenValidation.getValidationStatus() == 1){
                // Success!
                Log.d(TAG, "onRequestResponse: Token validated server side successfully!");
                writeSuccessPrefs(tokenValidation.getJsonWebToken());
                runInterfaceOnUi(tokenValidation.getValidationStatus(), tokenValidation.getValidationMessage());
            } else {
                // Code was not 1, either it was not checked, or it failed
            }
        }
    }

    public void startLoginActivity(){
        // This is called when the token-validation fails somewhere
        // When it fails in client-side, it will be called from runTokenValidation()
        // When it fails in server-side, it will be called from invalidateToken()
        if(mContext instanceof LoginActivity){
            // Checked in login activity, so just return start activity
        } else {
            // Not login activity, start it then
            Activity activity = (Activity) mContext;
            Intent i = new Intent(mContext, LoginActivity.class);
            mContext.startActivity(i);
            activity.finish();
            activity.overridePendingTransition(0,0);
        }
    }

    public void startMainActivity(){
        Activity activity = (Activity) mContext;
        Intent i = new Intent(mContext, CalendarListActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        mContext.startActivity(i);
        activity.finish();
        activity.overridePendingTransition(0,0);
    }

    public void invalidateToken(){
        setInvalidator(true);
        Log.d(TAG, "invalidateToken: Logged out, token_is_invalid set to " + sharedPref.getBoolean(TOKEN_INVALIDATION, false));
        startLoginActivity();
    }

    private void runInterfaceOnUi(final int responseCode, final String responseString){
        Activity activity = (Activity) mContext;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallback.onTokenStatusResponse(responseCode, responseString);
            }
        });
    }

    public interface SessionManagerHttpResponse {
        void onTokenStatusResponse(final int responseCode, final String responseString);
    }

    public SessionManager setSessionManagerListener(SessionManagerHttpResponse callback) {
        this.mCallback = callback;
        return this;
    }
}
