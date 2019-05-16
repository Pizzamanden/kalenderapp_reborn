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
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
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
import com.example.kalenderapp_reborn.dataobjects.CounterDialogNumbers;
import com.example.kalenderapp_reborn.dataobjects.SQLQueryJson;
import com.example.kalenderapp_reborn.fragments.CounterDialog;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.Calendar;

public class EventAddActivity extends AppCompatActivity implements SessionManager.SessionManagerHttpResponse, HttpRequestBuilder.HttpRequestResponse, CounterDialog.CounterDialogListener {

    private static final String TAG = "EventAddActivity";
    private static final String HTTP_INSERT_OR_UPDATE = "insert_or_update_query";
    private static final String HTTP_GET_SINGULAR = "select_single_row_query";

    TextInputLayout textInputLayout_name, textInputLayout_start_date, textInputLayout_start_time, textInputLayout_end_date, textInputLayout_end_time, textInputLayout_alarm_datetime;
    EditText textInputEditText_name, textInputEditText_start_date, textInputEditText_start_time, textInputEditText_end_date, textInputEditText_end_time, textInputEditText_alarm_datetime;
    Switch switchAlarmEnable;
    Spinner spinner_type;
    private int spinner_index = 0;
    private boolean switch_state = false;
    private int entryID = -1;
    private boolean isAnUpdate = false;
    private String storedStartDate;
    private String storedStartTime;
    private String storedEndDate;
    private String storedEndTime;

    String alarmDateTime;

