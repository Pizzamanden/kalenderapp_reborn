package com.example.kalenderapp_reborn.adapters;


import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.kalenderapp_reborn.R;

import java.util.ArrayList;

public class CounterDialogAdapter extends RecyclerView.Adapter<CounterDialogAdapter.ViewHolder> {

    private ArrayList<Integer> numberList;

    public CounterDialogAdapter(ArrayList<Integer> numberList){
        this.numberList = numberList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.recyclerview_counterdialog, viewGroup, false);
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
        }
    }
}
