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

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;
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
                eventStartString.add(datetimeToString(json.getInt("event_start")));
                eventEndString.add(datetimeToString(json.getInt("event_end")));
                eventAlarmStatus.add(json.getBoolean("event_alarmenable"));
                if(json.getBoolean("event_alarmenable")){
                    eventAlarmTimeString.add(datetimeToAlarmString(json.getInt("event_start"), json.getInt("event_alarmtime")));
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





    // Method job: Create a stitched date-string to put into views, using only an epoch-time(int/long)
    // Accepts: Takes an int that should function as an unix epoch time unit, either milliseconds or just seconds
    public String datetimeToString(long epochTime){
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
        dateString += stringArrayWeekDays[calendar.get(Calendar.DAY_OF_WEEK) - 1] + " ";
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





    // Method job: Creates a string to visualize a epoch long in a readable format
    // Requires: Joda-Time
    // Accepts: 2 longs, as an epoch-number, either in seconds or milliseconds.
    // Notes: the second long should be LESS than the first (alarms trigger before events)
    public String datetimeToAlarmString(long epochTimeStart, long epochTimeAlarm){
        Log.d(TAG, "datetimeToAlarmString: " + epochTimeStart);
        Log.d(TAG, "datetimeToAlarmString: " + epochTimeAlarm);
        int length = String.valueOf(epochTimeStart).length();
        DateTime eventStart;
        DateTime eventAlarm;
        if(length < 12){
            eventStart = new DateTime(epochTimeStart * 1000);
            eventAlarm = new DateTime(epochTimeAlarm * 1000);
        } else {
            eventStart = new DateTime(epochTimeStart);
            eventAlarm = new DateTime(epochTimeAlarm);
        }
        LocalDateTime localStart = eventStart.toLocalDateTime();
        LocalDateTime localalarm = eventAlarm.toLocalDateTime();




        int minBefore = Minutes.minutesBetween(localalarm, localStart).getMinutes();
        Log.d(TAG, "datetimeToAlarmString: " + minBefore);
        int hoursTotal = minBefore / 60;
        int hoursRest = minBefore % 60;
        int daysTotal = (minBefore / 60) / 24;
        int daysRest = (minBefore / 60) % 24;
        Log.d(TAG, "datetimeToAlarmString: " + hoursTotal);
        Log.d(TAG, "datetimeToAlarmString: " + hoursRest);
        Log.d(TAG, "datetimeToAlarmString: " + daysTotal);
        Log.d(TAG, "datetimeToAlarmString: " + daysRest);
        Log.d(TAG, "datetimeToAlarmString: ");



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





    public void isReady(){
        RecyclerView recV = findViewById(R.id.recyclerview_viewEvents);
        RelativeLayout relL = findViewById(R.id.loadingPanel);
        Toolbar toolbar = findViewById(R.id.toolbar_1);
        toolbar.setVisibility(View.VISIBLE);
        recV.setVisibility(View.VISIBLE);
        relL.setVisibility(View.GONE);
    }
}
