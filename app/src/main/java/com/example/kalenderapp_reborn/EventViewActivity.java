package com.example.kalenderapp_reborn;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.example.kalenderapp_reborn.adapters.ViewEventRecyclerAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EventViewActivity extends AppCompatActivity {

    private static final String TAG = "EventViewActivity";
    private Context mContext;
    private String mJSONdata;

    private String[] stringArrayMonths;
    private String[] stringArrayWeekDays;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_view);

        // Setup Views
        mContext = this;

        stringArrayMonths = getResources().getStringArray(R.array.monthss);
        stringArrayWeekDays = getResources().getStringArray(R.array.weekDaysS);

        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        getDataJSON();
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
                            initRecyclerView(myResponse);
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

    private void initRecyclerView(String jsonString){
        Log.d(TAG, "initRecyclerView: " + jsonString);

        ArrayList<String> eventNames = new ArrayList<>();
        ArrayList<String> eventStart = new ArrayList<>();
        ArrayList<String> eventEnd = new ArrayList<>();
        ArrayList<Boolean> eventAlarmStatus = new ArrayList<>();
        ArrayList<String> eventAlarmTime = new ArrayList<>();
        ArrayList<Integer> eventIds = new ArrayList<>();

        // Setup arraylists for recyclerview
        try {
            JSONArray mJSONArray = new JSONArray(jsonString);
            for(int i = 0;i<mJSONArray.length(); i++){
                JSONObject json = mJSONArray.getJSONObject(i);
                eventNames.add(json.getString("event_name"));
                eventStart.add(phpTimeToAndroid(json.getInt("event_start")));
                eventEnd.add(phpTimeToAndroid(json.getInt("event_end")));
                eventAlarmStatus.add(json.getBoolean("event_alarmenable"));
                Log.d(TAG, "initRecyclerView: " + json.getBoolean("event_alarmenable"));
                eventAlarmTime.add(convertAlarmTimeToString(json.getBoolean("event_alarmenable"), json.getInt("event_end"), json.getInt("event_start")));
                eventIds.add(json.getInt("post_id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerview_viewEvents);
        ViewEventRecyclerAdapter adapter = new ViewEventRecyclerAdapter(mContext, eventNames, eventStart, eventEnd, eventIds, eventAlarmStatus, eventAlarmTime);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
    }

    // Method job: Create a stitched date-string to put into views, using only an epoch-time(int/long)
    // Accepts: Takes an int that should function as an unix epoch time unit, either milliseconds or just seconds
    public String phpTimeToAndroid(long epochTime){
        Calendar calToday = Calendar.getInstance();
        Calendar calendar = Calendar.getInstance();
        Date mDate;
        int length = String.valueOf(epochTime).length();
        if(length < 12){
            mDate = new Date(epochTime * 1000);
        } else {
            mDate = new Date(epochTime);
        }
        calendar.setTime(mDate);
        String dateString = "";
        // Week day as 3 letters
        dateString += stringArrayWeekDays[calendar.get(Calendar.DAY_OF_WEEK)] + " ";
        // Day of the month
        if(calendar.get(Calendar.DAY_OF_MONTH)<10){
            dateString += "0";
        }
        dateString += calendar.get(Calendar.DAY_OF_MONTH);
        // Month as 3 letters
        dateString += ". " + stringArrayMonths[calendar.get(Calendar.MONTH)] + ", ";
        // Time as HH/MM
        // Hours
        if(calendar.get(Calendar.HOUR)<10){
            dateString += "0";
        }
        dateString += calendar.get(Calendar.HOUR) + ":";
        // Minutes
        if(calendar.get(Calendar.MINUTE)<10){
            dateString += "0";
        }
        dateString += calendar.get(Calendar.MINUTE);
        if(calendar.get(Calendar.YEAR) != calToday.get(Calendar.YEAR)){
            dateString += " - " + calendar.get(Calendar.YEAR);
        }
        return dateString;
    }

    public String convertAlarmTimeToString(Boolean isSet, long alarmTime, long startTime){
        Log.d(TAG, "convertAlarmTimeToString: isSet: " + isSet);
        Log.d(TAG, "convertAlarmTimeToString: alarmTime: " + alarmTime);
        Log.d(TAG, "convertAlarmTimeToString: startTime: " + startTime);
        return "Klokken yaads";
    }
}
