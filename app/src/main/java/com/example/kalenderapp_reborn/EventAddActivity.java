package com.example.kalenderapp_reborn;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class EventAddActivity extends AppCompatActivity {

    private static final String TAG = "EventAddActivity";

    EditText editText_name, editText_start_datefield, editText_start_timefield, editText_end_datefield, editText_end_timefield, editText_alarmdate, editText_alarmtime;
    Switch switchAlarmEnable;
    private int spinner_index = 0;
    private boolean switch_state = false;
    private int typeOfPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: i");
        setContentView(R.layout.activity_event_add);



        Log.d(TAG, "onCreate: o");
        
        editText_name = findViewById(R.id.editText_name);
        editText_start_datefield = findViewById(R.id.editText_start_datefield);
        editText_start_timefield = findViewById(R.id.editText_start_timefield);
        editText_end_datefield = findViewById(R.id.editText_end_datefield);
        editText_end_timefield = findViewById(R.id.editText_end_timefield);
        editText_alarmdate = findViewById(R.id.editText_alarmdate);
        editText_alarmtime = findViewById(R.id.editText_alarmtime);
        switchAlarmEnable = findViewById(R.id.switch_alarmenable);
        Toolbar toolbar = findViewById(R.id.toolbar_1);

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

        initSwitch();
        initSpinner();

        if(getIntent().getStringExtra("DATE_FROM_MAINACT") != null){
            editText_start_datefield.setText(getIntent().getStringExtra("DATE_FROM_MAINACT"));
        }
        if(getIntent().getIntExtra("EDIT_CALENDAR_ENTRY", 0) != 0){
            setTitle(getResources().getString(R.string.title_activity_addevent2));
            typeOfPost = 2;
            setAddEntryView(getIntent().getIntExtra("EDIT_CALENDAR_ENTRY", 0));
        } else {
            setTitle(getResources().getString(R.string.title_activity_addevent1));
            Log.d(TAG, "onCreate: switch state set");
            switchStateSync(false);
            typeOfPost = 1;
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
        Spinner spinner_type = findViewById(R.id.spinner_eventtype);
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
                parent.getItemAtPosition(position);
                Log.d(TAG, "onItemSelected: " + position);
                spinner_index = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
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
        String mToken = "1fb52hb2j3hk623kj2v";
        // TODO get a real user ID
        // As extension, create users and login section
        int thisuserId = 2;
        // TODO find correct zone with code
        String timeZone = "Europe/Copenhagen";

        String postMessage;


        String postFormdataJSON = "{" +
                "\"tableName\":" +
                "\"calendar_entries\"," +
                "\"data\":" +
                "{" +
                "\"userId\":" +
                thisuserId +
                ",\"eventName\":\"" +
                editText_name.getText().toString() +
                "\",\"eventStart\":\"" +
                editText_start_datefield.getText().toString() + " " + editText_start_timefield.getText().toString() +
                "\",\"eventEnd\":\"" +
                editText_end_datefield.getText().toString() + " " + editText_end_timefield.getText().toString() +
                "\",\"eventTimeZone\":\"" +
                timeZone +
                "\",\"eventType\":" +
                spinner_index +
                ",\"eventAlarmEnabled\":" +
                switch_state +
                ",\"eventAlarmTime\":\"" +
                editText_alarmdate.getText().toString() + " " + editText_alarmtime.getText().toString() +
                "\",\"tokenId\":\"" +
                mToken + "\"}" +
                "}";

        Log.d(TAG, "httpPOSTdata: " + postFormdataJSON);
        if(typeOfPost == 1){
            postMessage = "insert";
        } else if (typeOfPost == 2){
            postMessage = "update";
        } else {
            postMessage = "insert";
        }

        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuidler to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder(postMessage, postFormdataJSON);
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
                final String myResponse = response.body().string();
                if (response.code() == 200) {
                    Log.d(TAG, "onResponse: " + myResponse);
                    Log.d(TAG, "httpPOSTdata: 200");
                    EventAddActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            //findViewById(R.id.failurePanel).setVisibility(View.VISIBLE);
                            findViewById(R.id.successPanel).setVisibility(View.VISIBLE);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 3000);
                        }
                    });
                } else {
                    EventAddActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            //findViewById(R.id.failurePanel).setVisibility(View.VISIBLE);
                            findViewById(R.id.failurePanel).setVisibility(View.VISIBLE);
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    finish();
                                }
                            }, 3000);
                        }
                    });
                }
            }
        });
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

                        String savedDate;
                        if(dayOfMonth < 10){
                            savedDate = "0" + dayOfMonth + "-";
                        } else {
                            savedDate = dayOfMonth + "-";
                        }
                        if((monthOfYear + 1) < 10){
                            savedDate += "0" + (monthOfYear + 1);
                        } else {
                            savedDate += (monthOfYear + 1);
                        }
                        savedDate += "-" + year;
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
                        String savedDate;
                        if(hourOfDay < 10){
                            savedDate = "0" + hourOfDay + ":";
                        } else {
                            savedDate = hourOfDay + ":";
                        }
                        if(minute < 10){
                            savedDate += "0" + minute;
                        } else {
                            savedDate += minute;
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
        String postedRequest = "getSingleEventToEdit";
        String userToken = "f213412ui1g2";
        int userId = 2;

        String requestJSON = "{" +
                "\"request\":\"" +
                postedRequest +
                "\", \"identifiers\":{" +
                "\"userID\":" +
                userId +
                "\"postID\":" +
                id +
                "}, \"userToken\":\"" +
                userToken +
                "\"}";

        // Make Client
        OkHttpClient client = new OkHttpClient();
        // Use self-made class HttpRequestBuilder to make request
        Request request = new HttpRequestBuilder("http://www.folderol.dk/")
                .postBuilder("select", requestJSON);
        // Make call on client with request
        Log.d(TAG, "setAddEntryView: making call");
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d(TAG, "onFailure: Failure");
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                Log.d(TAG, "setAddEntryView: Response code " + response.code());
                if (response.code() == 200) {
                    final String myResponse = response.body().string();
                    Log.d(TAG, "onResponse: " + myResponse);
                    Log.d(TAG, "setAddEntryView: 200");
                    EventAddActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setupViewData(myResponse);
                        }
                    });
                }
            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void setupViewData(String jsonString){
        try {
            JSONArray mJSONArray = new JSONArray(jsonString);
            for(int i = 0;i<mJSONArray.length(); i++){
                // Get current json object
                JSONObject json = mJSONArray.getJSONObject(i);


                // Setup DateTime objects with relevant dates & time (surprise)
                DateTime startDT = new DateTime(json.getString("event_start"));
                DateTime endDT = new DateTime(json.getString("event_end"));
                DateTime alarmDT = new DateTime(json.getString("event_alarmTime"));



                // Add json fields to edit text views
                editText_name.setText(json.getString("event_name"));
                editText_start_datefield.setText(startDT.getDayOfMonth() + "-" + startDT.getMonthOfYear() + "-" + startDT.getYear());
                editText_start_timefield.setText(startDT.getHourOfDay() + ":" + startDT.getMinuteOfHour());
                editText_end_datefield.setText(endDT.getDayOfMonth() + "-" + endDT.getMonthOfYear() + "-" + endDT.getYear());
                editText_end_timefield.setText(endDT.getHourOfDay() + ":" + endDT.getMinuteOfHour());
                editText_alarmdate.setText(alarmDT.getDayOfMonth() + "-" + alarmDT.getMonthOfYear() + "-" + alarmDT.getYear());
                editText_alarmtime.setText(alarmDT.getHourOfDay() + ":" + alarmDT.getMinuteOfHour());
                // Set switch state to reflect alarm status
                switchStateSync(json.getBoolean("event_alarmenable"));

            }
        } catch (JSONException e) {
            e.printStackTrace();
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


    public ArrayList<Fragment> fragmentArrayList = new ArrayList<>();

    public Fragment myFragmentSelecter(int position){

        return fragmentArrayList.get(position);
    }
}