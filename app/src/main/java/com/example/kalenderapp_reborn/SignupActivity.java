package com.example.kalenderapp_reborn;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

public class SignupActivity extends AppCompatActivity {

    EditText editText_firstname, editText_email, editText_password, editText_passwordRepeat;
    CheckBox checkBox_rememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        editText_firstname = findViewById(R.id.editText_firstname);
        editText_email = findViewById(R.id.editText_email);
        editText_password = findViewById(R.id.editText_password);
        editText_passwordRepeat = findViewById(R.id.editText_passwordRepeat);
        checkBox_rememberMe = findViewById(R.id.checkBox_rememberMe);
    }

    public void gotoLogin(View view){
        finish();
    }

    public void buttonSignup(View view){
        if(validateFields()){

        }

    }

    private boolean validateFields(){
        int nameMinLength = 1;
        int emailMinLength = 1;
        int passwordMinLength = 1;

        if(editText_firstname.getText().length() < nameMinLength){
            // Name is shorter than 1
            return createErrToast(getResources().getString(R.string.addevent_val_nameTooShort));
        }
        if(editText_email.getText().length() < emailMinLength){
            // Email invalid
            // TODO run email validation
            return createErrToast(getResources().getString(R.string.addevent_val_startDateNotSet));
        }
        if(editText_password.getText() != editText_passwordRepeat.getText()) {
            // Passwords dosen't match
            return createErrToast(getResources().getString(R.string.addevent_val_alarmTimeNotSet));
        } else {
            if (editText_password.getText().length() < passwordMinLength) {
                // Password too short
                return createErrToast(getResources().getString(R.string.addevent_val_alarmDateNotSet));
            }
        }
        return true;
    }

    private boolean createErrToast(String toastText){
        Toast.makeText(this, toastText, Toast.LENGTH_SHORT).show();
        return false;
    }
}
