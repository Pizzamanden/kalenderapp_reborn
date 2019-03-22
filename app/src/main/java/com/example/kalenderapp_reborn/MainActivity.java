package com.example.kalenderapp_reborn;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;

import com.example.kalenderapp_reborn.adapters.CalendarRecyclerAdapter;
import com.example.kalenderapp_reborn.supportclasses.DrawerNavigationClass;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    // Tag D
    private static final String TAG = "MainActivity";

    // Views
    private ActionBarDrawerToggle mToggleButton;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ConstraintLayout contentRoot;
    private RelativeLayout loadingPanel;

    private String timezoneDiffMilli;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview_calendar);
        toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawerLayout_nav);
        mNavView = findViewById(R.id.drawer_nav_view);
        contentRoot = findViewById(R.id.content_root);
        loadingPanel = findViewById(R.id.loadingPanel);

        timezoneDiffMilli = TimeZone.getDefault().getID();

        setupDrawerNav();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Log.d(TAG, "onResume: Resumed");
        mDrawerLayout.closeDrawer(Gravity.START);
    }

    public void setupDrawerNav()
    {
        setSupportActionBar(toolbar);
        mToggleButton = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawerToggleOpen, R.string.drawerToggleClose);
        mDrawerLayout.addDrawerListener(mToggleButton);
        mToggleButton.syncState();
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DrawerNavigationClass addnavigation = new DrawerNavigationClass(MainActivity.this, this);
        addnavigation.setupDrawerClickable(mNavView);



        isLoading();
        // get data, init recyclerview, set status to ready
        getEventJSON();
    }

    private void getEventJSON(){
        String postedRequest = "getallevents";
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


        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuidler to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .buildPost("retrieveJSON", requestJSON);
        // Make call on client with request
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "getEventJSON: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            initRecyclerView(myResponse);
                        }
                    });
                }
            }
        });
    }

    private void initRecyclerView(String jsonString)
    {
        // A datetime of the current moment, timezone is within
        DateTime dateTimeToday = new DateTime();
        // Dummy long, used in start-date and is the datetime of 2000-01-01T00:00:00
        long calStartYear = dateTimeToday.minusYears(1).minusMinutes(dateTimeToday.getMinuteOfDay()).minusSeconds(dateTimeToday.getSecondOfMinute()).getMillis();
        long calEndYear = dateTimeToday.plusYears(5).minusMinutes(dateTimeToday.getMinuteOfDay()).minusSeconds(dateTimeToday.getSecondOfMinute()).getMillis();
        // This datetime is where the calendar is supposed to start
        // Math done on this is always position as added days, or compared vs dateTimeToday
        DateTime dateTimeCalStart = new DateTime(calStartYear);
        DateTime dateTimeCalEnd = new DateTime(calEndYear);
        Log.d(TAG, "initRecyclerView: " + dateTimeCalStart);
        Log.d(TAG, "initRecyclerView: " + dateTimeCalEnd);
        // Days between start and today as days, can be used as a position within layout manager
        int daysBetween = Days.daysBetween(dateTimeCalStart, dateTimeToday).getDays();


        ArrayList<String> eventNames = new ArrayList<>();
        ArrayList<Integer> eventIndex = new ArrayList<>();
        ArrayList<String> eventTime = new ArrayList<>();
        ArrayList<Integer> eventType = new ArrayList<>();
        ArrayList<Integer> eventID = new ArrayList<>();

        // Setup arraylists for recyclerview
        try {
            JSONArray mJSONArray = new JSONArray(jsonString);
            for(int i = 0;i<mJSONArray.length(); i++){
                // Add json fields to arrays for recyclerview
                JSONObject json = mJSONArray.getJSONObject(i);

                // For loop in for loop, because theres 2 types in a single row
                for(int typeInsert = 0; typeInsert < 2; typeInsert++){
                    String toGet;
                    String nameEnd;
                    if(typeInsert < 1){
                        toGet = "event_start";
                        nameEnd = " Starts";
                    } else {
                        toGet = "event_end";
                        nameEnd = " Ends";
                    }
                    eventNames.add(json.getString("event_name") + nameEnd);
                    DateTime jsonDate = new DateTime(json.getString(toGet));
                    Log.d(TAG, "initRecyclerView: " + json.getString("event_name"));
                    Log.d(TAG, "initRecyclerView: " + jsonDate);
                    Log.d(TAG, "initRecyclerView: " + Days.daysBetween(dateTimeCalStart, jsonDate).getDays());
                    Log.d(TAG, "initRecyclerView: ");
                    // Adding a relative number to what should share index with recyclerview index
                    eventIndex.add(Days.daysBetween(dateTimeCalStart, jsonDate).getDays());
                    // Adding string to show what time of day it happens
                    // this is either the start or end of an event, chosen by what typeInsert is
                    eventTime.add(timeToTwoNum(jsonDate.getHourOfDay()) + "." + timeToTwoNum(jsonDate.getMinuteOfHour()));
                    // Adding typeInsert, a saved instance of whether it is the start or end
                    eventType.add(typeInsert);
                    // Adding ID (directly taken from SQL) to be able to reference this entry in the future
                    eventID.add(json.getInt("post_id"));
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }



        Log.d(TAG, "initRecyclerView: " + timezoneDiffMilli);


        CalendarRecyclerAdapter adapter = new CalendarRecyclerAdapter(this, dateTimeCalStart, dateTimeCalEnd, dateTimeToday, eventNames, eventIndex, eventTime, eventType, eventID);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);



        // Scroll to current day
        mLayoutManager.scrollToPosition(daysBetween);
        Log.d(TAG, "initRecyclerView: Scrolled");

        // Declare ready to show content
        isReady();
    }

    public String timeToTwoNum(int timeToFormat){
        if(timeToFormat < 10){
            return "0" + timeToFormat;
        } else {
            return "" + timeToFormat;
        }
    }






    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(mToggleButton.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void isLoading()
    {
        // Hides content, and shows placeholders/loading icons
        loadingPanel.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);
        contentRoot.setVisibility(View.GONE);
        Log.d(TAG, "isLoading: Loadstatus");
    }

    public void isReady()
    {
        // Shows content, and hides placeholders/loading icons
        loadingPanel.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);
        contentRoot.setVisibility(View.VISIBLE);
        Log.d(TAG, "isReady: Loadstatus");
    }

}
