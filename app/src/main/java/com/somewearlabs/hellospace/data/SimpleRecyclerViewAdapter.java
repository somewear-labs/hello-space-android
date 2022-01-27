package com.somewearlabs.hellospace.data;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<SimpleRecyclerViewAdapter.EventViewHolder> {
    private List<String> items = new ArrayList<>();

    public void setItems(List<String> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return SimpleRecyclerViewAdapter.EventViewHolder.build(parent);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        TextView textView = (TextView) holder.itemView;
        textView.setText(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {

        private EventViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        static SimpleRecyclerViewAdapter.EventViewHolder build(ViewGroup parent) {
            View v = LayoutInflater
                    .from(parent.getContext())
                    .inflate(android.R.layout.simple_list_item_1, parent, false);
            return new SimpleRecyclerViewAdapter.EventViewHolder(v);
        }
    }
}
