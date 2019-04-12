package com.example.kalenderapp_reborn;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
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
import com.example.kalenderapp_reborn.dataobjects.CalendarEntriesTable;
import com.example.kalenderapp_reborn.dataobjects.SQLQueryJson;
import com.example.kalenderapp_reborn.fragments.CounterDialog;
import com.example.kalenderapp_reborn.supportclasses.DrawerNavigationClass;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.google.gson.Gson;

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
import okhttp3.OkHttpClient;
import okhttp3.Request;
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

    DialogFragment myVeryOwnDialog;

    private String[] monthNames;

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

        myVeryOwnDialog = CounterDialog.newInstance(fillArrays(0, 100), fillArrays(0, 23), fillArrays(0, 59));

        initStringArrays();
        setupDrawerNav();
    }



    private void initStringArrays(){
        monthNames = getResources().getStringArray(R.array.months);
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


        // Mark activity as loading, hides all relevant views
        isLoading();
        // Continue progress to load activity
        getEventJSON();
    }

    private void getEventJSON(){

        // TODO code 1 Replace this
        int userID = 2;
        String token = "f213412ui1g2";

        SQLQueryJson sqlQueryJson = new SQLQueryJson(token, "select_all", userID);
        String json = new Gson().toJson(sqlQueryJson);

        Log.d(TAG, "getEventJSON: " + json);


        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuilder to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder("query", json);
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
                            // Use data to init Recycler View
                            initRecyclerView(myResponse);
                        }
                    });
                }
            }
        });
    }

    private void initRecyclerView(String jsonString) {

        // I'll need 2 datetimes, one for start point, another for end
        // Days between these 2 dates is the max rows in my recycler view
        // The start date can also be used with days between to find index num
        // The datetimes should only be dates, but i want to NOT have to convert ever again, so its datetime
        // The reason is i should only measure days between from 00:00, as seen with 22:01 and 22:00 (22:01 is LESS than a day, i.e 0 days from 22:00)
        final DateTime dateTimeToday = new DateTime();
        final DateTime dateTimeCalStart = new DateTime(dateTimeToday.minusYears(1).minusMinutes(dateTimeToday.getMinuteOfDay()).minusSeconds(dateTimeToday.getSecondOfMinute()).getMillis());
        final DateTime dateTimeCalEnd = new DateTime(dateTimeToday.plusYears(5).minusMinutes(dateTimeToday.getMinuteOfDay()).minusSeconds(dateTimeToday.getSecondOfMinute()).getMillis());
        // Check correctness in debug logcat
        Log.d(TAG, "initRecyclerView: " + dateTimeCalStart);
        Log.d(TAG, "initRecyclerView: " + dateTimeCalEnd);
        // Days between start and today as days, can be used as a position within layout manager
        int daysBetween = Days.daysBetween(dateTimeCalStart, dateTimeToday).getDays();


        // Setup arraylists for recyclerview
        ArrayList<String> eventNames = new ArrayList<>();
        ArrayList<Integer> eventIndex = new ArrayList<>();
        ArrayList<String> eventTime = new ArrayList<>();
        ArrayList<Integer> eventType = new ArrayList<>();
        ArrayList<Integer> eventID = new ArrayList<>();

        // Now fill arraylists with content
        SQLQueryJson json = new Gson().fromJson(jsonString, SQLQueryJson.class);
        ArrayList<CalendarEntriesTable> calendarEntryTables = json.getQueryResponseArrayList();
        for(int i = 0; i < calendarEntryTables.size(); i++){
            for(int typeInsert = 0; typeInsert < 2; typeInsert++){

                // Is this a starting date, or an ending date?
                DateTime jsonDate;
                if(typeInsert == 0){
                    eventNames.add(calendarEntryTables.get(i).getEventName() + " starts");
                    jsonDate = new DateTime(calendarEntryTables.get(i).getEventStartTime());
                } else {
                    eventNames.add(calendarEntryTables.get(i).getEventName() + " ends");
                    jsonDate = new DateTime(calendarEntryTables.get(i).getEventEndTime());
                }
                // Adding a relative number to what should share index with recyclerview index
                eventIndex.add(Days.daysBetween(dateTimeCalStart, jsonDate).getDays());
                // Adding string to show what time of day it happens
                // this is either the start or end of an event, chosen by what typeInsert is
                eventTime.add(timeToTwoNum(jsonDate.getHourOfDay()) + "." + timeToTwoNum(jsonDate.getMinuteOfHour()));
                // Adding typeInsert, a saved instance of whether it is the start or end
                eventType.add(typeInsert);
                // Adding ID (directly taken from SQL) to be able to reference this entry in the future
                eventID.add(calendarEntryTables.get(i).getEventID());
            }
        }




        Log.d(TAG, "initRecyclerView: " + timezoneDiffMilli);

        // Now make adapter, and set it on my recycler view
        CalendarRecyclerAdapter adapter = new CalendarRecyclerAdapter(this, dateTimeCalStart, dateTimeCalEnd, dateTimeToday, eventNames, eventIndex, eventTime, eventType, eventID);
        recyclerView.setAdapter(adapter);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        // I want to set month name as text-title, so i put a listener on the recycler view
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // I'll start by getting the current position
                // I'm unsure if it should be just the first visible, or the first COMPLETELY visible
                int firstVisible = mLayoutManager.findFirstVisibleItemPosition();
                int lastVisible = mLayoutManager.findLastVisibleItemPosition();
                // Then i get the appropriate month name as string
                String actionbarTitle = monthNames[dateTimeCalStart.plusDays(firstVisible).getMonthOfYear() - 1];
                if(dateTimeCalStart.plusDays(firstVisible).getMonthOfYear() != dateTimeCalStart.plusDays(lastVisible).getMonthOfYear()){
                    actionbarTitle += " - " + monthNames[dateTimeCalStart.plusDays(lastVisible).getMonthOfYear() - 1];
                }
                // I'll also put a year after my month name, but only if it isn't the current year
                if(dateTimeToday.getYear() != dateTimeCalStart.plusDays(firstVisible).getYear()){
                    actionbarTitle += " - " + dateTimeCalStart.plusDays(firstVisible).getYear();
                }
                // Then i'll set the string as the actionbar title
                if(getSupportActionBar() != null){
                    getSupportActionBar().setTitle(actionbarTitle);
                }
            }
        });

        // Scroll to current day
        mLayoutManager.scrollToPosition(daysBetween);
        Log.v(TAG, "initRecyclerView: Scrolled to today");

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

    public void showNoticeDialog(View view) {
        // Create an instance of the dialog fragment and show it
        myVeryOwnDialog.show(getSupportFragmentManager(), "CounterDialog");
    }

    private ArrayList<Integer> fillArrays(int startNum, int maxNum){
        ArrayList<Integer> myNum = new ArrayList<>();
        for(int i = maxNum; i >= startNum; i--){
            myNum.add(i);
        }
        return myNum;
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
