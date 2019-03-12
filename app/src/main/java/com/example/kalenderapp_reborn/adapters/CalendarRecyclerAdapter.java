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
import android.widget.TextView;

import com.example.kalenderapp_reborn.EventViewActivity;
import com.example.kalenderapp_reborn.R;

import org.joda.time.DateTime;
import org.joda.time.DateTimeFieldType;

import static java.security.AccessController.getContext;

public class CalendarRecyclerAdapter extends RecyclerView.Adapter<CalendarRecyclerAdapter.ViewHolder> {

    private static final String TAG = "CalendarRecyclerAdapter";

    private DateTime dateTime;
    private DateTime dateTimeToday = new DateTime();

    public CalendarRecyclerAdapter() {
        // Construct

        // The datetime start, used to output and make logic on
        dateTime = new DateTime(946684800);
    }


    @Override
    public int getItemViewType(int position) {
        // 1 = Today
        // 2 = Past
        // 3 = Future
        // TODO have set year to year 2000 (needs testing/confirmation) and below dosen't work
        if (dateTime.plusDays(position).getDayOfYear() == dateTimeToday.getDayOfYear() && dateTime.plusDays(position).getYear() == dateTimeToday.getYear()){
            // Today
            return 1;
        } else if(position < dateTimeToday.getDayOfYear()) {
            // Days already passed
            return 2;
        } else {
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
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_today, viewGroup, false);
            return new ViewHolder(view);
        } else if(i == 2) {
            // is 2, layout for days before today
            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_beforedate, viewGroup, false);
            return new ViewHolder(view);
        } else if(i == 3){
            // is 3, layout for days after today
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
        Log.d(TAG, "onBindViewHolder: called");

        // Bind data to layout
        viewHolder.dateView.setText("" + dateTime.plusDays(i).getDayOfMonth());
        viewHolder.weekdayView.setText("" + dateTime.plusDays(i).getDayOfYear());
        viewHolder.plansView.setText("" + dateTime.plusDays(i).getYear());
    }

    @Override
    public int getItemCount() {
        // mArrayList.size();
        return 56000;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Declare views
        ConstraintLayout parent_layout;
        TextView dateView, weekdayView, plansView;
        ImageView addImage;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            // Setup views
            parent_layout = itemView.findViewById(R.id.parent_layout);
            dateView = itemView.findViewById(R.id.textView_date);
            weekdayView = itemView.findViewById(R.id.textView_weekday);
            plansView = itemView.findViewById(R.id.textView_plans);
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
