package com.example.kalenderapp_reborn.fragments;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kalenderapp_reborn.R;

import java.util.ArrayList;
import java.util.Calendar;

public class Calendar_RecViewAdap extends RecyclerView.Adapter<Calendar_RecViewAdap.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";


    // Dates
    private Calendar cal_flexible = Calendar.getInstance();
    private Calendar cal_today = Calendar.getInstance();


    private Context mContext;
    private ArrayList<Integer> mDates;
    private ArrayList<Integer> mWeekDays;
    private ArrayList<Integer> mDOY;
    private String[] weekDays;


    public Calendar_RecViewAdap(Context context, ArrayList<Integer> dates, ArrayList<Integer> weekdays, ArrayList<Integer> doy) {
        // Construct for external data
        mContext = context;
        mDates = dates;
        mWeekDays = weekdays;
        mDOY = doy;
        weekDays = mContext.getResources().getStringArray(R.array.weekDaysS);

        cal_flexible.setFirstDayOfWeek(Calendar.MONDAY);
        cal_today.setFirstDayOfWeek(Calendar.MONDAY);
        cal_flexible.setMinimalDaysInFirstWeek(4);
        cal_today.setMinimalDaysInFirstWeek(4);
        cal_today.set(cal_today.get(Calendar.YEAR), cal_today.get(Calendar.MONTH), cal_today.get(Calendar.DAY_OF_MONTH));
        cal_flexible.set(cal_flexible.get(Calendar.YEAR), cal_flexible.get(Calendar.MONTH), cal_flexible.get(Calendar.DAY_OF_MONTH));
    }


    @Override
    public int getItemViewType(int position) {

        // Match True for layout 1, false for 2

        if (position == (cal_today.get(Calendar.DAY_OF_YEAR)) - 1) return 1;
        else return 2;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        // Bind layout to manager
        if (i == 1) {
            // Concluded true, special layout
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_recviewadap_spec1, viewGroup, false);
            return new ViewHolder(view);
        } else {
            // Concluded false, basic layout
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.fragment_recviewadap_basic, viewGroup, false);
            return new ViewHolder(view);
        }

    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        Log.d(TAG, "onBindViewHolder: called");

        // Bind data to layout

        cal_flexible.set(Calendar.DAY_OF_YEAR, i + 1);

        String datetext;
        if(mDates.get(i) < 10){
            datetext = "0" + mDates.get(i);
        } else {
            datetext = mDates.get(i) + "";
        }
        viewHolder.dateView.setText(datetext);
        viewHolder.weekdayView.setText(weekDays[mWeekDays.get(i) - 1]);
        //viewHolder.plansView.setText(mContext.getResources().getString(R.string.dummy_text));


        //viewHolder.weekdayView.setText(cal_flexible.get(Calendar.MONDAY) + "");
        viewHolder.plansView.setText(cal_flexible.get(Calendar.WEEK_OF_YEAR) + "");
    }

    @Override
    public int getItemCount() {

        // Return amount of layouts to bind
        // mArrayList.size();
        return mDates.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        // Declare views
        ConstraintLayout parent_layout;
        TextView dateView;
        TextView weekdayView;
        TextView plansView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Setup views
            parent_layout = itemView.findViewById(R.id.parent_layout);
            dateView = itemView.findViewById(R.id.textView_date);
            weekdayView = itemView.findViewById(R.id.textView_weekday);
            plansView = itemView.findViewById(R.id.textView_plans);
        }
    }
}
