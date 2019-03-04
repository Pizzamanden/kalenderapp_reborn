package com.example.kalenderapp_reborn.adapters;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.kalenderapp_reborn.EventAddActivity;
import com.example.kalenderapp_reborn.R;

import org.json.JSONArray;

import java.util.ArrayList;

public class ViewEventRecyclerAdapter extends RecyclerView.Adapter<ViewEventRecyclerAdapter.ViewHolder> {

    final static private String TAG = "ViewEventRecycler";
    Context mContext;
    private JSONArray mJSONArray;
    private ArrayList<String> eventTitle;
    private ArrayList<String> eventStart;
    private ArrayList<String> eventEnd;
    private ArrayList<Boolean> eventAlarmStatus;
    private ArrayList<String> eventAlarmTime;
    private ArrayList<Integer> eventId;

    public ViewEventRecyclerAdapter(Context context, ArrayList<String> titles, ArrayList<String> timestarts, ArrayList<String> timeends, ArrayList<Integer> ids, ArrayList<Boolean> alarmstatus, ArrayList<String> alarmtime){
        mContext = context;
        eventTitle = titles;
        eventStart = timestarts;
        eventEnd = timeends;
        eventId = ids;
        eventAlarmStatus = alarmstatus;
        eventAlarmTime = alarmtime;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_viewevents, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int i) {
        viewHolder.textView_title.setText(eventTitle.get(i));
        viewHolder.textView_timestart.setText(eventStart.get(i));
        viewHolder.textView_timeend.setText(eventEnd.get(i));
        if(eventAlarmStatus.get(i)){
            viewHolder.textView_alarmstatus.setText(eventAlarmTime.get(i));
        } else {
            viewHolder.textView_alarmstatus.setText("No");
        }
        viewHolder.imageView_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Edit " + eventId.get(viewHolder.getAdapterPosition()));

                Intent i = new Intent(mContext, EventAddActivity.class);
                i.putExtra("EDIT_CALENDAR_ENTRY", eventId.get(viewHolder.getAdapterPosition()));
                mContext.startActivity(i);
            }
        });
        viewHolder.imageView_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Delete " + eventId.get(viewHolder.getAdapterPosition()));
            }
        });
    }


    @Override
    public int getItemCount() {
        return eventTitle.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {
        // Declare views
        ConstraintLayout constraintLayout_parent;
        ImageView imageView_edit, imageView_delete;
        TextView textView_title, textView_timestart, textView_timeend, textView_alarmstatus, textView_timealarm;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views
            constraintLayout_parent = itemView.findViewById(R.id.constraintLayout_parent);
            textView_title = itemView.findViewById(R.id.textView_title);
            textView_timestart = itemView.findViewById(R.id.textView_timestart);
            textView_timeend = itemView.findViewById(R.id.textView_timeend);
            textView_alarmstatus = itemView.findViewById(R.id.textView_alarmstatus);
            textView_timealarm = itemView.findViewById(R.id.textView_timealarm);
            imageView_edit = itemView.findViewById(R.id.imageView_edit);
            imageView_delete = itemView.findViewById(R.id.imageView_delete);
        }
    }
}
