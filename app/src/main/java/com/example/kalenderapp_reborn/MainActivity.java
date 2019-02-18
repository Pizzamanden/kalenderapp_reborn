package com.example.kalenderapp_reborn;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.example.kalenderapp_reborn.supportclasses.DrawerNavigationClass;

public class MainActivity extends AppCompatActivity {

    // Tag D
    private static final String TAG = "MainActivity";

    // Views
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mToggleButton;
    private NavigationView mNavView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupDrawerNav();
    }

    public void setupDrawerNav() {
        mNavView = findViewById(R.id.drawer_nav_view);
        mDrawerLayout = findViewById(R.id.drawerLayout_nav);
        mToggleButton = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawerToggleOpen, R.string.drawerToggleClose);
        mDrawerLayout.addDrawerListener(mToggleButton);
        mToggleButton.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        DrawerNavigationClass addnavigation = new DrawerNavigationClass(MainActivity.this, this);
        addnavigation.setupDrawerClickable(mNavView);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(mToggleButton.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }




}
