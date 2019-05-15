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
import com.example.kalenderapp_reborn.dataobjects.CalendarEntriesTable;
import com.example.kalenderapp_reborn.dataobjects.SQLQueryJson;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;
import com.google.gson.Gson;

import net.danlew.android.joda.JodaTimeAndroid;

import org.joda.time.DateTime;
import org.joda.time.Days;
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

public class EventViewActivity extends AppCompatActivity implements SessionManager.SessionManagerHttpResponse, HttpRequestBuilder.HttpRequestResponse {

    private static final String TAG = "EventViewActivity";
    private static final String HTTP_GET_ALL_EVENTS = "select_all_user_events_query";
    private static final String HTTP_FIND_EVENT_TO_DELETE = "delete_single_row_query";


    private Context mContext;
    private String mJSONdata;

    private String[] stringArrayMonths;
    private String[] stringArrayWeekDays;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO revisit this whole activity, with the new dialog i made, and also the implementation of joda time
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

        sessionManager = new SessionManager(this).setSessionManagerListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        sessionManager.runTokenValidation();
    }

    private void startInit(){
        getDataJSON();
    }


    public void getDataJSON(){
        Log.d(TAG, "getDataJSON: Fired");

        SQLQueryJson sqlQueryJson = new SQLQueryJson(sessionManager, "select_all_pre_today");
        String json = new Gson().toJson(sqlQueryJson);

        Log.d(TAG, "getDataJSON: " + json);

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(this, this,"http://www.folderol.dk/")
                .postBuilder("query", json, HTTP_GET_ALL_EVENTS);
        requestBuilder.makeHttpRequest();
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

        // Now fill arraylists with content
        SQLQueryJson json = new Gson().fromJson(jsonString, SQLQueryJson.class);
        ArrayList<CalendarEntriesTable> calendarEntryTables = json.getQueryResponseArrayList();
        for(int i = 0; i < calendarEntryTables.size(); i++){
            // Add json fields to arrays for recyclerview
            eventNames.add(calendarEntryTables.get(i).getEventName());
            eventStartString.add(datetimeToString(calendarEntryTables.get(i).getEventStartTime()));
            eventEndString.add(datetimeToString(calendarEntryTables.get(i).getEventEndTime()));
            eventAlarmStatus.add(calendarEntryTables.get(i).getEventAlarmStatus());
            if(calendarEntryTables.get(i).getEventAlarmStatus()){
                eventAlarmTimeString.add(datetimeToAlarmString(calendarEntryTables.get(i).getEventStartTime(), calendarEntryTables.get(i).getEventAlarmTime()));
            } else {
                eventAlarmTimeString.add("No");
            }
            eventIds.add(calendarEntryTables.get(i).getEventID());
        }

        RecyclerView recyclerView = findViewById(R.id.recyclerview_viewEvents);
        ViewEventRecyclerAdapter adapter = new ViewEventRecyclerAdapter(mContext, eventNames, eventStartString, eventEndString, eventIds, eventAlarmStatus, eventAlarmTimeString);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(mLayoutManager);
        isReady();
    }


    public void initDeleteQuery(final int id){
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

        SQLQueryJson sqlQueryJson = new SQLQueryJson(sessionManager, "delete", id);
        String json = new Gson().toJson(sqlQueryJson);

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(this, this, "http://www.folderol.dk/")
                .postBuilder("query", json, HTTP_FIND_EVENT_TO_DELETE);
        requestBuilder.makeHttpRequest();
    }


    // Method job: Create a stitched date-string to put into views, using only an epoch-time(int/long)
    // Accepts: Takes an int that should function as an unix epoch time unit, either milliseconds or just seconds
    public String datetimeToString(String epochTime){
        // TODO fix this, it is no longer epoch nums, but a joda-time string
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


        // TODO this doesnt work / produces wrong results, fix
        // TODO above, and the other todo WAAAAY above
        // TODO !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
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

    @Override
    public void onTokenStatusResponse(int responseCode, String responseString) {
        if(responseCode > 0){
            Log.d(TAG, "onTokenStatusResponse: Auth failed");
            sessionManager.invalidateToken();
        } else {
            Log.d(TAG, "onTokenStatusResponse: Auth Successful!");
            startInit();
        }
    }

    @Override
    public void onHttpRequestResponse(int responseCode, String responseJson, String requestName) {
        final Handler handler = new Handler();
        if(requestName.equals(HTTP_FIND_EVENT_TO_DELETE)){
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            }, 3000);
        } else if (requestName.equals(HTTP_GET_ALL_EVENTS)){
            initRecyclerView(responseJson);
        }
    }
}
