package com.example.kalenderapp_reborn.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.kalenderapp_reborn.R;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;

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
        for ( int i = 0; i < eventIndex.size(); i++){
            Log.d(TAG, "CalendarRecyclerAdapter: " + mEventNames.get(i));
            Log.d(TAG, "CalendarRecyclerAdapter: " + mEventIndex.get(i));
            Log.d(TAG, "CalendarRecyclerAdapter: ");
        }

        stringArrayWeekDays = mContext.getResources().getStringArray(R.array.weekDaysS);

    }


    @Override
    public int getItemViewType(int position) {
        // 1 = Today
        // 2 = Past
        // 3 = Future
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
            //Log.d(TAG, "onCreateViewHolder: Inflate 1");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_today, viewGroup, false);
            return new ViewHolder(view);
        } else if(i == 2) {
            // is 2, layout for days before today
            //Log.d(TAG, "onCreateViewHolder: Inflate 2");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_beforedate, viewGroup, false);
            return new ViewHolder(view);
        } else if(i == 3){
            // is 3, layout for days after today
            //Log.d(TAG, "onCreateViewHolder: Inflate 3");
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_afterdate, viewGroup, false);
            return new ViewHolder(view);
        } else {
            // Basic layout (in this case its the future layout)
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_afterdate, viewGroup, false);
            return new ViewHolder(view);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        boolean wasPlan = false;
        // TODO Clean this section of code
        DateTime iDateTime = mDateTimeCalStart.plusDays(i);
        LayoutInflater inflater = LayoutInflater.from(mContext);
        viewHolder.linearLayout_scheduleCont.removeAllViews();

        // Bind data to layout
        if(mDateTimeCalStart.plusDays(i).getDayOfMonth() < 10) {
            viewHolder.textView_date.setText(String.valueOf("0" + iDateTime.getDayOfMonth()));
        } else {
            viewHolder.textView_date.setText(String.valueOf(iDateTime.getDayOfMonth()));
        }
        viewHolder.textView_weekDay.setText(stringArrayWeekDays[iDateTime.getDayOfWeek()]);

        for(int indexNr = 0; indexNr < mEventIndex.size(); indexNr++){
            if(mEventIndex.get(indexNr) == i){
                // The date of this day is equal to a day in arraylist

                // Inflate layout, and insert text where needed
                View v;
                if(viewHolder.getItemViewType() == 2){
                    v = inflater.inflate(R.layout.inflater_scheduleboxbefore, viewHolder.linearLayout_scheduleCont, false);
                } else {
                    v = inflater.inflate(R.layout.inflater_scheduleboxafter, viewHolder.linearLayout_scheduleCont, false);
                }

                TextView planName = v.findViewById(R.id.textView_planName);
                TextView planTime = v.findViewById(R.id.textView_planTime);
                planName.setText(mEventNames.get(indexNr));
                planTime.setText(mEventTime.get(indexNr));
                viewHolder.linearLayout_scheduleCont.addView(v);

                wasPlan = true;
            }
        }
        if(!wasPlan){
            // Insert "No Found entries" View
            View v;
            v = inflater.inflate(R.layout.inflater_noeventsfoundbefore, viewHolder.linearLayout_scheduleCont, false);
            /*if(viewHolder.getItemViewType() == 2){
                v = inflater.inflate(R.layout.inflater_noeventsfoundbefore, viewHolder.linearLayout_scheduleCont, false);
            } else {
                v = inflater.inflate(R.layout.inflater_noeventsfoundafter, viewHolder.linearLayout_scheduleCont, false);
            }*/
            viewHolder.linearLayout_scheduleCont.addView(v);
        }
    }

    @Override
    public int getItemCount() {
        return Days.daysBetween(mDateTimeCalStart, mDateTimeCalEnd).getDays();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Declare views
        ConstraintLayout parent_layout;
        LinearLayout linearLayout_scheduleCont;
        TextView textView_date, textView_weekDay;
        ImageView addImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Setup views
            parent_layout = itemView.findViewById(R.id.parent_layout);
            linearLayout_scheduleCont = itemView.findViewById(R.id.linearLayout_scheduleCont);
            textView_date = itemView.findViewById(R.id.textView_date);
            textView_weekDay = itemView.findViewById(R.id.textView_weekday);
            addImage = itemView.findViewById(R.id.imageView_addbutt);
            addImage.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            // TODO implement intent for adding date to AddEvent
            /*final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setPositiveButton(R.string.dialogAddEntryCalendar_RecViewAdap_agree, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(TAG, "onClick onClick: Accept");
                    // User Accepts
                    if(getContext() instanceof EventViewActivity){

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

            dialog.show();*/
            Log.d(TAG, "onClick: " + getAdapterPosition());
        }

        public Context getContext() {return itemView.getContext();}
    }
}
