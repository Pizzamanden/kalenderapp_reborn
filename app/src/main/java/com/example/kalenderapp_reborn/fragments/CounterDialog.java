package com.example.kalenderapp_reborn.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.kalenderapp_reborn.R;
import com.example.kalenderapp_reborn.adapters.CalendarRecyclerAdapter;
import com.example.kalenderapp_reborn.adapters.CounterDialogAdapter;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class CounterDialog extends DialogFragment {

    private static final String ARG_DAYSLIST = "INT_LIST_DAYS";
    private static final String ARG_HOUSRLIST = "INT_LIST_HOURS";
    private static final String ARG_MINSLIST = "INT_LIST_MINS";
    private ArrayList<Integer> intList_days;
    private ArrayList<Integer> intList_hours;
    private ArrayList<Integer> intList_mins;
    private int currentDay;
    private int currentHour;
    private int currentMin;
    public RecyclerView myRecView;

    View inflatedLayout;


    public CounterDialog(){
        // Empty for instance and saving and such

    }

    public static CounterDialog newInstance(ArrayList<Integer> intList_days, ArrayList<Integer> intList_hours, ArrayList<Integer> intList_mins) {
        CounterDialog myDialog = new CounterDialog();
        Bundle args = new Bundle();
        args.putIntegerArrayList(ARG_DAYSLIST, intList_days);
        args.putIntegerArrayList(ARG_HOUSRLIST, intList_hours);
        args.putIntegerArrayList(ARG_MINSLIST, intList_mins);
        myDialog.setArguments(args);

        return myDialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            intList_days = getArguments().getIntegerArrayList(ARG_DAYSLIST);
            intList_hours = getArguments().getIntegerArrayList(ARG_HOUSRLIST);
            intList_mins = getArguments().getIntegerArrayList(ARG_MINSLIST);
        }
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        inflatedLayout = inflater.inflate(R.layout.fragment_counter_dialog, null);

        // Recycler views
        RecyclerView recView_days = inflatedLayout.findViewById(R.id.recyclerView_days);
        RecyclerView recView_hours = inflatedLayout.findViewById(R.id.recyclerView_hours);
        RecyclerView recView_mins = inflatedLayout.findViewById(R.id.recyclerView_mins);
        myRecView = recView_days;

        // Layout Managers
        LinearLayoutManager layoutManager_days = setupAdapterAndManager(recView_days, intList_days);
        LinearLayoutManager layoutManager_hours = setupAdapterAndManager(recView_hours, intList_hours);
        LinearLayoutManager layoutManager_mins = setupAdapterAndManager(recView_mins, intList_mins);

        // Set listener for scrolling on recycler views
        setupScrollAndClickListener(R.id.button_day_inc, R.id.button_day_dec, recView_days, layoutManager_days, intList_days);
        //setupScrollAndClickListener(R.id.button_day_inc, R.id.button_day_dec, recView_hours, layoutManager_hours);
        //setupScrollAndClickListener(R.id.button_day_inc, R.id.button_day_dec, recView_mins, layoutManager_mins);
    }

    private LinearLayoutManager setupAdapterAndManager(RecyclerView recyclerView, ArrayList<Integer> dataList){
        CounterDialogAdapter adapter = new CounterDialogAdapter(dataList);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        return layoutManager;
    }

    private void setupScrollAndClickListener(final int buttonIncId, final int buttonDecId, final RecyclerView recView, final LinearLayoutManager layoutManager, final ArrayList<Integer> dataSet){
        // Buttons
        ImageButton buttonInc = inflatedLayout.findViewById(buttonIncId);
        ImageButton buttonDec = inflatedLayout.findViewById(buttonDecId);

        // Recycler View Height
        final int recView_Height = recView.getHeight();

        // Button listeners
        buttonInc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRecyclerViewPosition(-2, recView, layoutManager);
            }
        });
        buttonDec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeRecyclerViewPosition(2, recView, layoutManager);
            }
        });
        recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int SCROLL_DIRECTION;
            
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition();
                int lastVisible = layoutManager.findLastCompletelyVisibleItemPosition();
                Log.d(TAG, "onScrolled: " + recView.getHeight());
                View mView = layoutManager.findViewByPosition(firstVisible);

                if(dy < 0){
                    SCROLL_DIRECTION = 1;
                } else {
                    SCROLL_DIRECTION = 2;
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Do something
                    //Log.d(TAG, "onScrollStateChanged: Fling");
                } else if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    // Do something
                    //Log.d(TAG, "onScrollStateChanged: Touch");
                } else {
                    // Do something
                    //Log.d(TAG, "onScrollStateChanged: Stopped");
                    if(SCROLL_DIRECTION == 1){
                        Log.d(TAG, "onScrollStateChanged: It was Up");
                    } else {
                        Log.d(TAG, "onScrollStateChanged: It was Down");
                    }


                }
            }
        });
        layoutManager.scrollToPosition(Integer.MAX_VALUE / 2);
    }



    // Change position of recycler view by +1 or -1
    private void changeRecyclerViewPosition(int buttonAction, RecyclerView recView, LinearLayoutManager layoutManager){
        int position;
        if(buttonAction > 0){
            position = layoutManager.findFirstCompletelyVisibleItemPosition() + buttonAction;
        } else {
            position = layoutManager.findLastCompletelyVisibleItemPosition() + buttonAction;
        }
        recView.scrollToPosition(position);
    }

    private int getRecyclerViewPosition(ArrayList<Integer> dataSet, int position){
        return dataSet.get(dataSet.size() % position);
    }


    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_counter_dialog, null);


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflatedLayout)
                // Add action buttons
                .setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        Log.d(TAG, "onClick: POS");
                    }
                })
                .setNegativeButton("no", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CounterDialog.this.getDialog().cancel();
                        Log.d(TAG, "onClick: NEG");
                    }
                });
        return builder.create();
    }
}
