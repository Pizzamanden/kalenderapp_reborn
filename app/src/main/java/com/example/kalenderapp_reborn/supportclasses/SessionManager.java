package com.example.kalenderapp_reborn.supportclasses;

import android.os.Handler;
import android.util.Log;

import com.example.kalenderapp_reborn.EventAddActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SessionManager{

    private static final String SAVED_AUTH_TOKEN = "savedToken";
    private static final String TAG = "SessionManager";
    private HttpResponseInterface mCallback;

    public SessionManager(){

    }

    public void testMethod(){
        mCallback.onHttpResponse("yaaadssddsdsdsd");
    }

    public boolean writePrefs(String jsonWebToken){

        return false;
    }

    public boolean checkPrefs(String jsonWebToken){

        return false;
    }

    public boolean validateToken(){
        // Token should be saved as string in Prefs

        return false;
    }

    private void httpCall(String requestName, String jsonToSend){
        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuilder to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder(requestName, jsonToSend);
        // Make call on client with request
        Log.d(TAG, "setAddEntryView: making call");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "setAddEntryView: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    Log.d(TAG, "setAddEntryView: 200");
                    mCallback.onHttpResponse(myResponse);
                }
            }
        });
    }

    public interface HttpResponseInterface {
        void onHttpResponse(String jsonResponse);
    }

    public void setOnHttpResponseListener(HttpResponseInterface callback)
    {
        this.mCallback = callback;
    }
}
