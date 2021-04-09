package com.withullc.app.withu.utils;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.withullc.app.withu.R;
import com.withullc.app.withu.model.dataObjects.Progress;
import com.withullc.app.withu.model.dataObjects.Walk;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by shantanusinghal on 10/12/17.
 */

public class TripHistoryAdapter extends android.support.v7.widget.RecyclerView.Adapter<TripHistoryAdapter.TripViewHolder> {

    private List<Walk> walks = new ArrayList<>();

    public static final Format TIME = new SimpleDateFormat("dd MMM ''yy 'at' hh:mm aaa");

    public void setWalks(List<Walk> walks) {
        this.walks = walks;
    }

    public class TripViewHolder extends RecyclerView.ViewHolder {
        private View durationIcon, distanceIcon, activeStamp, ratingsStamp, cancelledStamp;
        public TextView dateTime, origin, destination, duration, distance, rating;
        public TripViewHolder(View view) {
            super(view);
            dateTime = view.findViewById(R.id.card_header_date_time);
            origin = view.findViewById(R.id.card_orig_text);
            destination = view.findViewById(R.id.card_dest_text);
            duration = view.findViewById(R.id.card_durn_text);
            distance = view.findViewById(R.id.card_dist_text);
            rating = view.findViewById(R.id.card_rating_text);

            activeStamp = view.findViewById(R.id.card_header_active);
            ratingsStamp = view.findViewById(R.id.card_header_rating);
            cancelledStamp = view.findViewById(R.id.card_header_cancelled);
            distanceIcon = view.findViewById(R.id.card_dist_icon);
            durationIcon = view.findViewById(R.id.card_durn_icon);
        }
    }

    @Override
    public TripViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.fragment_trip_history_card, parent, false);

        return new TripViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(TripViewHolder holder, int position) {
        Walk walk = walks.get(position);
        holder.dateTime.setText(TIME.format(new Date(walk.getStartTime())));
        holder.origin.setText(walk.getOrig().getProvider());
        holder.destination.setText(walk.getDest().getProvider());
        if(walk.getProgress().equals(Progress.COMPLETED)) {
            holder.rating.setText(String.valueOf(walk.getRating()));
            holder.duration.setText(walk.getDuration());
            holder.distance.setText(walk.getDistance());
        } else if(walk.getProgress().equals(Progress.CANCELLED)) {
            hideDistanceDurationAndRating(holder);
            holder.cancelledStamp.setVisibility(View.VISIBLE);
        } else {
            hideDistanceDurationAndRating(holder);
            holder.activeStamp.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return walks.size();
    }

    private void hideDistanceDurationAndRating(TripViewHolder holder) {
        holder.duration.setVisibility(View.GONE);
        holder.distance.setVisibility(View.GONE);
        holder.durationIcon.setVisibility(View.GONE);
        holder.distanceIcon.setVisibility(View.GONE);
        holder.ratingsStamp.setVisibility(View.GONE);
    }
}
