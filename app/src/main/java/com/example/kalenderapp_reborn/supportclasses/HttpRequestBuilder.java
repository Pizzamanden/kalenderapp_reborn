package com.example.kalenderapp_reborn.supportclasses;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


/**
 * HOW THIS WORKS:
 *
 * You feed it:
 * an url
 * a json string
 * an identifier as string (to recognise result)
 * a query type (like login, tokencheck or just query)
 *
 * then you build the object:
 * Set the url in construct
 * Also give it context (it needs to run result on UI thread)
 *
 * then run XBuilder
 * where X is the type you need
 *
 * either:
 * multiple $_GET, or singular
 * multiple $_POST, or singular
 *
 * when one has been run, you need to set listener for response (interface)
 *
 * Then run makeHttpRequest on the instance
 *
 * then compare in interface using the identifier (a static final private string)
 * to find result
 * if message is null, it means return code wasn't 200
 * if code isn't 200, something is wrong
 *
 */
public class HttpRequestBuilder {

    final static private String TAG = "HttpRequestBuilder";

    private String url;
    private Context mContext;

    private Request currentRequest;
    private String requestName;

    private HttpRequestResponse mCallback;

    public HttpRequestBuilder(Context context, String siteURL){
        url = siteURL;
        mContext = context;
    }


    // Singular get
    public HttpRequestBuilder getBuilder(String getKey, String getValue, String requestName){
        // Set request Name:
        this.requestName = requestName;
        // Build url string
        String concatUrl = getUrl() + "?" + getKey + "=" + getValue;
        // Build request with url string
        Request request = new Request.Builder()
                .url(concatUrl)
                .build();
        // Return Request
        this.currentRequest = request;
        return this;
    }

    // Singular post
    public HttpRequestBuilder postBuilder(String postKey, String postValue, String requestName){
        // Set request Name:
        this.requestName = requestName;
        // Build request body
        // Only needs requestname and value
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart(postKey, postValue)
                .build();
        // Build request
        Request request = new Request.Builder()
                .url(getUrl())
                .post(requestBody)
                .build();
        // Return request
        this.currentRequest = request;
        return this;
    }

    // Arraylist get
    public HttpRequestBuilder getBuilder(ArrayList<String> getKey, ArrayList<String> getValue, String requestName){
        // Set request Name:
        this.requestName = requestName;
        // Build url string
        String concatUrl = getUrl() + "?";
        for(int i = 0; i < getKey.size(); i++){
            // Check if its the first part of the arraylist
            // If not, add a "&" to add more parameters
            if(i == 0){
                concatUrl += getKey.get(i) + "=" + getValue.get(i);
            } else {
                concatUrl += "&" + getKey.get(i) + "=" + getValue.get(i);
            }
        }
        // Build request with url string
        Request request = new Request.Builder()
                .url(concatUrl)
                .build();
        // Return Request
        this.currentRequest = request;
        return this;
    }

    // Arraylist post
    public HttpRequestBuilder postBuilder(ArrayList<String> postKey, ArrayList<String> postValue, String requestName){
        // Set request Name:
        this.requestName = requestName;
        // Start building multipart body
        MultipartBody.Builder mBody = new MultipartBody.Builder();
        mBody.setType(MultipartBody.FORM);
        for(int i = 0; i < postKey.size(); i++){
            mBody.addFormDataPart(postKey.get(i), postValue.get(i));
        }
        // Build Body
        RequestBody requestBody = mBody.build();
        // Build request with body and url
        Request request = new Request.Builder()
                .url(getUrl())
                .post(requestBody)
                .build();
        // Return Request
        this.currentRequest = request;
        return this;
    }

    private String getUrl(){
        return this.url;
    }
    public void setUrl(String url){
        this.url = url;
    }




    // The great holy OKHttp request code block
    // All praise the beauty
    // Never disturb its important work
    // For without it, we are all lost
    public void makeHttpRequest(){
        Log.d(TAG, "makeHttpRequest: Fired");
        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Make call on client with request
        Log.d(TAG, "makeHttpRequest: " + requestName);
        client.newCall(currentRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "makeHttpRequest: onFailure: Fired");
                e.printStackTrace();
                Log.d(TAG, "makeHttpRequest: ");
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "makeHttpRequest: onResponse: Fired");
                Log.d(TAG, "makeHttpRequest: " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "makeHttpRequest: Response: " + myResponse);
                    runInterfaceOnUi(response.code(), myResponse, requestName);
                } else {
                    runInterfaceOnUi(response.code(), null, requestName);
                }
                Log.d(TAG, "makeHttpRequest: ");
            }
        });
    }

    private void runInterfaceOnUi(final int responseCode, final String responseString, final String requestName){
        Activity activity = (Activity) mContext;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCallback.onHttpRequestResponse(responseCode, responseString, requestName);
            }
        });
    }


    public interface HttpRequestResponse {
        void onHttpRequestResponse(final int responseCode, final String responseMessage, final String requestName);
    }

    public HttpRequestBuilder setHttpResponseListener(HttpRequestResponse callback) {
        this.mCallback = callback;
        return this;
    }


}
