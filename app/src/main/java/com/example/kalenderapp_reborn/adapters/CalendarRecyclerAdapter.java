package com.example.kalenderapp_reborn.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kalenderapp_reborn.EventViewActivity;
import com.example.kalenderapp_reborn.MainActivity;
import com.example.kalenderapp_reborn.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;
import org.joda.time.Days;

import java.util.ArrayList;

import static java.security.AccessController.getContext;

public class CalendarRecyclerAdapter extends RecyclerView.Adapter<CalendarRecyclerAdapter.ViewHolder> {

    private static final String TAG = "CalendarRecyclerAdapter";

    private DateTime mDateTimeCalStart, mDateTimeCalEnd, mDateTimeToday;
    private String[] stringArrayWeekDays;
    private Context mContext;

    private ArrayList<String> mEventNames, mEventTime;
    private ArrayList<Integer> mEventType, mEventID, mEventIndex;

    public CalendarRecyclerAdapter(Context context, DateTime dateTimeStart, DateTime dateTimeEnd, DateTime dateTimeToday, ArrayList<String> eventNames, ArrayList<Integer> eventIndex, ArrayList<String> eventTime, ArrayList<Integer> eventType, ArrayList<Integer> eventID) {
        // Construct
        mContext = context;
        // The datetime start, used to output and make logic on
        mDateTimeCalStart = dateTimeStart;
        mDateTimeCalEnd = dateTimeEnd;
        mDateTimeToday = dateTimeToday;

        mEventNames = eventNames;
        mEventIndex = eventIndex;
        mEventTime = eventTime;
        mEventType = eventType;
        mEventID = eventID;

        stringArrayWeekDays = mContext.getResources().getStringArray(R.array.weekDaysS);

    }


    @Override
    public int getItemViewType(int position) {
        // 1 = Today
        // 2 = Past
        // 3 = Future
        // TODO have set year to year 2000 (needs testing/confirmation) and below dosen't work
        if (mDateTimeCalStart.plusDays(position).getDayOfYear() == mDateTimeToday.getDayOfYear() && mDateTimeCalStart.plusDays(position).getYear() == mDateTimeToday.getYear()){
            //Log.d(TAG, "getItemViewType: (Today) It is this year, and this Day of Year");
            // Today
            return 1;
        } else if(mDateTimeCalStart.plusDays(position).getYear() < mDateTimeToday.getYear() || (mDateTimeCalStart.plusDays(position).getDayOfYear() < mDateTimeToday.getDayOfYear() && mDateTimeCalStart.plusDays(position).getYear() == mDateTimeToday.getYear())) {
            //Log.d(TAG, "getItemViewType: (Past) It is before this year, or it is this year, but before this Day of Year");
            // Days already passed
            return 2;
        } else {
            //Log.d(TAG, "getItemViewType: (Future) It is after this year, or it is this year, but after this Day of Year");
            // Days to pass in the future
            return 3;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        // Bind layout to manager
        if (i == 1) {
            // is 1, layout for today
            Log.d(TAG, "onCreateViewHolder: Inflate 1");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_today, viewGroup, false);
            return new ViewHolder(view);
        } else if(i == 2) {
            // is 2, layout for days before today
            Log.d(TAG, "onCreateViewHolder: Inflate 2");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_beforedate, viewGroup, false);
            return new ViewHolder(view);
        } else if(i == 3){
            // is 3, layout for days after today
            Log.d(TAG, "onCreateViewHolder: Inflate 3");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_afterdate, viewGroup, false);
            return new ViewHolder(view);
        } else {
            // Basic layout (in this case its the future layout)
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_afterdate, viewGroup, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        boolean wasPlan = false;
        DateTime iDateTime = mDateTimeCalStart.plusDays(i);

        // Bind data to layout
        if(mDateTimeCalStart.plusDays(i).getDayOfMonth() < 10) {
            viewHolder.textView_date.setText(String.valueOf("0" + iDateTime.getDayOfMonth()));
        } else {
            viewHolder.textView_date.setText(String.valueOf(iDateTime.getDayOfMonth()));
        }
        viewHolder.textView_weekDay.setText(stringArrayWeekDays[iDateTime.getDayOfWeek()]);

        int eventsForDate = 0;
        for(int indexNr = 0; indexNr < mEventIndex.size(); indexNr++){
            if(mEventIndex.get(indexNr) == i){
                // The date of this day is equal to a day in arraylist
                // TODO Build planName and planTime programmaticly
                // TODO Clean and setup other layouts with same style

                viewHolder.textView_planName.setText(mEventNames.get(indexNr));
                viewHolder.textView_planTime.setText(mEventTime.get(indexNr));
                //Log.d(TAG, "onBindViewHolder: " + mEventNames.get(indexNr));
                wasPlan = true;
                eventsForDate++;
                break;
            }
        }
        Log.d(TAG, "onBindViewHolder: Dates at this date " + eventsForDate);
        Log.d(TAG, "onBindViewHolder: " + iDateTime.getDayOfMonth());
        if(!wasPlan){
            viewHolder.textView_planName.setText("Nothingg planned");
        } else {
            //Log.d(TAG, "onBindViewHolder: A plan was found");
        }
    }

    @Override
    public int getItemCount() {
        return Days.daysBetween(mDateTimeCalStart, mDateTimeCalEnd).getDays();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Declare views
        ConstraintLayout parent_layout;
        LinearLayout linearLayout_scheduleCont, linearLayout_scheduleBox;
        TextView textView_date, textView_weekDay, textView_planName, textView_planTime;
        ImageView addImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Setup views
            parent_layout = itemView.findViewById(R.id.parent_layout);
            linearLayout_scheduleCont = itemView.findViewById(R.id.linearLayout_scheduleCont);
            linearLayout_scheduleBox = itemView.findViewById(R.id.linearLayout_scheduleBox);
            textView_date = itemView.findViewById(R.id.textView_date);
            textView_weekDay = itemView.findViewById(R.id.textView_weekday);
            textView_planName = itemView.findViewById(R.id.textView_planName);
            textView_planTime = itemView.findViewById(R.id.textView_planTime);
            addImage = itemView.findViewById(R.id.imageView_addbutt);
            addImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setPositiveButton(R.string.dialogAddEntryCalendar_RecViewAdap_agree, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(TAG, "onClick onClick: Accept");
                    // User Accepts
                    if(getContext() instanceof EventViewActivity){
                        // TODO implement intent for adding date to AddEvent
                        //((EventViewActivity) getContext()).makeDeleteHTTP(eventId.get(getAdapterPosition()));
                    }
                }
            });
            builder.setNegativeButton(R.string.dialogAddEntryCalendar_RecViewAdap_decline, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(TAG, "onClick onClick: Cancel");
                    // User Cancels
                }
            });
            builder.setMessage(R.string.dialogAddEntryCalendar_RecViewAdap_message)
                    .setTitle(R.string.dialogAddEntryCalendar_RecViewAdap_title);
            final AlertDialog dialog = builder.create();

            dialog.show();
        }

        public Context getContext() {return itemView.getContext();}
    }
}
