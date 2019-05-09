package com.example.kalenderapp_reborn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.kalenderapp_reborn.dataobjects.CalendarEntriesTable;
import com.example.kalenderapp_reborn.dataobjects.SQLQueryJson;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class EventAddActivity extends AppCompatActivity implements SessionManager.SessionManagerHttpResponse, HttpRequestBuilder.HttpRequestResponse {

    private static final String TAG = "EventAddActivity";
    private static final String HTTP_INSERT_OR_UPDATE = "insert_or_update_query";
    private static final String HTTP_GET_SINGULAR = "select_single_row_query";

    EditText editText_name, editText_start_datefield, editText_start_timefield, editText_end_datefield, editText_end_timefield, editText_alarmdate, editText_alarmtime;
    Switch switchAlarmEnable;
    Spinner spinner_type;
    private int spinner_index = 0;
    private boolean switch_state = false;
    private int entryID = -1;
    private boolean isAnUpdate = false;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_add);
        
        editText_name = findViewById(R.id.editText_name);
        editText_start_datefield = findViewById(R.id.editText_start_datefield);
        editText_start_timefield = findViewById(R.id.editText_start_timefield);
        editText_end_datefield = findViewById(R.id.editText_end_datefield);
        editText_end_timefield = findViewById(R.id.editText_end_timefield);
        editText_alarmdate = findViewById(R.id.editText_alarmdate);
        editText_alarmtime = findViewById(R.id.editText_alarmtime);
        switchAlarmEnable = findViewById(R.id.switch_alarmenable);
        spinner_type = findViewById(R.id.spinner_eventtype);
        Toolbar toolbar = findViewById(R.id.toolbar_1);

        sessionManager = new SessionManager(this).setSessionManagerListener(this);

        // Set toolbar, and icon for toolbar
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_close_gray_24dp);

        // Setup dialogs, edittext, date or time (1 or 2)
        initDialogFields(editText_start_datefield, 1);
        initDialogFields(editText_start_timefield, 2);
        initDialogFields(editText_end_datefield, 1);
        initDialogFields(editText_end_timefield, 2);
        initDialogFields(editText_alarmdate, 1);
        initDialogFields(editText_alarmtime, 2);


        // Init fields with more data and actions attached
        initSwitch();
        initSpinner();

        // Get startup extras
        // If startup was from CalendarListActivity, from the '+' buttons
        if(getIntent().getStringExtra("DATE_FROM_MAINACT") != null){
            editText_start_datefield.setText(getIntent().getStringExtra("DATE_FROM_MAINACT"));
        }
        // Check if startup was from edit button from EventViewActivity
        if(getIntent().getIntExtra("EDIT_CALENDAR_ENTRY", 0) != 0){
            setTitle(getResources().getString(R.string.title_activity_addevent2));
            int eventID = getIntent().getIntExtra("EDIT_CALENDAR_ENTRY", 0);
            isAnUpdate = true;
            entryID = eventID;
            setAddEntryView(eventID);
        } else {
            setTitle(getResources().getString(R.string.title_activity_addevent1));
            Log.d(TAG, "onCreate: switch state set");
            switchStateSync(false);
        }
    }

    private void initDialogFields(final EditText v, final int type){
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: got focus");
                if (hasFocus) {
                    if(type == 2){
                        setTime(v);
                    } else {
                        setDate(v);
                    }
                }
            }
        });
    }

    private void initSpinner(){
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.addevent_typespinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner_type.setAdapter(adapter);
        spinner_type.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

        spinner_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // To retrieve string from spinner: parent.getItemAtPosition(position);
                spinnerStateSync(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void spinnerStateSync(int spinnerState){
        spinner_index = spinnerState;
        spinner_type.setSelection(spinnerState);

    }

    private void initSwitch(){
        switchAlarmEnable.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switchStateSync(isChecked);
                Log.d(TAG, "onCheckedChanged: listener fired, state: " + isChecked);
            }
        });
        Log.d(TAG, "initSwitch: listener setup complete");
    }

    private void switchStateSync(boolean switchState){
        switch_state = switchState;
        if(switchAlarmEnable.isChecked() != switchState){
            switchAlarmEnable.setChecked(switchState);
        }
        editText_alarmdate.setFocusable(switchState);
        editText_alarmtime.setFocusable(switchState);
        editText_alarmdate.setFocusableInTouchMode(switchState);
        editText_alarmtime.setFocusableInTouchMode(switchState);
        editText_alarmdate.setEnabled(switchState);
        editText_alarmtime.setEnabled(switchState);

    }

    private void httpPOSTdata(){
        final Handler handler = new Handler();
        Log.d(TAG, "httpPOSTdata: fired");

        String token = sessionManager.getToken();
        int userID = sessionManager.getUserID();

        // As extension to above, create users and login section
        // TODO find correct zone as String
        String timeZone = "Europe/Copenhagen";


        String eventName = editText_name.getText().toString();
        String eventStartTime = editText_start_datefield.getText().toString() + " " + editText_start_timefield.getText().toString();
        String eventEndTime = editText_end_datefield.getText().toString() + " " + editText_end_timefield.getText().toString();
        String eventAlarmTime = editText_alarmdate.getText().toString() + " " + editText_alarmtime.getText().toString();

        // Is a select or update, so it needs an instance of CalendarEntriesTable
        CalendarEntriesTable calendarEntriesTable = new CalendarEntriesTable(
                eventName,
                eventStartTime,
                eventEndTime,
                timeZone,
                spinner_index,
                switch_state,
                eventAlarmTime
        );

        SQLQueryJson sqlQueryJson;
        if(isAnUpdate){
            sqlQueryJson = new SQLQueryJson(token, calendarEntriesTable, "update", userID, entryID);
        } else {
            sqlQueryJson = new SQLQueryJson(token, calendarEntriesTable, "insert", userID);
        }
        String json = new Gson().toJson(sqlQueryJson);

        Log.d(TAG, "httpPOSTdata: " + json);

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(this, this,"http://www.folderol.dk/")
                .postBuilder("query", json, HTTP_INSERT_OR_UPDATE);
        requestBuilder.makeHttpRequest();
    }

    private void setDate(final EditText v) {

        final Calendar c = Calendar.getInstance();
        final int year, month, day;
        year = c.get(Calendar.YEAR);
        month = c.get(Calendar.MONTH);
        day = c.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {

                        String savedDate = timeToTwoNum(dayOfMonth) + "-" + timeToTwoNum((monthOfYear + 1)) + "-" + timeToTwoNum(year);
                        /*if(dayOfMonth < 10){
                            savedDate = "0" + dayOfMonth + "-";
                        } else {
                            savedDate = dayOfMonth + "-";
                        }
                        if((monthOfYear + 1) < 10){
                            savedDate += "0" + (monthOfYear + 1);
                        } else {
                            savedDate += (monthOfYear + 1);
                        }
                        savedDate += "-" + year;*/
                        v.setText(savedDate);

                    }
                }, year, month, day);
        datePickerDialog.show();
        v.clearFocus();
    }

    private void setTime(final EditText v){
        final Calendar c = Calendar.getInstance();
        final int hour, minute;
        hour = c.get(Calendar.HOUR);
        minute = c.get(Calendar.MINUTE);

        final TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        String savedDate = timeToTwoNum(hourOfDay) + ":" + timeToTwoNum(minute);
                        v.setText(savedDate);
                    }
                }, hour, minute, true);
        timePickerDialog.show();
        v.clearFocus();
    }

    public void buttonConfirm(View view){
        // Validate users input
        if(inputValidation()) {
            // UI changes and delays for showing butter smooth animations,
            final Handler handler = new Handler();
            hideKeyboard(this);

            // Hide current views with small delay
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.scrollview_contentroot).setVisibility(View.GONE);
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
                    httpPOSTdata();
                }
            }, 2600);
        }
    }

    private boolean inputValidation(){
        if(editText_name.getText().length() <= 0){
            return createErrToast(getResources().getString(R.string.addevent_val_nameTooShort));
        }
        if(editText_start_datefield.getText().length() != 10){
            return createErrToast(getResources().getString(R.string.addevent_val_startDateNotSet));
        }
        if(editText_start_timefield.getText().length() != 5){
            return createErrToast(getResources().getString(R.string.addevent_val_startTimeNotSet));
        }
        if(editText_end_datefield.getText().length() != 10){
            return createErrToast(getResources().getString(R.string.addevent_val_endDateNotSet));
        }
        if(editText_end_timefield.getText().length() != 5){
            return createErrToast(getResources().getString(R.string.addevent_val_endTimeNotSet));
        }
        if(spinner_index == 0){
            return createErrToast(getResources().getString(R.string.addevent_val_spinErrToast));
        }
        if(switch_state) {
            if (editText_alarmdate.getText().length() != 10) {
                return createErrToast(getResources().getString(R.string.addevent_val_alarmDateNotSet));
            }
            if (editText_alarmtime.getText().length() != 5) {
                return createErrToast(getResources().getString(R.string.addevent_val_alarmTimeNotSet));
            }
        }
        return true;
    }

    private boolean createErrToast(String toastText){
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void setAddEntryView(int id){
        // Get saved data
        String token = sessionManager.getToken();
        int userID = sessionManager.getUserID();

        // Make query Json
        SQLQueryJson sqlQueryJson = new SQLQueryJson(token, "select_single", userID, id);
        String json = new Gson().toJson(sqlQueryJson);

        Log.d(TAG, "setAddEntryView: " + json);


        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(this, this, "http://www.folderol.dk/")
                .postBuilder("query", json, HTTP_GET_SINGULAR);
        requestBuilder.makeHttpRequest();
    }

    @SuppressLint("SetTextI18n")
    public void setupViewData(String jsonString){
        Log.d(TAG, "setupViewData: Fired");
        SQLQueryJson json = new Gson().fromJson(jsonString, SQLQueryJson.class);
        ArrayList<CalendarEntriesTable> calendarEntryTables = json.getQueryResponseArrayList();
        for(int i = 0; i < calendarEntryTables.size(); i++){
            DateTime startDT = new DateTime(calendarEntryTables.get(i).getEventStartTime());
            DateTime endDT = new DateTime(calendarEntryTables.get(i).getEventEndTime());
            DateTime alarmDT = new DateTime(calendarEntryTables.get(i).getEventAlarmTime());



            // Add json fields to edit text views
            // timeToTwoNum takes a number and if its under 10, it prepends a 0
            editText_name.setText(calendarEntryTables.get(i).getEventName());
            editText_start_datefield.setText(timeToTwoNum(startDT.getDayOfMonth()) + "-" + timeToTwoNum(startDT.getMonthOfYear()) + "-" + timeToTwoNum(startDT.getYear()));
            editText_start_timefield.setText(timeToTwoNum(startDT.getHourOfDay()) + ":" + timeToTwoNum(startDT.getMinuteOfHour()));
            editText_end_datefield.setText(timeToTwoNum(endDT.getDayOfMonth()) + "-" + timeToTwoNum(endDT.getMonthOfYear()) + "-" + timeToTwoNum(endDT.getYear()));
            editText_end_timefield.setText(timeToTwoNum(endDT.getHourOfDay()) + ":" + timeToTwoNum(endDT.getMinuteOfHour()));
            editText_alarmdate.setText(timeToTwoNum(alarmDT.getDayOfMonth()) + "-" + timeToTwoNum(alarmDT.getMonthOfYear()) + "-" + timeToTwoNum(alarmDT.getYear()));
            editText_alarmtime.setText(timeToTwoNum(alarmDT.getHourOfDay()) + ":" + timeToTwoNum(alarmDT.getMinuteOfHour()));
            // Set spinner type to reflect type from DB
            spinnerStateSync(calendarEntryTables.get(i).getEventType());
            // Set switch state to reflect alarm status
            switchStateSync(calendarEntryTables.get(i).getEventAlarmStatus());
        }
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, then grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, then grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    public String timeToTwoNum(int timeToFormat){
        if(timeToFormat < 10){
            return "0" + timeToFormat;
        } else {
            return "" + timeToFormat;
        }
    }

    @Override
    public void onTokenStatusResponse(int responseCode, String responseString) {
        if(responseCode > 0){
            Log.d(TAG, "onTokenStatusResponse: Auth failed");
            sessionManager.invalidateToken();
        } else {
            Log.d(TAG, "onTokenStatusResponse: Auth Successful!");
        }
    }

    @Override
    public void onHttpRequestResponse(int responseCode, String responseJson, String requestName) {
        Log.d(TAG, "onHttpRequestResponse: Response");
        // TODO Perform check for return code
        if(responseCode == 200){
            final Handler handler = new Handler();
            if(requestName.equals(HTTP_INSERT_OR_UPDATE)){
                Log.d(TAG, "onHttpRequestResponse: " + HTTP_INSERT_OR_UPDATE);
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                //findViewById(R.id.failurePanel).setVisibility(View.VISIBLE);
                findViewById(R.id.successPanel).setVisibility(View.VISIBLE);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent i = new Intent(EventAddActivity.this, CalendarListActivity.class);
                        startActivity(i);
                    }
                }, 3000);
            } else if(requestName.equals(HTTP_GET_SINGULAR)){
                Log.d(TAG, "onHttpRequestResponse: " + HTTP_GET_SINGULAR);
                setupViewData(responseJson);
            }
        } else {
            // Server error
            // TODO implement error handling for server responses in HttpRequestBuilder
        }
    }
}