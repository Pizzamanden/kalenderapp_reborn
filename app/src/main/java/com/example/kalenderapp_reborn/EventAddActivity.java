package com.example.kalenderapp_reborn;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;

import java.io.IOException;
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

    EditText editText_name, editText_start_datefield, editText_start_timefield, editText_end_datefield, editText_end_timefield;


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

        Toolbar toolbar = findViewById(R.id.toolbar_1);

        setSupportActionBar(toolbar);
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationIcon(R.drawable.ic_close_gray_36dp);


        // Setup dialogs, edittext, date or time (1 or 2)
        setDialogFieldsListener(editText_start_datefield, 1);
        setDialogFieldsListener(editText_start_timefield, 2);
        setDialogFieldsListener(editText_end_datefield, 1);
        setDialogFieldsListener(editText_end_timefield, 2);

        initSpinner();
    }

    private void setDialogFieldsListener(final EditText v, final int type){
        v.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    if(type == 2){
                        setTime(v);
                    } else {
                        setDate(v);
                    }
                    v.clearFocus();
                }
            }
        });
    }


    private void initSpinner(){
        Spinner spinner = findViewById(R.id.spinner_eventtype);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.addevent_typespinner, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
    }


    public void buttonConfirm(View view){
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

    private void httpPOSTdata(){
        final Handler handler = new Handler();
        Log.d(TAG, "httpPOSTdata: fired");
        String mToken = "1fb52hb2j3hk623kj2v";
        // TODO fix datefields
        String futureJSON = "{" +
                "\"userId\":2," +
                "\"eventName\":\"" +
                editText_name.getText().toString() +
                "\",\"eventStart\":\"" +
                editText_start_datefield.getText().toString() + " " + editText_start_timefield.getText().toString() +
                "\",\"eventEnd\":\"" +
                editText_end_datefield.getText().toString() + " " + editText_end_timefield.getText().toString() +
                "\",\"eventType\":2," +
                "\"eventAlarmEnabled\":true," +
                "\"eventAlarmTime\":\"02-09-2019 02:02\"," +
                "\"tokenId\":\"" +
                mToken + "\"}";

        OkHttpClient client = new OkHttpClient();
        Log.d(TAG, "httpPOSTdata: making client");
        String url = "http://www.folderol.dk/";

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("postJSON", futureJSON)
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
                }
            }
        });
    }









    public void setDate(final EditText v) {

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
    }

    public void setTime(final EditText v){
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
                            savedDate = " 0" + hourOfDay + ":";
                        } else {
                            savedDate = " " + hourOfDay + ":";
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
}
