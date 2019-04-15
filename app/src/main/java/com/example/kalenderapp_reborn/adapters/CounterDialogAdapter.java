package com.example.kalenderapp_reborn.adapters;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kalenderapp_reborn.R;

import java.util.ArrayList;

import static android.support.constraint.Constraints.TAG;

public class CounterDialogAdapter extends RecyclerView.Adapter<CounterDialogAdapter.ViewHolder> {

    private ArrayList<Integer> numberList;

    public CounterDialogAdapter(ArrayList<Integer> numberList){
        this.numberList = numberList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_counterdialog, viewGroup, false);
        // work here if you need to control height of your items
        // keep in mind that parent is RecyclerView in this case
        int height = (viewGroup.getMeasuredHeight() + 3) / 3;
        Log.d(TAG, "onCreateViewHolder: " + viewGroup.getMeasuredHeight());
        Log.d(TAG, "onCreateViewHolder: " + height);
        view.setMinimumHeight(height);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.numberView.setText(String.valueOf(numberList.get(i % numberList.size())));
    }

    @Override
    public int getItemCount() {
        return Integer.MAX_VALUE;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        // Declare views
        TextView numberView;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            numberView = itemView.findViewById(R.id.textView_number);

            // Manage text size, padding, general height and more here, depending on recyclerview height
            //numberView.setHeight((recyclerViewHeight + 1)/3);
        }
    }
}
