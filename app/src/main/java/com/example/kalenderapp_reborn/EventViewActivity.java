package com.example.kalenderapp_reborn;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.kalenderapp_reborn.adapters.ViewEventRecyclerAdapter;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

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
        JodaTimeAndroid.init(this);
        setTitle(getResources().getString(R.string.title_activity_viewevent));

        // Setup Views
        mContext = this;

        stringArrayMonths = getResources().getStringArray(R.array.monthss);
        stringArrayWeekDays = getResources().getStringArray(R.array.weekDaysS);
        Toolbar toolbar = findViewById(R.id.toolbar_1);

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        getDataJSON();
    }


    public void getDataJSON(){

        String postedRequest = "getUserEventsPastToday";
        int userId = 2;
        String userToken = "f213412ui1g2";

        String requestJSON = "{" +
                "\"request\":\"" +
                postedRequest +
                "\", \"identifiers\":{" +
                    "\"userID\":" +
                    userId +
                "}, \"userToken\":\"" +
                userToken +
                "\"}";

        Log.d(TAG, "getDataJSON: " + requestJSON);

        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuidler to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder("select", requestJSON);
        // Make call on client with request
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


    // Method job: Creates and inits recyclerview
    // Uses: datetimeToString
    // -//- datetimeToAlarmString
    // Accepts: a string, should be an encoded json string as array[object{}, object{}...]
    private void initRecyclerView(String jsonString){
        Log.d(TAG, "initRecyclerView: " + jsonString);

        ArrayList<String> eventNames = new ArrayList<>();
        ArrayList<String> eventStartString = new ArrayList<>();
        ArrayList<String> eventEndString = new ArrayList<>();
        ArrayList<Boolean> eventAlarmStatus = new ArrayList<>();
        ArrayList<String> eventAlarmTimeString = new ArrayList<>();
        ArrayList<Integer> eventIds = new ArrayList<>();

        // Setup arraylists for recyclerview
        try {
            JSONArray mJSONArray = new JSONArray(jsonString);
            for(int i = 0;i<mJSONArray.length(); i++){

                // Add json fields to arrays for recyclerview
                JSONObject json = mJSONArray.getJSONObject(i);
                eventNames.add(json.getString("event_name"));
                eventStartString.add(datetimeToString(json.getString("event_start")));
                eventEndString.add(datetimeToString(json.getString("event_end")));
                eventAlarmStatus.add(json.getBoolean("event_alarmenable"));
                if(json.getBoolean("event_alarmenable")){
                    eventAlarmTimeString.add(datetimeToAlarmString(json.getString("event_start"), json.getString("event_alarmtime")));
                } else {
                    eventAlarmTimeString.add("No");
                }
                eventIds.add(json.getInt("post_id"));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        RecyclerView recyclerView = findViewById(R.id.recyclerview_viewEvents);
        ViewEventRecyclerAdapter adapter = new ViewEventRecyclerAdapter(mContext, eventNames, eventStartString, eventEndString, eventIds, eventAlarmStatus, eventAlarmTimeString);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
        isReady();
    }


    public void makeDeleteHTTP(final int id){
            // UI changes and delays for showing butter smooth animations,
            final Handler handler = new Handler();

            // Hide current views with small delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.recyclerview_viewEvents).setVisibility(View.GONE);
                    findViewById(R.id.toolbar_1).setVisibility(View.GONE);
                }
            }, 400);

            // Show loading animation, medium delay in total for facade of doing stuff with data
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                }
            }, 1800);

            // After large delay, actual information starts to get processed
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    postDeleteRequest(id);
                }
            }, 2600);
    }


    public void postDeleteRequest(int id){
        final Handler handler = new Handler();
        Log.d(TAG, "postDeleteRequest: " + id);
        String postedRequest = "deleteevent";
        String userToken = "f213412ui1g2";
        int userId = 2;

        String requestJSON = "{" +
                "\"userId\":" +
                userId +
                ",\"request\":\"" +
                postedRequest +
                "\",\"arg\":" +
                id +
                ", \"userToken\":\"" +
                userToken +
                "\"}";
        Log.d(TAG, "postDeleteRequest: " + requestJSON);
        OkHttpClient client = new OkHttpClient();
        Log.d(TAG, "postDeleteRequest: making client");
        String url = "http://www.folderol.dk/";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("retrieveJSON", requestJSON)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Log.d(TAG, "postDeleteRequest: making call");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "postDeleteRequest: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    Log.d(TAG, "postDeleteRequest: 200");
                    EventViewActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            //findViewById(R.id.failurePanel).setVisibility(View.VISIBLE);
                            findViewById(R.id.successPanel).setVisibility(View.VISIBLE);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                    /*getDataJSON();
                                    findViewById(R.id.successPanel).setVisibility(View.GONE);
                                    findViewById(R.id.recyclerview_viewEvents).setVisibility(View.GONE);
                                    findViewById(R.id.toolbar_1).setVisibility(View.GONE);
                                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);*/
                                }
                            }, 3000);
                        }
                    });
                } else {
                    EventViewActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            //findViewById(R.id.failurePanel).setVisibility(View.VISIBLE);
                            findViewById(R.id.failurePanel).setVisibility(View.VISIBLE);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                    /*getDataJSON();
                                    findViewById(R.id.failurePanel).setVisibility(View.GONE);
                                    findViewById(R.id.toolbar_1).setVisibility(View.GONE);
                                    findViewById(R.id.recyclerview_viewEvents).setVisibility(View.GONE);
                                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);*/
                                }
                            }, 3000);
                        }
                    });
                }
            }
        });
    }


    // Method job: Create a stitched date-string to put into views, using only an epoch-time(int/long)
    // Accepts: Takes an int that should function as an unix epoch time unit, either milliseconds or just seconds
    public String datetimeToString(String epochTime){
        // TODO fix this, it is no lonnger epoch nums, but a joda-time string
        DateTime mDate = new DateTime(epochTime);

        // Start building string
        String dateString;

        // Week day as 3 letters
        dateString = stringArrayWeekDays[mDate.getDayOfWeek()] + " ";

        // Day of the month
        dateString += timeToTwoNum(mDate.getDayOfMonth());

        // Month as 3 letters
        dateString += ". " + stringArrayMonths[mDate.getMonthOfYear() - 1] + ", ";

        // Time as HH.MM
        // Hours
        dateString += timeToTwoNum(mDate.getHourOfDay()) + ".";
        // Minutes
        dateString += timeToTwoNum(mDate.getMinuteOfHour());

        // If it isnt this current year, also add what year its for
        if(mDate.getYear() != new DateTime().getYear()){
            dateString += " - " + mDate.getYear();
        }
        // Return built string
        return dateString;
    }


    // Method job: Creates a string to visualize a epoch long in a readable format
    // Requires: Joda-Time
    // Accepts: 2 longs, as an epoch-number, either in seconds or milliseconds.
    // Notes: the second long should be LESS than the first (alarms trigger before events)
    public String datetimeToAlarmString(String startTime, String alarmTime){

        DateTime startDT = new DateTime(startTime);
        DateTime alarmDT = new DateTime(startTime);

        int minBefore = Minutes.minutesBetween(alarmDT, startDT).getMinutes();
        int hoursTotal = minBefore / 60;
        int hoursRest = minBefore % 60;
        int daysTotal = (minBefore / 60) / 24;
        int daysRest = (minBefore / 60) % 24;

        String returnString = "";
        if(daysTotal > 0){
            // Plural or not
            if(daysTotal > 1){
                returnString += daysTotal + " Days";
            } else {
                returnString += daysTotal + " Day";
            }
        }
        if(daysRest > 0){
            // Previous resource
            if(daysTotal > 0){
                returnString += ", ";
            }
            // Plural or not
            if(daysRest > 1){
                returnString += daysRest + " Hours";
            } else {
                returnString += daysRest + " Hour";
            }
        }
        if(hoursRest > 0){
            // Previous resource
            if(daysTotal > 0 || daysRest > 0){
                returnString += ", ";
            }
            // Plural or not
            if(hoursRest > 1){
                returnString += hoursRest + " Minutes";
            } else {
                returnString += hoursRest + " Minute";
            }
        }
        returnString += " before";
        return returnString;
    }


    public String timeToTwoNum(int timeToFormat){
        if(timeToFormat < 10){
            return "0" + timeToFormat;
        } else {
            return "" + timeToFormat;
        }
    }


    public void isReady(){
        RecyclerView recV = findViewById(R.id.recyclerview_viewEvents);
        RelativeLayout relL = findViewById(R.id.loadingPanel);
        Toolbar toolbar = findViewById(R.id.toolbar_1);
        toolbar.setVisibility(View.VISIBLE);
        recV.setVisibility(View.VISIBLE);
        relL.setVisibility(View.GONE);
    }
}