    private CounterDialogNumbers counterDialogNumbers;
    private CounterDialog myVeryOwnDialog;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_add);

        textInputLayout_name = findViewById(R.id.textInputLayout_name);
        textInputLayout_start_date = findViewById(R.id.textInputLayout_start_date);
        textInputLayout_start_time = findViewById(R.id.textInputLayout_start_time);
        textInputLayout_end_date = findViewById(R.id.textInputLayout_end_date);
        textInputLayout_end_time = findViewById(R.id.textInputLayout_end_time);
        textInputLayout_alarm_datetime = findViewById(R.id.textInputLayout_alarm_datetime);

        textInputEditText_name = textInputLayout_name.getEditText();
        textInputEditText_start_date = textInputLayout_start_date.getEditText();
        textInputEditText_start_time = textInputLayout_start_time.getEditText();
        textInputEditText_end_date = textInputLayout_end_date.getEditText();
        textInputEditText_end_time = textInputLayout_end_time.getEditText();
        textInputEditText_alarm_datetime = textInputLayout_alarm_datetime.getEditText();

        switchAlarmEnable = findViewById(R.id.switch_alarmenable);
        spinner_type = findViewById(R.id.spinner_eventtype);
        Toolbar toolbar = findViewById(R.id.toolbar_1);

        sessionManager = new SessionManager(this).setSessionManagerListener(this);

        // Set toolbar, and icon for toolbar
        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_close_gray_24dp);

        initCounterDialog();

        // Setup dialogs, edittext, date or time (1 or 2)
        initDialogFields(textInputEditText_start_date, 1, 1);
        initDialogFields(textInputEditText_start_time, 2, 1);
        initDialogFields(textInputEditText_end_date, 1, 2);
        initDialogFields(textInputEditText_end_time, 2, 2);
        initDialogFields(textInputEditText_alarm_datetime, 3, 3);


        // Init fields with more data and actions attached
        initSwitch();
        initSpinner();

        // Get startup extras
        // If startup was from CalendarListActivity, from the '+' buttons
        if(getIntent().getStringExtra("DATE_FROM_MAINACT") != null){
            textInputEditText_start_date.setText(getIntent().getStringExtra("DATE_FROM_MAINACT"));
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

    private void initDialogFields(final EditText v, final int timeType, final int fieldType){
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: got focus");
                if (hasFocus) {
                    if(timeType == 2){
                        setTime(v, fieldType);
                    } else if(timeType == 1) {
                        setDate(v, fieldType);
                    } else if(timeType == 3){
                        myVeryOwnDialog.show(getSupportFragmentManager(), "CounterDialog");
                    }
                }
            }
        });
    }

    private void initCounterDialog(){
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
    }

    private ArrayList<Integer> fillArrays(int startNum, int maxNum){
        ArrayList<Integer> myNum = new ArrayList<>();
        for(int i = maxNum; i >= startNum; i--){
            myNum.add(i);
        }
        return myNum;
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
        textInputEditText_alarm_datetime.setFocusable(switchState);
        textInputEditText_alarm_datetime.setFocusableInTouchMode(switchState);
        textInputEditText_alarm_datetime.setEnabled(switchState);

    }

    private void httpPOSTdata(){
        final Handler handler = new Handler();
        Log.d(TAG, "httpPOSTdata: fired");

        // As extension to above, create users and login section
        // TODO find correct zone as String
        String timeZone = "Europe/Copenhagen";

        String eventName = textInputEditText_name.getText().toString();
        String eventStartDate = textInputEditText_start_date.getText().toString();
        String eventStartTime = textInputEditText_start_time.getText().toString();
        String eventEndDate = textInputEditText_end_date.getText().toString();
        String eventEndTime = textInputEditText_end_time.getText().toString();
        String eventAlarmDateTime = textInputEditText_alarm_datetime.getText().toString();

        Log.d(TAG, "setupViewData: " + storedStartDate);
        Log.d(TAG, "setupViewData: " + storedStartTime);
        Log.d(TAG, "setupViewData: " + storedEndDate);
        Log.d(TAG, "setupViewData: " + storedEndTime);

        DateTime dateTime = new DateTime(storedStartDate + "T" + storedStartTime + ":00");
        Log.d(TAG, "httpPOSTdata: " + dateTime);

        // TODO actually make an input that can be inserted into DB from eventAlarmTime
        String eventAlarmDateTimeForServer = dateTime.minusDays(counterDialogNumbers.getDays()).minusHours(counterDialogNumbers.getHours()).minusMinutes(counterDialogNumbers.getMins()).toString();
        Log.d(TAG, "httpPOSTdata: " + eventAlarmDateTimeForServer);
        // Is a select or update, so it needs an instance of CalendarEntriesTable
        CalendarEntriesTable calendarEntriesTable = new CalendarEntriesTable(
                eventName,
                eventStartDate + " " + eventStartTime,
                eventEndDate + " " + eventEndTime,
                timeZone,
                spinner_index,
                switch_state,
                eventAlarmDateTimeForServer
        );

        SQLQueryJson sqlQueryJson;
        if(isAnUpdate){
            sqlQueryJson = new SQLQueryJson(sessionManager, calendarEntriesTable, "update", entryID);
            Log.d(TAG, "httpPOSTdata: Sent an update query");
        } else {
            sqlQueryJson = new SQLQueryJson(sessionManager, calendarEntriesTable, "insert");
            Log.d(TAG, "httpPOSTdata: Sent an insert query");
        }
        String json = new Gson().toJson(sqlQueryJson);

        Log.d(TAG, "httpPOSTdata: " + json);

        HttpRequestBuilder requestBuilder = new HttpRequestBuilder(this, this,"http://www.folderol.dk/")
                .postBuilder("query", json, HTTP_INSERT_OR_UPDATE);
        requestBuilder.makeHttpRequest();
    }

    private void setDate(final EditText v, final int type) {
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
                        if(type == 1){
                            storedStartDate = timeToTwoNum(year) + "-" + timeToTwoNum((monthOfYear + 1)) + "-" + timeToTwoNum(dayOfMonth);
                        } else {
                            storedEndDate = timeToTwoNum(year) + "-" + timeToTwoNum((monthOfYear + 1)) + "-" + timeToTwoNum(dayOfMonth);
                        }
                        v.setText(savedDate);

                    }
                }, year, month, day);
        datePickerDialog.show();
        v.clearFocus();
    }

    private void setTime(final EditText v, final int type){
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
                        if(type == 1){
                            storedStartTime = timeToTwoNum(hourOfDay) + ":" + timeToTwoNum(minute);
                        } else {
                            storedEndTime = timeToTwoNum(hourOfDay) + ":" + timeToTwoNum(minute);
                        }
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
        if(textInputEditText_name.getText().length() <= 0){
            return createErrToast(getResources().getString(R.string.addevent_val_nameTooShort));
        }
        if(textInputEditText_start_date.getText().length() != 10){
            return createErrToast(getResources().getString(R.string.addevent_val_startDateNotSet));
        }
        if(textInputEditText_start_time.getText().length() != 5){
            return createErrToast(getResources().getString(R.string.addevent_val_startTimeNotSet));
        }
        if(textInputEditText_end_date.getText().length() != 10){
            return createErrToast(getResources().getString(R.string.addevent_val_endDateNotSet));
        }
        if(textInputEditText_end_time.getText().length() != 5){
            return createErrToast(getResources().getString(R.string.addevent_val_endTimeNotSet));
        }
        if(spinner_index == 0){
            return createErrToast(getResources().getString(R.string.addevent_val_spinErrToast));
        }
        if(switch_state) {
            /*if (textInputEditText_alarm_datetime.getText().length() != 10) {
                return createErrToast(getResources().getString(R.string.addevent_val_alarmDateNotSet));
            }*/
        }
        return true;
    }

    private boolean createErrToast(String toastText){
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        return false;
    }

    private void setAddEntryView(int id){
        // Get saved data

        // Make query Json
        SQLQueryJson sqlQueryJson = new SQLQueryJson(sessionManager, "select_single", id);
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
            storedStartDate = timeToTwoNum(startDT.getYear()) + "-" + timeToTwoNum(startDT.getMonthOfYear()) + "-" + timeToTwoNum(startDT.getDayOfMonth());
            storedStartTime = timeToTwoNum(startDT.getHourOfDay()) + ":" + timeToTwoNum(startDT.getMinuteOfHour());
            storedEndDate = timeToTwoNum(endDT.getYear()) + "-" + timeToTwoNum(endDT.getMonthOfYear()) + "-" + timeToTwoNum(endDT.getDayOfMonth());
            storedEndTime = timeToTwoNum(endDT.getHourOfDay()) + ":" + timeToTwoNum(endDT.getMinuteOfHour());

            Log.d(TAG, "setupViewData: " + storedStartDate);
            Log.d(TAG, "setupViewData: " + storedStartTime);
            Log.d(TAG, "setupViewData: " + storedEndDate);
            Log.d(TAG, "setupViewData: " + storedEndTime);

            // Add json fields to edit text views
            // timeToTwoNum takes a number and if its under 10, it prepends a 0
            textInputEditText_name.setText(calendarEntryTables.get(i).getEventName());
            textInputEditText_start_date.setText(timeToTwoNum(startDT.getDayOfMonth()) + "-" + timeToTwoNum(startDT.getMonthOfYear()) + "-" + timeToTwoNum(startDT.getYear()));
            textInputEditText_start_time.setText(timeToTwoNum(startDT.getHourOfDay()) + ":" + timeToTwoNum(startDT.getMinuteOfHour()));
            textInputEditText_end_date.setText(timeToTwoNum(endDT.getDayOfMonth()) + "-" + timeToTwoNum(endDT.getMonthOfYear()) + "-" + timeToTwoNum(endDT.getYear()));
            textInputEditText_end_time.setText(timeToTwoNum(endDT.getHourOfDay()) + ":" + timeToTwoNum(endDT.getMinuteOfHour()));

            int minBefore = Minutes.minutesBetween(alarmDT, startDT).getMinutes();
            counterDialogNumbers = setCounterDialogNumbers(minBefore);

            String toSet = counterDialogNumbers.getDays() + " days, " + counterDialogNumbers.getHours() + " hours, " + counterDialogNumbers.getMins() + " minutes";

            textInputEditText_alarm_datetime.setText(toSet);
            // Set spinner type to reflect type from DB
            spinnerStateSync(calendarEntryTables.get(i).getEventType());
            // Set switch state to reflect alarm status
            switchStateSync(calendarEntryTables.get(i).getEventAlarmStatus());
        }
    }

    private CounterDialogNumbers setCounterDialogNumbers(int minutesBefore){

        int hoursTotal = minutesBefore / 60;
        int hoursRest = minutesBefore % 60;
        int daysTotal = (minutesBefore / 60) / 24;
        int daysRest = (minutesBefore / 60) % 24;

        Log.d(TAG, "setCounterDialogNumbers: " + hoursTotal);
        Log.d(TAG, "setCounterDialogNumbers: " + hoursRest);
        Log.d(TAG, "setCounterDialogNumbers: " + daysTotal);
        Log.d(TAG, "setCounterDialogNumbers: " + daysRest);


        CounterDialogNumbers counterDialogNumbers = new CounterDialogNumbers(daysTotal, daysRest, hoursRest);
        return counterDialogNumbers;
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
        if(responseCode != 1){
            Log.d(TAG, "onTokenStatusResponse: Auth failed");
            sessionManager.invalidateToken();
        } else {
            Log.d(TAG, "onTokenStatusResponse: Auth Successful!");
        }
    }

    @Override
    public void onHttpRequestResponse(int responseCode, String responseJson, String requestName) {
        Log.d(TAG, "onHttpRequestResponse: Response");
        Log.d(TAG, "onHttpRequestResponse: " + responseCode);
        Log.d(TAG, "onHttpRequestResponse: " + requestName);
        Log.d(TAG, "onHttpRequestResponse: " + responseJson);
        // TODO Perform check for return code
        if(responseCode == 200){
            final Handler handler = new Handler();
            if(requestName.equals(HTTP_INSERT_OR_UPDATE)){
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
                setupViewData(responseJson);
            }
        } else {
            // Server error
            // TODO implement error handling for server responses in HttpRequestBuilder
        }
    }

    @Override
    public void counterDialogResponse(DialogFragment dialog, CounterDialogNumbers counterDialogNumbers, String responseType) {
        if(responseType.equals(CounterDialog.POSITIVE)){
            Log.d(TAG, "counterDialogResponse: POSTITIVE");
            this.counterDialogNumbers = counterDialogNumbers;
            String toSet = counterDialogNumbers.getDays() + " days, " + counterDialogNumbers.getHours() + " hours, " + counterDialogNumbers.getMins() + " minutes";
            textInputEditText_alarm_datetime.setText(toSet);
        } else {
            Log.d(TAG, "counterDialogResponse: NEGATIVE");
        }
        textInputEditText_alarm_datetime.clearFocus();
    }
}