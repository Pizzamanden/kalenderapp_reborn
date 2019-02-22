package com.example.kalenderapp_reborn;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EventViewActivity extends AppCompatActivity {

    private static final String TAG = "EventViewActivity";

    private String mJSONdata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getDataJSON();
        Log.d(TAG, "onCreate: " + mJSONdata);
    }


    public void getDataJSON(){

        String postedRequest = "getevents";
        int userId = 2;
        String userToken = "f213412ui1g2";

        String requestJSON = "{" +
                "\"userId\":" +
                userId +
                ",\"request\":\"" +
                postedRequest +
                "\", \"userToken\":\"" +
                userToken +
                "\"}";

        OkHttpClient client = new OkHttpClient();
        Log.d(TAG, "httpPOSTdata: making client");
        String url = "http://www.folderol.dk/";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("retrieveJSON", requestJSON)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Log.d(TAG, "httpPOSTdata: making call");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "httpPOSTdata: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    Log.d(TAG, "httpPOSTdata: 200");
                    EventViewActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mJSONdata = myResponse;
                        }
                    });
                }
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }
}
