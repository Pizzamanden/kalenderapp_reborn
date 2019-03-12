package com.example.kalenderapp_reborn;

import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.example.kalenderapp_reborn.adapters.CalendarRecyclerAdapter;
import com.example.kalenderapp_reborn.adapters.ViewEventRecyclerAdapter;
import com.example.kalenderapp_reborn.supportclasses.DrawerNavigationClass;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    // Tag D
    private static final String TAG = "MainActivity";

    // Views
    private ActionBarDrawerToggle mToggleButton;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavView;
    private RecyclerView recyclerView;
    private Toolbar toolbar;
    private ConstraintLayout contentroot;
    private RelativeLayout loadingpanel;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerview_calendar);
        toolbar = findViewById(R.id.toolbar);
        mDrawerLayout = findViewById(R.id.drawerLayout_nav);
        mNavView = findViewById(R.id.drawer_nav_view);
        contentroot = findViewById(R.id.content_root);
        loadingpanel = findViewById(R.id.loadingPanel);

        setupDrawerNav();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        mDrawerLayout.closeDrawer(Gravity.START);
    }

    public void setupDrawerNav()
    {
        setSupportActionBar(toolbar);
        mToggleButton = new ActionBarDrawerToggle(this, mDrawerLayout, R.string.drawerToggleOpen, R.string.drawerToggleClose);
        mDrawerLayout.addDrawerListener(mToggleButton);
        mToggleButton.syncState();
        assert getSupportActionBar() != null;
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        DrawerNavigationClass addnavigation = new DrawerNavigationClass(MainActivity.this, this);
        addnavigation.setupDrawerClickable(mNavView);



        isLoading();
        // get data, when data is here, init recview
        initRecyclerView();
    }

    private void initRecyclerView()
    {
        CalendarRecyclerAdapter adapter = new CalendarRecyclerAdapter();
        recyclerView.setAdapter(adapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        isReady();
    }







    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(mToggleButton.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void isLoading()
    {
        loadingpanel.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);
        contentroot.setVisibility(View.GONE);
    }

    public void isReady()
    {
        loadingpanel.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);
        contentroot.setVisibility(View.VISIBLE);
    }

}
