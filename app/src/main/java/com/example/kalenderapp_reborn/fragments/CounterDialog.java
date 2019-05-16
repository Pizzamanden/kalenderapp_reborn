package com.example.kalenderapp_reborn.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kalenderapp_reborn.R;
import com.example.kalenderapp_reborn.adapters.CounterDialogAdapter;
import com.example.kalenderapp_reborn.dataobjects.CounterDialogNumbers;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class CounterDialog extends DialogFragment {

    public static final String POSITIVE = "dialog_response_positive";
    public static final String NEGATIVE = "dialog_response_negative";

    private static final String ARG_DAYSLIST = "INT_LIST_DAYS";
    private static final String ARG_HOUSRLIST = "INT_LIST_HOURS";
    private static final String ARG_MINSLIST = "INT_LIST_MINS";
    private ArrayList<Integer> intList_days;
    private ArrayList<Integer> intList_hours;
    private ArrayList<Integer> intList_mins;
    private int currentDay;
    private int currentHour;
    private int currentMin;

    private String titleText;
    private String positiveText;
    private String negativeText;

    View inflatedLayout;


    public CounterDialog() {
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
        LinearLayoutManager layoutManager_days = setupAdapterAndManager(recView_days, intList_days, false);
        LinearLayoutManager layoutManager_hours = setupAdapterAndManager(recView_hours, intList_hours, true);
        LinearLayoutManager layoutManager_mins = setupAdapterAndManager(recView_mins, intList_mins, true);

        // Set listener for scrolling on recycler views
        setupScrollAndClickListener(R.id.imageButton_day_inc, R.id.imageButton_day_dec, R.id.textView_daysheader, recView_days, layoutManager_days, intList_days, false, 1);
        setupScrollAndClickListener(R.id.imageButton_hour_inc, R.id.imageButton_hour_dec, R.id.textView_hoursheader, recView_hours, layoutManager_hours, intList_hours, true, 2);
        setupScrollAndClickListener(R.id.imageButton_min_inc, R.id.imageButton_min_dec, R.id.textView_minsheader, recView_mins, layoutManager_mins, intList_mins, true, 3);
    }

    private LinearLayoutManager setupAdapterAndManager(RecyclerView recyclerView, ArrayList<Integer> dataList, boolean listRepeat) {
        CounterDialogAdapter adapter = new CounterDialogAdapter(dataList, listRepeat);
        recyclerView.setAdapter(adapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        return layoutManager;
    }

    // Change position of recycler view by +1 or -1
    private void counterButtonListener(ImageButton button, final int buttonAction, final RecyclerView recView) {
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                recView.smoothScrollBy(0, (getInflatedLayoutHeight(recView) * buttonAction));
            }
        });
    }

    private void updateData(int type, int data){


        // Can also validate numbers are real number here, although it serves no purpose


        switch (type){
            case 1:
                this.currentDay = data;
                break;
            case 2:
                this.currentHour = data;
                break;
            case 3:
                this.currentMin = data;
                break;
            default:
                Log.d(TAG, "updateData: Data update failed???");
                break;
        }
    }

    private void setupScrollAndClickListener(final int buttonIncId, final int buttonDecId, final int headerID, final RecyclerView recView, final LinearLayoutManager layoutManager, final ArrayList<Integer> dataSet, boolean repeatList, final int type) {
        // Buttons
        final ImageButton buttonInc = inflatedLayout.findViewById(buttonIncId);
        final ImageButton buttonDec = inflatedLayout.findViewById(buttonDecId);
        final TextView header = inflatedLayout.findViewById(headerID);

        // Button listeners
        // Increment goes down (its a wheel)
        counterButtonListener(buttonInc, -1, recView);
        // Decrement goes up (still a wheel)
        counterButtonListener(buttonDec, 1, recView);


        recView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            int SCROLL_DIRECTION;
            int TARGET_VIEW;
            int TARGET_VIEW_DATA;
            boolean wasTouchScroll;
            boolean wasSnapScroll;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy < 0) {
                    SCROLL_DIRECTION = 1;
                } else {
                    SCROLL_DIRECTION = 2;
                }
                TARGET_VIEW = findClosestPosition(recView, layoutManager, SCROLL_DIRECTION);
                if(TARGET_VIEW != -1){
                    TARGET_VIEW_DATA = dataSet.get(TARGET_VIEW % dataSet.size());
                    header.setText(String.valueOf(TARGET_VIEW_DATA));
                    updateData(type, TARGET_VIEW_DATA);
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                // Checks when state changes, if its changed to idle (scrolling stopped)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL) {
                    wasTouchScroll = true;
                }
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    int closestPos = findClosestPosition(recView, layoutManager, SCROLL_DIRECTION);
                    Log.d(TAG, "onScrollStateChanged: Scroll is idle.");
                    Log.d(TAG, "onScrollStateChanged: Touch Scroll: " + wasTouchScroll);
                    if (SCROLL_DIRECTION == 1) {
                        Log.d(TAG, "onScrollStateChanged: Scroll Way: Up");
                    } else {
                        Log.d(TAG, "onScrollStateChanged: Scroll Way: Down");
                    }
                    if (closestPos == -1) {
                        Log.d(TAG, "onScrollStateChanged: Snapping failed, pos was -1");
                    } else {
                        Log.d(TAG, "onScrollStateChanged: Firing snapRecyclerViewPosition");
                        int pixelsMoved = snapRecyclerViewPosition(closestPos, recView, layoutManager);
                        if(pixelsMoved != 0){
                            Log.d(TAG, "onScrollStateChanged: Snap was fired, and scrolled " + pixelsMoved + " Pixels");
                        } else {
                            Log.d(TAG, "onScrollStateChanged: Snap was fired, but didn't move anything");
                        }
                    }
                    Log.d(TAG, "onScrollStateChanged: ");
                }
            }
        });
        // Sets starting position to 0
        // Might be an easier way to do this, but this works, and the datasets are only up to 100 long
        if (repeatList) {
            int realStartPosition = Integer.MAX_VALUE / 2;
            // Recycler max length is Integer.MAX_VALUE without the / 2
            for (int i = 0; i < dataSet.size(); i++) {
                if (dataSet.get((realStartPosition - i) % dataSet.size()) == 0) {
                    // if this number is the start of the dataset array (should be 0)
                    // it should break the loop, and have set that position as start position to jump to
                    realStartPosition = (realStartPosition - i);
                    break;
                }
            }
            layoutManager.scrollToPosition(realStartPosition);
            updateData(type, (dataSet.get(realStartPosition % dataSet.size())));
            Log.d(TAG, "setupScrollAndClickListener: setting data for type " + type + " as " + dataSet.get(realStartPosition % dataSet.size()));
        } else {
            layoutManager.scrollToPosition(dataSet.size() - 1);
            updateData(type, (dataSet.get(dataSet.size() - 1)));
            Log.d(TAG, "setupScrollAndClickListener: setting data for type " + type + " as " + dataSet.get(dataSet.size() - 1));
        }
    }


    private int snapRecyclerViewPosition(int positionToSnap, RecyclerView recyclerView, LinearLayoutManager layoutManager) {
        LinearLayout view = (LinearLayout) layoutManager.findViewByPosition(positionToSnap);
        // These are the numbers needed to find the difference between the middle point, and the topline minus 50% of the elements height
        assert view != null;
        int halfRecyclerHeight = recyclerView.getMeasuredHeight() / 2;
        int distanceToScroll = (view.getMeasuredHeight() / 2 + view.getTop()) - halfRecyclerHeight;
        recyclerView.smoothScrollBy(0, distanceToScroll);
        if (distanceToScroll == 0) {
            // did not scroll
            return 0;
        } else {
            // did do a scroll, since distance was not 0
            return distanceToScroll;
        }
    }

    private int findClosestPosition(final RecyclerView recyclerView, final LinearLayoutManager layoutManager, final int scrollDirection) {
        int firstPos = layoutManager.findFirstVisibleItemPosition();
        int lastPos = layoutManager.findLastVisibleItemPosition();

        int halfRecyclerHeight = recyclerView.getMeasuredHeight() / 2;

        int topDistance = findRelativeDistanceToMiddle(layoutManager, firstPos, halfRecyclerHeight);
        int botDistance = findRelativeDistanceToMiddle(layoutManager, lastPos, halfRecyclerHeight);

        if (botDistance > topDistance && topDistance >= 0) {
            // topDistance was lower (and therefore closer to the middle) than botDistance
            // and it wasn't -1 (which is error)
            return firstPos;
        } else if (botDistance < topDistance && botDistance >= 0) {
            // botDistance was lower (and therefore closer to the middle) than topDistance
            // and it wasn't -1 (which is error)
            return lastPos;
        } else if (botDistance < 0 && topDistance < 0) {
            // error, none of the views was found
            return -1;
        } else {
            // This means they are the same, and are not lower than 0 (other words, not error)
            // this means we rely on what direction that the scroll occurred in
            // and then choose the one in the direction of the scroll to tie-break
            if (scrollDirection == 1) {
                // its one, so the scroll was up
                return firstPos;
            } else {
                // its not one (actually always 2 then), so the scroll was down
                return lastPos;
            }
        }
    }

    private int findRelativeDistanceToMiddle(final LinearLayoutManager layoutManager, int position, int parentHeight) {
        int distance;
        if (position > 0) {
            LinearLayout view = (LinearLayout) layoutManager.findViewByPosition(position);
            int distanceToMiddle = (view.getMeasuredHeight() / 2 + view.getTop()) - parentHeight;
            if (distanceToMiddle < 0) {
                distance = distanceToMiddle * -1;
            } else {
                distance = distanceToMiddle;
            }
        } else {
            distance = -1;
        }
        return distance;
    }

    private int getInflatedLayoutHeight(RecyclerView recyclerView) {
        int recHeight = recyclerView.getMeasuredHeight();
        return recHeight / 2;
    }

    public interface CounterDialogListener {
        void counterDialogResponse(DialogFragment dialog, CounterDialogNumbers counterDialogNumbers, String responseType);
    }

    // Use this instance of the interface to deliver action events
    CounterDialogListener listener;

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.titleText = getActivity().getResources().getString(R.string.counterdialog_default_title);
        this.positiveText = getActivity().getResources().getString(R.string.dialog_default_done);
        this.negativeText = getActivity().getResources().getString(R.string.dialog_default_cancel);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the CounterDialogListener so we can send events to the host
            listener = (CounterDialogListener) context;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(getActivity().toString()
                    + " must implement CounterDialogListener");
        }
    }

    public void setTitle(int titleStringID, Activity activity){
        this.titleText = activity.getResources().getString(titleStringID);
    }
    public void setPositive(int positiveStringID, Activity activity){
        this.positiveText = activity.getResources().getString(positiveStringID);
    }
    public void setnegative(int negativeStringID, Activity activity){
        this.negativeText = activity.getResources().getString(negativeStringID);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater


        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        builder.setView(inflatedLayout)
                // Add action buttons
                .setPositiveButton(positiveText, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        // sign in the user ...
                        Log.d(TAG, "onClick: POS");
                        CounterDialogNumbers counterDialogNumbers = new CounterDialogNumbers(currentDay, currentHour, currentMin);
                        listener.counterDialogResponse(CounterDialog.this, counterDialogNumbers, CounterDialog.POSITIVE);
                    }
                })
                .setNegativeButton(negativeText, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        CounterDialog.this.getDialog().cancel();
                        Log.d(TAG, "onClick: NEG");
                        CounterDialogNumbers counterDialogNumbers = new CounterDialogNumbers(-1, -1, -1);
                        listener.counterDialogResponse(CounterDialog.this, counterDialogNumbers, CounterDialog.POSITIVE);
                    }
                });
        builder.setTitle(titleText);
        return builder.create();
    }
}
