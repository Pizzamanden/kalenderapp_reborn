package com.example.kalenderapp_reborn.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kalenderapp_reborn.R;
import com.example.kalenderapp_reborn.interfaces.RecyclerViewClickListener;

import java.util.ArrayList;
import java.util.Calendar;

public class Calendar_RecViewImp extends Fragment implements RecyclerViewClickListener {

    // Tag D
    private static final String TAG = "Calendar_RecViewImp";

    // View vars
    ConstraintLayout constraintLayout_root;
    RecyclerView recyclerView;
    TextView recViewHeader;

    // String Arrays
    public String[] stringArrayWeekDays;
    public String[] stringArrayWeekDayss;
    public String[] stringArrayWeekDaysss;
    public String[] stringArrayMonths;

    // Other vars
    private Context mContext;
    private ArrayList<Integer> mDates = new ArrayList<>();
    private ArrayList<Integer> mWeekDays = new ArrayList<>();
    private ArrayList<Integer> mDOY = new ArrayList<>();
    Calendar cal_flexible = Calendar.getInstance();



    // Called when creating views
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recveiwimp, parent, false);
    }


    // Triggers after onCreateView
    // Bind views to vars
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        constraintLayout_root = view.findViewById(R.id.constraintlayout_root);
        recyclerView = view.findViewById(R.id.recyclerview_calendarlist);
        recViewHeader = view.findViewById(R.id.textView_recviewheader);

        mContext = getActivity();


        cal_flexible.setFirstDayOfWeek(Calendar.MONDAY);
        cal_flexible.setMinimalDaysInFirstWeek(4);
        cal_flexible.set(cal_flexible.get(Calendar.YEAR), cal_flexible.get(Calendar.MONTH), cal_flexible.get(Calendar.DAY_OF_MONTH));


        initStringArrays();
        initRecyclerData();
    }

    public void initStringArrays()
    {
        stringArrayWeekDays = getResources().getStringArray(R.array.weekDays);
        stringArrayWeekDayss = getResources().getStringArray(R.array.weekDaysS);
        stringArrayWeekDaysss = getResources().getStringArray(R.array.weekDaysSS);
        stringArrayMonths = getResources().getStringArray(R.array.months);
    }

    public void initRecyclerData(){

        Calendar fillingCalendar = Calendar.getInstance();
        fillingCalendar.setMinimalDaysInFirstWeek(4);
        fillingCalendar.set(Calendar.DAY_OF_YEAR, 1);

        int antal = 3000;
        for(int i = 1; i <= antal; i++){

            // Date
            mDates.add(fillingCalendar.get(Calendar.DATE));

            // Week days, takes day of week from date and year
            mWeekDays.add(fillingCalendar.get(Calendar.DAY_OF_WEEK));

            mDOY.add(fillingCalendar.get(Calendar.DAY_OF_YEAR));

            //Increase with 1
            fillingCalendar.add(Calendar.DAY_OF_MONTH, +1);
        }

        initRecyclerView();
    }

    @Override
    public void recyclerViewListClicked(View v, int position) {
        Toast.makeText(mContext, "Yads", Toast.LENGTH_SHORT).show();
    }

    public void initRecyclerView(){

        // Init recyclerview, attach manager, attach adapter

        {
            Log.d(TAG, "initRecyclerView: Making View");


            Calendar_RecViewAdap adapter = new Calendar_RecViewAdap(mContext, mDates, mWeekDays, mDOY);
            recyclerView.setAdapter(adapter);
            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(mContext);
            recyclerView.setLayoutManager(mLayoutManager);


            Calendar cal_today = Calendar.getInstance();
            recyclerView.scrollToPosition(cal_today.get(Calendar.DAY_OF_YEAR) - 3);


            // Manages scrolling, finds view relative to screen
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {



                // Int for
                int firstVisiblePosition;
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {

                    // Triggered on scrolling on RecyclerView
                    super.onScrolled(recyclerView, dx, dy);
                    firstVisiblePosition = mLayoutManager.findFirstCompletelyVisibleItemPosition();
                    cal_flexible.set(Calendar.DAY_OF_YEAR, firstVisiblePosition+1);

                    // Title as Month - Year
                    getActivity().setTitle(stringArrayMonths[cal_flexible.get(Calendar.MONTH)] + " - " + cal_flexible.get(Calendar.YEAR));
                    cal_flexible.set(Calendar.DAY_OF_YEAR, firstVisiblePosition+1);

                    String setHeader = getString(R.string.WORD_week) + ": " + cal_flexible.get(Calendar.WEEK_OF_YEAR);
                    recViewHeader.setText(setHeader);
                }
            });
        }
    }
}
