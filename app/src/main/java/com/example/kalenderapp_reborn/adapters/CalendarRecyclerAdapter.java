package com.example.kalenderapp_reborn.adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.kalenderapp_reborn.CalendarListActivity;
import com.example.kalenderapp_reborn.EventAddActivity;
import com.example.kalenderapp_reborn.R;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;

public class CalendarRecyclerAdapter extends RecyclerView.Adapter<CalendarRecyclerAdapter.ViewHolder> {

    private static final String TAG = "CalendarRecyclerAdapter";

    private DateTime mDateTimeCalStart, mDateTimeCalEnd, mDateTimeToday;
    private String[] stringArrayWeekDays, stringArrayMonths;
    private Context mContext;

    private ArrayList<String> mEventNames, mEventTime;
    private ArrayList<Integer> mEventType, mEventID, mEventIndex;
    private ArrayList<Integer> baseLayouts = new ArrayList<>();

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

        // This was changed to show the method to coworker
        // ORDER MUST ALWAYS BE:
        // Present
        // Past
        // Future
        baseLayouts.add(R.layout.recyclerview_today);
        baseLayouts.add(R.layout.recyclerview_beforedate);
        baseLayouts.add(R.layout.recyclerview_afterdate);

        stringArrayWeekDays = mContext.getResources().getStringArray(R.array.weekDaysS);
        stringArrayMonths = mContext.getResources().getStringArray(R.array.months);
    }


    @Override
    public int getItemViewType(int position) {
        // This parts job is to define what the position should mean when compared to today.
        // As seen below, it returns a simple int, and takes an int.
        // The logic makes sense because it is compared to the one (1) of the dates that make up the recyclerview
        // The date is of course the start date, and since the base for this logic is to find if date is before, is the same, or is after current date, start date is compared to the present.
        // We then need to find out if the date is indeed the past or the present
        // If none we can then assume it is in the future
        // 0 = Today
        // 1 = Past
        // 2 = Future
        if (mDateTimeCalStart.plusDays(position).getDayOfYear() == mDateTimeToday.getDayOfYear() && mDateTimeCalStart.plusDays(position).getYear() == mDateTimeToday.getYear()){
            //Log.d(TAG, "getItemViewType: (Present) It is this year, and this Day of Year");
            // Present
            return 0;
        } else if(mDateTimeCalStart.plusDays(position).getYear() < mDateTimeToday.getYear() || (mDateTimeCalStart.plusDays(position).getDayOfYear() < mDateTimeToday.getDayOfYear() && mDateTimeCalStart.plusDays(position).getYear() == mDateTimeToday.getYear())) {
            //Log.d(TAG, "getItemViewType: (Past) It is before this year, or it is this year, but before this Day of Year");
            // Days already passed
            return 1;
        } else {
            //Log.d(TAG, "getItemViewType: (Future) It is after this year, or it is this year, but after this Day of Year");
            // Days to pass in the future
            return 2;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        // Return Layout according to the argument {i}, which stems from getItemViewType, which  gets the position from getItemId
        // The layout to inflate is to be found further up, as it contains the possible layouts to inflate
        // Layouts should be ordered as-is, or it will take the wrong layouts for the wrong job
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(baseLayouts.get(i), viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        boolean wasPlan = false;
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

        // This loop the entire length of the arraylist each time the recycler view loads a new position
        for(int indexNr = 0; indexNr < mEventIndex.size(); indexNr++){
            if(mEventIndex.get(indexNr) == i){
                // The date of this day is equal to a day in arraylist

                // Inflate layout, and insert text where needed
                View v;
                if(viewHolder.getItemViewType() == 2){
                    v = inflater.inflate(R.layout.inflater_scheduleboxafter, viewHolder.linearLayout_scheduleCont, false);
                } else {
                    v = inflater.inflate(R.layout.inflater_scheduleboxbefore, viewHolder.linearLayout_scheduleCont, false);
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
            View v = inflater.inflate(R.layout.inflater_noeventsfoundbefore, viewHolder.linearLayout_scheduleCont, false);
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
            DateTime dialogDateTime = mDateTimeCalStart.plusDays(getAdapterPosition());
            String dialogTitle = dialogDateTime.getDayOfMonth() + ". " + stringArrayMonths[dialogDateTime.getMonthOfYear() - 1] + " - " + dialogDateTime.getYear();
            String intentDate;
            if(mContext instanceof CalendarListActivity){
                // This gets day of month and month of year as at least two numbers
                intentDate = ((CalendarListActivity) mContext).timeToTwoNum(dialogDateTime.getDayOfMonth()) + "-" + ((CalendarListActivity) mContext).timeToTwoNum(dialogDateTime.getMonthOfYear()) + "-" + dialogDateTime.getYear();
            } else {
                intentDate = dialogDateTime.getDayOfMonth() + "-" + dialogDateTime.getMonthOfYear() + "-" + dialogDateTime.getYear();
            }
            final String intentDateFinal = intentDate;
            final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setPositiveButton(R.string.dialog_default_yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(TAG, "onClick onClick: Accept");
                    // User Accepts
                    Intent i = new Intent(mContext, EventAddActivity.class);
                    i.putExtra("DATE_FROM_MAINACT", intentDateFinal);
                    mContext.startActivity(i);
                }
            });
            builder.setNegativeButton(R.string.dialog_default_no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Log.d(TAG, "onClick onClick: Cancel");
                    // User Cancels
                }
            });
            builder.setMessage(R.string.calendaradapter_newentry_message)
                    .setTitle(dialogTitle);
            final AlertDialog dialog = builder.create();

            dialog.show();
            Log.d(TAG, "onClick: " + getAdapterPosition());
        }

        public Context getContext() {return itemView.getContext();}
    }
}
