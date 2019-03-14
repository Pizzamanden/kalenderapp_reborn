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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.kalenderapp_reborn.adapters.CalendarRecyclerAdapter;
import com.example.kalenderapp_reborn.adapters.ViewEventRecyclerAdapter;
import com.example.kalenderapp_reborn.supportclasses.DrawerNavigationClass;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import org.joda.time.Minutes;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
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

        OkHttpClient client = new OkHttpClient();
        Log.d(TAG, "getEventJSON: making client");
        String url = "http://www.folderol.dk/";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("retrieveJSON", requestJSON)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(requestBody)
                .build();
        Log.d(TAG, "getEventJSON: making call");
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
        long calEndYear = dateTimeToday.plusYears(5).getMillis();
        long calStartYear = dateTimeToday.minusYears(1).getMillis();
        // This datetime is where the calendar is supposed to start
        // Math done on this is always position as added days, or compared vs dateTimeToday
        DateTime dateTimeCalStart = new DateTime(calStartYear);
        DateTime dateTimeCalEnd = new DateTime(calEndYear);
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

                // For loop in for loop, because
                for(int typeInsert = 0; typeInsert < 2; typeInsert++){
                    eventNames.add(json.getString("event_name"));
                    String toGet;
                    if(typeInsert < 1){
                        toGet = "event_start";
                    } else {
                        toGet = "event_end";
                    }
                    Log.d(TAG, "initRecyclerView: " + json.getString("event_name"));
                    long jsonEpoch = json.getInt(toGet);
                    DateTime jsonDate = new DateTime(jsonEpoch * 1000);
                    eventIndex.add(Days.daysBetween(dateTimeCalStart, jsonDate).getDays() + 1);
                    eventTime.add(String.valueOf(jsonDate.getMinuteOfDay()));
                    eventType.add(typeInsert);
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
