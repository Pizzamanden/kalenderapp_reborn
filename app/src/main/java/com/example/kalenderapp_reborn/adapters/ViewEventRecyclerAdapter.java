package com.example.kalenderapp_reborn.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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

import com.example.kalenderapp_reborn.EventAddActivity;
import com.example.kalenderapp_reborn.EventViewActivity;
import com.example.kalenderapp_reborn.R;

import org.json.JSONArray;

import java.util.ArrayList;

public class ViewEventRecyclerAdapter extends RecyclerView.Adapter<ViewEventRecyclerAdapter.ViewHolder> {

    final static private String TAG = "ViewEventRecycler";
    private Context mContext;
    private JSONArray mJSONArray;
    private ArrayList<String> eventTitle, eventStart, eventEnd, eventAlarmTime;
    private ArrayList<Boolean> eventAlarmStatus;
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
            viewHolder.textView_alarmtext.setText(eventAlarmTime.get(i));
        } else {
            viewHolder.textView_alarmtext.setText("No");
        }
    }


    @Override
    public int getItemCount() {
        return eventTitle.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // Declare views
        ConstraintLayout constraintLayout_parent;
        ImageView imageView_edit, imageView_delete;
        TextView textView_title, textView_timestart, textView_timeend, textView_alarmtext;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // Setup views
            constraintLayout_parent = itemView.findViewById(R.id.constraintLayout_parent);
            textView_title = itemView.findViewById(R.id.textView_title);
            textView_timestart = itemView.findViewById(R.id.textView_timestart);
            textView_timeend = itemView.findViewById(R.id.textView_timeend);
            textView_alarmtext = itemView.findViewById(R.id.textView_alarmtext);
            imageView_edit = itemView.findViewById(R.id.imageView_edit);
            imageView_delete = itemView.findViewById(R.id.imageView_delete);

            imageView_edit.setOnClickListener(this);
            imageView_delete.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.imageView_edit:
                    Log.d(TAG, "onClick: Edit " + eventId.get(getAdapterPosition()));
                    Intent i = new Intent(mContext, EventAddActivity.class);
                    i.putExtra("EDIT_CALENDAR_ENTRY", eventId.get(getAdapterPosition()));
                    mContext.startActivity(i);
                    break;
                case R.id.imageView_delete:
                    Log.d(TAG, "onClick: Delete " + eventId.get(getAdapterPosition()));
                    final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setPositiveButton(R.string.viewevent_delete_positive, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "onClick onClick: Accept");
                            // User Accepts
                            // TODO make delete function in both php and here (needs interface?)
                            if(mContext instanceof EventViewActivity){
                                ((EventViewActivity) mContext).makeDeleteHTTP(eventId.get(getAdapterPosition()));
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.viewevent_delete_negative, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Log.d(TAG, "onClick onClick: Cancel");
                            // User Cancels
                        }
                    });
                    builder.setMessage(R.string.viewevent_delete_desc)
                            .setTitle(R.string.viewevent_delete_title);
                    final AlertDialog dialog = builder.create();

                    dialog.show();
                    break;
                default:

                    break;
            }
        }
    }
}
