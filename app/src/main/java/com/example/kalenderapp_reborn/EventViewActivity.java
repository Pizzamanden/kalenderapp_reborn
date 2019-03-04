package com.example.kalenderapp_reborn;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

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
                eventStart.add(datetimeToString(json.getInt("event_start")));
                eventEnd.add(datetimeToString(json.getInt("event_end")));
                eventAlarmStatus.add(json.getBoolean("event_alarmenable"));
                Log.d(TAG, "initRecyclerView: " + json.getBoolean("event_alarmenable"));
                if(json.getBoolean("event_alarmenable")){
                    eventAlarmTime.add(datetimeToAlarmString(json.getInt("event_start"), json.getInt("event_alarmtime")));
                } else {
                    eventAlarmTime.add("No");
                }

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

    // Method job: Creates a string to visualize a epoch long in a readable format
    // Requires: Joda-Time
    // Accepts: 2 longs, as an epoch-number, either in seconds or milliseconds.
    // Notes: the seconds long should be LESS than the first (alarms trigger before events)
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
}
