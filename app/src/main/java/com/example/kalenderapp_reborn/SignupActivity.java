package com.example.kalenderapp_reborn;

import android.support.constraint.ConstraintLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kalenderapp_reborn.dataobjects.SignupQuery;
import com.example.kalenderapp_reborn.dataobjects.TokenValidation;
import com.example.kalenderapp_reborn.supportclasses.HttpRequestBuilder;
import com.example.kalenderapp_reborn.supportclasses.SessionManager;
import com.google.gson.Gson;

import org.xml.sax.Parser;

import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity implements HttpRequestBuilder.HttpRequestResponse, SessionManager.SessionManagerHttpResponse {

    private static final String TAG = "SignupActivity";
    private static final String SIGNUP_USER_REQUEST = "signup_user_request";
    // TODO test regex to full capacity
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9#_~!$&'()*+,;=:.\"<>@\\[\\]\\\\]+@[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)*$";


    private TextInputLayout editTextLayout_firstname, editTextLayout_email, editTextLayout_password, editTextLayout_passwordRepeat;
    private CheckBox checkBox_rememberMe;
    private Toolbar toolbar;
    private ConstraintLayout contentRoot, constraintLayout_email, constraintLayout_firstname, constraintLayout_password, constraintLayout_passwordrepeat;
    private LinearLayout linearLayout_errorCont, linearLayoutError_email, linearLayoutError_firstname, linearLayoutError_password, linearLayoutError_passwordrepeat;
    private RelativeLayout loadingPanel;
    private SessionManager sessionManager;
    private boolean contentReady;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Log.d(TAG, "onCreate: Lifecycle");
        // TODO signup dosent work 100%, can fail login, but still returns a token???
        editTextLayout_email = findViewById(R.id.editTextLayout_email);
        editTextLayout_firstname = findViewById(R.id.editTextLayout_firstname);
        editTextLayout_password = findViewById(R.id.editTextLayout_password);
        editTextLayout_password.setPasswordVisibilityToggleEnabled(true);
        editTextLayout_passwordRepeat = findViewById(R.id.editTextLayout_passwordRepeat);
        editTextLayout_passwordRepeat.setPasswordVisibilityToggleEnabled(true);

        setFieldChangeListener(editTextLayout_email, "EMAIL");
        setFieldChangeListener(editTextLayout_firstname, "FIRSTNAME");
        setFieldChangeListener(editTextLayout_password, "PASSWORD");
        setFieldChangeListener(editTextLayout_passwordRepeat, "PASSWORD");


        constraintLayout_email = findViewById(R.id.layout_email);
        constraintLayout_firstname = findViewById(R.id.layout_firstname);
        constraintLayout_password = findViewById(R.id.layout_password);
        constraintLayout_passwordrepeat = findViewById(R.id.layout_passwordRepeat);

        linearLayout_errorCont = findViewById(R.id.linearLayout_errorCont);

        linearLayoutError_email = findViewById(R.id.linearLayoutErrorCont_email);
        linearLayoutError_firstname = findViewById(R.id.linearLayoutErrorCont_firstname);
        linearLayoutError_password = findViewById(R.id.linearLayoutErrorCont_password);
        linearLayoutError_passwordrepeat = findViewById(R.id.linearLayoutErrorCont_passwordRepeat);

        contentRoot = findViewById(R.id.content_root);
        loadingPanel = findViewById(R.id.loadingPanel);

        TextView forgotPassword = findViewById(R.id.textView_forgotPassword);
        forgotPassword.setText(forgotPassword.getText() + " (WIP)");

        // FOR DEVELOPMENT
        // TODO make sure this is removed when testing is done
        editTextLayout_firstname.getEditText().setText("Peter");
        editTextLayout_password.getEditText().setText("password123");
        editTextLayout_passwordRepeat.getEditText().setText("password123");

        checkBox_rememberMe = findViewById(R.id.checkBox_rememberMe);
        sessionManager = new SessionManager(this).setSessionManagerListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Lifecycle");
    }

    public void setFieldChangeListener(final TextInputLayout textInputLayout, final String type){
        textInputLayout.getEditText().setOnFocusChangeListener(new View.OnFocusChangeListener() {
            String fieldType = type;
            EditText editText = textInputLayout.getEditText();
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: " + hasFocus + " : " + fieldType);
                if(!hasFocus){
                    if(fieldType.equals("EMAIL")){
                        Log.d(TAG, "onFocusChange: Email lost focus");
                        //validateEmail(editText.getText().toString());
                        Log.d(TAG, "onFocusChange: " + validateEmail(editText.getText().toString()));
                    } else if (fieldType.equals("FIRSTNAME")) {
                        Log.d(TAG, "onFocusChange: Firstname lost focus");
                        //validateFirstname(editText.getText().toString());
                        Log.d(TAG, "onFocusChange: " + validateFirstname(editText.getText().toString()));
                    } else if (fieldType.equals("PASSWORD")){
                        Log.d(TAG, "onFocusChange: Password lost focus");
                        //validatePassword(editText.getText().toString());
                        Log.d(TAG, "onFocusChange: " + validatePassword(editText.getText().toString()));

                        //validatePasswordMatch(editText.getText().toString(), editTextLayout_passwordRepeat.getEditText().getText().toString());
                        Log.d(TAG, "onFocusChange: " + validatePasswordMatch(editText.getText().toString(), editTextLayout_passwordRepeat.getEditText().getText().toString()));
                    }
                }
            }
        });
    }

    public void gotoLogin(View view){
        finish();
    }

    public void doSignup(View view){
        contentReady = onLoading();
        String email = editTextLayout_email.getEditText().getText().toString();
        String firstname = editTextLayout_firstname.getEditText().getText().toString();
        String password = editTextLayout_password.getEditText().getText().toString();
        String passwordRepeat = editTextLayout_passwordRepeat.getEditText().getText().toString();
        Log.d(TAG, "doSignup: " + email);
        Log.d(TAG, "doSignup: " + firstname);
        Log.d(TAG, "doSignup: " + password);
        Log.d(TAG, "doSignup: " + passwordRepeat);


        // Client-side validate fields
        if(validateFields(email, firstname, password, passwordRepeat)){
            Log.d(TAG, "doSignup: Verified");
            Toast.makeText(this, "Valid Email", Toast.LENGTH_SHORT).show();
            /*SignupQuery signupQuery = new SignupQuery(email, firstname, password);
            String json = new Gson().toJson(signupQuery);

            Log.d(TAG, "doSignup: " + json);

            HttpRequestBuilder requestBuilder =
                    new HttpRequestBuilder(this, this, "http://www.folderol.dk/")
                           .postBuilder("signup_user", json, SIGNUP_USER_REQUEST);
            requestBuilder.makeHttpRequest();*/
        } else {
            Log.d(TAG, "doSignup: No valid :(");
            contentReady = onReady();
        }
    }

    public boolean validateEmail(String email){
        boolean emailValid = true;
        // Parent layout, what layout to mark as "invalid input"
        ConstraintLayout parentLayout = constraintLayout_email;
        // Error container, the layout to put written errors into
        LinearLayout errorLayout = linearLayoutError_email;
        errorLayout.removeAllViews();
        parentLayout.setBackgroundResource(0);
        if(email.length() <= 0){
            // Email not set
            createErrorMessage(R.string.signup_val_error_occurred, parentLayout, errorLayout, true);
            createErrorMessage(R.string.signup_val_email_empty, parentLayout, errorLayout, false);
            emailValid = false;
        }
        if(!Pattern.matches(EMAIL_REGEX, email)){
            // Email not confirmed by regex
            if(emailValid){
                createErrorMessage(R.string.signup_val_error_occurred, parentLayout, errorLayout, true);
            }
            createErrorMessage(R.string.signup_val_email_notvalid, parentLayout, errorLayout, false);
            emailValid = false;
        }
        return emailValid;
    }

    public boolean validateFirstname(String firstname){
        boolean firstnameValid = true;
        // Parent layout, what layout to mark as "invalid input"
        ConstraintLayout parentLayout = constraintLayout_firstname;
        // Error container, the layout to put written errors into
        LinearLayout errorLayout = linearLayoutError_firstname;
        errorLayout.removeAllViews();
        parentLayout.setBackgroundResource(0);
        if(firstname.length() <= 0){
            // First name not set
            createErrorMessage(R.string.signup_val_error_occurred, parentLayout, errorLayout, true);
            createErrorMessage(R.string.signup_val_firstname_empty, parentLayout, errorLayout, false);
            firstnameValid = false;
        }
        return firstnameValid;
    }

    public boolean validatePassword(String password){
        boolean passwordValid = true;
        // Parent layout, what layout to mark as "invalid input"
        ConstraintLayout parentLayout = constraintLayout_password;
        // Error container, the layout to put written errors into
        LinearLayout errorLayout = linearLayoutError_password;
        errorLayout.removeAllViews();
        parentLayout.setBackgroundResource(0);
        if(password.length() <= 0){
            // Password not set
            createErrorMessage(R.string.signup_val_error_occurred, parentLayout, errorLayout, true);
            createErrorMessage(R.string.signup_val_password_empty, parentLayout, errorLayout, false);
            passwordValid = false;
        }
        if(password.length() <= 6){
            // Password too short
            if(passwordValid){
                createErrorMessage(R.string.signup_val_error_occurred, parentLayout, errorLayout, true);
            }
            createErrorMessage(R.string.signup_val_password_tooshort, parentLayout, errorLayout, false);
            passwordValid = false;
        }
        return passwordValid;
    }

    public boolean validatePasswordMatch(String passwordRepeat, String password){
        boolean passwordRepeatValid = true;
        // Parent layout, what layout to mark as "invalid input"
        ConstraintLayout parentLayout = constraintLayout_passwordrepeat;
        // Error container, the layout to put written errors into
        LinearLayout errorLayout = linearLayoutError_passwordrepeat;
        errorLayout.removeAllViews();
        parentLayout.setBackgroundResource(0);
        if(passwordRepeat.length() <= 0){
            // Password repeat not set
            createErrorMessage(R.string.signup_val_error_occurred, parentLayout, errorLayout, true);
            createErrorMessage(R.string.signup_val_passwordrepeat_empty, parentLayout, errorLayout, false);
            passwordRepeatValid = false;
        }
        if(!password.equals(passwordRepeat)){
            // Passwords doesn't match
            if(passwordRepeatValid){
                createErrorMessage(R.string.signup_val_error_occurred, parentLayout, errorLayout, true);
            }
            createErrorMessage(R.string.signup_val_password_nomatch, parentLayout, errorLayout, false);
            passwordRepeatValid = false;
        }
        return passwordRepeatValid;
    }

    private boolean validateFields(String email, String firstname, String password, String passwordRepeat){
        boolean allFieldsValid = true;
        if(!validateEmail(email)){
            Log.d(TAG, "validateFields: Email failed");
            allFieldsValid = false;
        }
        if(!validateFirstname(firstname)){
            Log.d(TAG, "validateFields: Firstname failed");
            allFieldsValid = false;
        }
        if(!validatePassword(password)){
            Log.d(TAG, "validateFields: Password failed");
            allFieldsValid = false;
        }
        if(!validatePasswordMatch(passwordRepeat, password)){
            Log.d(TAG, "validateFields: Password repeat failed");
            allFieldsValid = false;
        }
        return allFieldsValid;
    }

    private void createErrorMessage(int errorMessage, ConstraintLayout parentLayout, LinearLayout errorCont, boolean isHeader){
        LayoutInflater inflater = LayoutInflater.from(this);
        View v = inflater.inflate(R.layout.inflator_errortextview, errorCont, false);
        TextView errorTextView = v.findViewById(R.id.textView_errorMessage);
        if(isHeader){
            errorTextView.setTextSize(18);
            v.findViewById(R.id.prepend_dash).setVisibility(View.GONE);
        } else {
            parentLayout.setBackgroundResource(R.drawable.rounded_corner_red_nofill);
        }
        errorTextView.setText(errorMessage);
        errorCont.addView(v);
    }

    private void onSignupResponse(String jsonResponse){
        Log.d(TAG, "onSignupResponse: Fired");
        SignupQuery signupQuery = new Gson().fromJson(jsonResponse, SignupQuery.class);
        SignupQuery.SignupResponse response = signupQuery.getSignupResponse();
        int responseCode = response.getResponseCode();
        String responseString = response.getResponseString();
        Log.d(TAG, "onSignupResponse: " + responseCode);
        Log.d(TAG, "onSignupResponse: " + responseString);
        if(responseCode == 0){
            TokenValidation tokenValidation = response.getTokenValidation();
            if(sessionManager.writeSuccessPrefs(tokenValidation.getJsonWebToken())){
                sessionManager.runTokenValidation();
            }
        } else {
            // TODO prepare error
            // show error
            contentReady = onReady();
        }
    }

    public boolean onLoading() {
        // Hides content, and shows placeholders/loading icons
        loadingPanel.setVisibility(View.VISIBLE);
        if(toolbar != null){
            toolbar.setVisibility(View.GONE);
        }
        contentRoot.setVisibility(View.GONE);
        Log.d(TAG, "onLoading: Loadstatus");
        return false;
    }

    public boolean onReady() {
        // Shows content, and hides placeholders/loading icons
        loadingPanel.setVisibility(View.GONE);
        if(toolbar != null){
            toolbar.setVisibility(View.VISIBLE);
        }
        contentRoot.setVisibility(View.VISIBLE);
        Log.d(TAG, "onReady: Loadstatus");
        return true;
    }

    @Override
    public void onHttpRequestResponse(int responseCode, String responseJson, String requestName) {
        Log.d(TAG, "onHttpRequestResponse: Fired");
        Log.d(TAG, "onHttpRequestResponse: " + responseCode);
        switch (requestName){
            case SIGNUP_USER_REQUEST:
                Log.d(TAG, "onHttpRequestResponse: " + responseJson);
                // TODO act on user signed in (ALREADY WORKS)
                // TODO decipher what already works ^
                onSignupResponse(responseJson);
                break;
            default:
                // bad request/response
                Log.d(TAG, "onHttpRequestResponse: REQUEST FAILED");
        }
    }

    @Override
    public void onTokenStatusResponse(int responseCode, String responseString) {
        if (responseCode == 0) {
            // Success!
            Log.d(TAG, "onTokenStatusResponse: Token validated, starting activity now");
            Log.d(TAG, "onTokenStatusResponse: " + sessionManager.getToken());
            sessionManager.startMainActivity();
        } else {
            // Some kind of failure
            Log.d(TAG, "onTokenStatusResponse: Error: " + responseString);
            Log.d(TAG, "onTokenStatusResponse: Code: " + responseCode);
            // While the user got a response from their signup, when checking the token again (as i do in login)
            sessionManager.startLoginActivity();
        }
    }
}
