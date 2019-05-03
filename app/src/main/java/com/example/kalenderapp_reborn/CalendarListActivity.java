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
import com.example.kalenderapp_reborn.dataobjects.CounterDialogNumbers;
import com.example.kalenderapp_reborn.dataobjects.SQLQueryJson;
import com.example.kalenderapp_reborn.fragments.CounterDialog;
import com.example.kalenderapp_reborn.supportclasses.DrawerNavigationClass;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.io.IOException;
import java.util.ArrayList;
import java.util.TimeZone;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CalendarListActivity extends AppCompatActivity implements CounterDialog.CounterDialogListener, SessionManager.SessionManagerHttpResponse, HttpRequestBuilder.HttpRequestResponse {

    // Tag
    private static final String TAG = "CalendarListActivity";

    // Statics
    private static final String RECYCLER_LIST_KEY = "calendar_recycler_list";

    // Views
    private ActionBarDrawerToggle mToggleButton;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ConstraintLayout contentRoot;
    private RelativeLayout loadingPanel;

    private SessionManager sessionManager;

    private String timezoneDiffMilli;

    CounterDialog myVeryOwnDialog;

    private String[] monthNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: Fired");
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview_calendar);
        toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawerLayout_nav);
        mNavView = findViewById(R.id.drawer_nav_view);
        contentRoot = findViewById(R.id.content_root);
        loadingPanel = findViewById(R.id.loadingPanel);

        // Mark activity as loading, hides all relevant views
        onLoading();

        sessionManager = new SessionManager(this).setSessionManagerListener(this);

        setupDrawerNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Fired");
        sessionManager.runTokenValidation();
    }

    private void startInit(){
        Log.d(TAG, "startInit: Fired");
        timezoneDiffMilli = TimeZone.getDefault().getID();

        ArrayList<Integer> days = fillArrays(0, 99);
        ArrayList<Integer> hours = fillArrays(0, 23);
        ArrayList<Integer> mins = fillArrays(0, 59);
        Log.d(TAG, "onCreate: " + days.size());
        Log.d(TAG, "onCreate: " + hours.size());
        Log.d(TAG, "onCreate: " + mins.size());

        myVeryOwnDialog = CounterDialog.newInstance(days, hours, mins);
        myVeryOwnDialog.setTitle(R.string.counterdialog_default_title, this);
        myVeryOwnDialog.setPositive(R.string.dialog_default_done, this);
        myVeryOwnDialog.setnegative(R.string.dialog_default_discard, this);

        initStringArrays();
        getEventJSON();
    }

    private void initStringArrays(){
        monthNames = getResources().getStringArray(R.array.months);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mDrawerLayout.closeDrawer(Gravity.START);
    }

    public void setupDrawerNav() {
        setSupportActionBar(toolbar);
        mToggleButton = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawerToggleOpen, R.string.drawerToggleClose);
        mDrawerLayout.addDrawerListener(mToggleButton);
        mToggleButton.syncState();
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DrawerNavigationClass addnavigation = new DrawerNavigationClass(this, sessionManager);
        addnavigation.setupDrawerClickable(mNavView);
    }

    private void getEventJSON(){
        Log.d(TAG, "getEventJSON: Fired");

        // TODO check if works
        int userID = sessionManager.getUserID();
        String token = sessionManager.getToken();

        SQLQueryJson sqlQueryJson = new SQLQueryJson(token, "select_all", userID);
        String json = new Gson().toJson(sqlQueryJson);

        Log.d(TAG, "getEventJSON: " + json);

        HttpRequestBuilder requestBuilder =
                new HttpRequestBuilder(this, this,"http://www.folderol.dk/")
                        .postBuilder("query", json, RECYCLER_LIST_KEY);
        requestBuilder.makeHttpRequest();
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
        mLayoutManager.scrollToPositionWithOffset(daysBetween, 0);
        Log.v(TAG, "initRecyclerView: Scrolled to today");

        // Declare ready to show content
        onReady();
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


    public void onLoading()
    {
        // Hides content, and shows placeholders/loading icons
        loadingPanel.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);
        contentRoot.setVisibility(View.GONE);
        Log.d(TAG, "onLoading: Loadstatus");
    }

    public void onReady()
    {
        // Shows content, and hides placeholders/loading icons
        loadingPanel.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);
        contentRoot.setVisibility(View.VISIBLE);
        Log.d(TAG, "onReady: Loadstatus");
    }

    @Override
    public void onCounterDialogPos(DialogFragment dialog, CounterDialogNumbers counterDialogNumbers) {
        Log.d(TAG, "onCounterDialogPos: " + counterDialogNumbers.getDays());
        Log.d(TAG, "onCounterDialogPos: " + counterDialogNumbers.getHours());
        Log.d(TAG, "onCounterDialogPos: " + counterDialogNumbers.getMins());
    }

    @Override
    public void onCounterDialogNeg(DialogFragment dialog) {

    }

    @Override
    public void onTokenStatusResponse(final int responseCode, final String responseString) {
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
        Log.d(TAG, "onHttpRequestResponse: Fired");
        if(requestName.equals(RECYCLER_LIST_KEY)){
            initRecyclerView(responseJson);
        }
    }
}
