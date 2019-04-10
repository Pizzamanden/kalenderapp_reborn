package com.example.kalenderapp_reborn.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

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

        // Layout Managers
        LinearLayoutManager layoutManager_days = setupAdapterAndManager(recView_days, intList_days);
        LinearLayoutManager layoutManager_hours = setupAdapterAndManager(recView_hours, intList_hours);
        LinearLayoutManager layoutManager_mins = setupAdapterAndManager(recView_mins, intList_mins);

        // Set listener on recycler views
        setupRecyclerViewListener(recView_days, layoutManager_days, intList_days);
        setupRecyclerViewListener(recView_hours, layoutManager_hours, intList_hours);
        setupRecyclerViewListener(recView_mins, layoutManager_mins, intList_mins);
    }

    private void setupRecyclerViewListener(RecyclerView recView, final LinearLayoutManager layoutManager, final ArrayList<Integer> dataList){
        Log.d(TAG, "setupRecyclerViewListener: " + dataList.size());
        recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                // I'll start by getting the current position
                // I'm unsure if it should be just the first visible, or the first COMPLETELY visible
                int firstVisible = layoutManager.findFirstVisibleItemPosition();
                int lastVisible = layoutManager.findLastVisibleItemPosition();


            }
        });
        layoutManager.scrollToPosition(Integer.MAX_VALUE / 2);
    }

    private LinearLayoutManager setupAdapterAndManager(RecyclerView recyclerView, ArrayList<Integer> dataList){
        CounterDialogAdapter adapter = new CounterDialogAdapter(dataList);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        return layoutManager;
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

    public void setup(){

    }
}
