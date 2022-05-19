package com.example.parentalcontrol;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TimeTableRecyclerViewAdapter extends RecyclerView.Adapter<TimeTableRecyclerViewAdapter.MyViewHolder>{
    Context mContext;
    ArrayList<TimeTableItem> listItems;

    public TimeTableRecyclerViewAdapter(Context mContext, ArrayList<TimeTableItem> listItems) {
        this.mContext = mContext;
        this.listItems = listItems;
    }
    public void setData(ArrayList<TimeTableItem> listItems){
        this.listItems = listItems;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.table_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        TimeTableItem item = listItems.get(position);
        if(item.isSelected){
            holder.tableItem.setBackgroundColor(mContext.getResources().getColor(R.color.grey));
        }
        else{
            holder.tableItem.setBackgroundColor(mContext.getResources().getColor(R.color.white));
        }
        holder.tableItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(item.isSelected == false){
                    for(int i = 0;i<listItems.size();i++){
                        listItems.get(i).isSelected = false;
                    }
                    listItems.get(position).isSelected = true;
                }
                else{
                    listItems.get(position).isSelected = false;
                    holder.tableItem.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                }
                notifyDataSetChanged();
            }
        });
        holder.from.setText(item.getFrom());
        holder.to.setText(item.getTo());
        holder.duration.setText(item.getDuration());
        holder.interval.setText(item.getInterval());
        holder.sum.setText(item.getSum());
    }

    @Override
    public int getItemCount() {
        return listItems.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        LinearLayout tableItem;
        TextView from, to, duration, interval, sum;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            tableItem = itemView.findViewById(R.id.tableItem);
            from = itemView.findViewById(R.id.from);
            to = itemView.findViewById(R.id.to);
            duration = itemView.findViewById(R.id.duration);
            interval = itemView.findViewById(R.id.interval);
            sum = itemView.findViewById(R.id.sum);
        }
    }
}
