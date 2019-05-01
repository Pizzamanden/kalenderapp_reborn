package com.example.kalenderapp_reborn.supportclasses;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;

import com.example.kalenderapp_reborn.EventAddActivity;
import com.example.kalenderapp_reborn.EventViewActivity;
import com.example.kalenderapp_reborn.LoginActivity;
import com.example.kalenderapp_reborn.R;

public class DrawerNavigationClass {

    // Tag D
    private static final String TAG = "DrawerNavigationClass";

    private Context mContext;
    private SessionManager sessionManager;

    public DrawerNavigationClass(Context context, SessionManager sessionManager){
        mContext = context;
        this.sessionManager = sessionManager;
    }

    public void setupDrawerClickable(NavigationView navigationView)
    {
        // Setup dialog for exit/cancel
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setPositiveButton(mContext.getString(R.string.dialog_default_yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "onNavigationItemSelected onClick: Agree Logout");
                // User wants to exit
                sessionManager.invalidateToken();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.dialog_default_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "onNavigationItemSelected onClick: Cancel Logout");
                // User cancels exit, do nothing
            }
        });
        builder.setMessage(mContext.getString(R.string.drawermenu_logout_message))
                .setTitle(mContext.getString(R.string.drawermenu_logout_title));
        final AlertDialog dialog = builder.create();



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                if (id == R.id.nav_event) {
                    Intent i = new Intent(mContext, EventAddActivity.class);
                    mContext.startActivity(i);
                } else if (id == R.id.nav_schedule) {
                    Intent i = new Intent(mContext, EventViewActivity.class);
                    mContext.startActivity(i);
                } else if (id == R.id.nav_settings) {
                    Log.d(TAG, "onNavigationItemSelected: sett nav_event");
                    Intent i = new Intent(mContext, LoginActivity.class);
                    mContext.startActivity(i);
                    // TODO make setting activity
                } else if (id == R.id.nav_logout) {
                    Log.d(TAG, "onNavigationItemSelected: kill token and logout");
                    dialog.show();
                }
                return true;
            }
        });
    }
}
