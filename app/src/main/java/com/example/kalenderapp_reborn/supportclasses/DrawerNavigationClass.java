package com.example.kalenderapp_reborn.supportclasses;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;

import com.example.kalenderapp_reborn.EventAddActivity;
import com.example.kalenderapp_reborn.EventViewActivity;
import com.example.kalenderapp_reborn.R;

public class DrawerNavigationClass {

    // Tag D
    private static final String TAG = "DrawerNavigationClass";

    Context mContext;
    Activity mActivity;

    public DrawerNavigationClass(Context context, Activity activity){
        mContext = context;
        mActivity = activity;
    }

    public void setupDrawerClickable(NavigationView navigationView)
    {
        // Setup dialog for exit/cancel
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setPositiveButton(mContext.getString(R.string.DL_ok), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "onNavigationItemSelected onClick: Agree Exit");

                // User wants to exit
                shutdownApp();
            }
        });
        builder.setNegativeButton(mContext.getString(R.string.DL_cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                Log.d(TAG, "onNavigationItemSelected onClick: Cancel Exit");

                // User cancels exit, just do nothing?
                // TODO
            }
        });
        builder.setMessage(mContext.getString(R.string.DL_exit_MSG))
                .setTitle(mContext.getString(R.string.DL_exit_TTL));
        final AlertDialog dialog = builder.create();



        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();
                Intent i;

                if (id == R.id.nav_event) {
                    Log.d(TAG, "onNavigationItemSelected: event nav_event");

                    i = new Intent(mContext, EventAddActivity.class);
                    mContext.startActivity(i);
                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

                } else if (id == R.id.nav_schedule) {
                    Log.d(TAG, "onNavigationItemSelected: sched nav_event");

                    i = new Intent(mContext, EventViewActivity.class);
                    mContext.startActivity(i);
                    mActivity.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                } else if (id == R.id.nav_settings) {
                    Log.d(TAG, "onNavigationItemSelected: sett nav_event");

                    // TODO make setting activity
                } else if (id == R.id.nav_exit) {
                    Log.d(TAG, "onNavigationItemSelected: exit nav_event");

                    dialog.show();
                }
                return true;
            }
        });
    }

    private void shutdownApp(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            mActivity.finishAffinity();
        } else {
            mActivity.finish();
        }

    }
}
