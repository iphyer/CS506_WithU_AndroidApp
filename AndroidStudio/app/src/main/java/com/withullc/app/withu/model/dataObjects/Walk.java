package com.withullc.app.withu.model.dataObjects;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.concurrent.TimeUnit;

/**
 * Data Model Object for Walk data
 */

public class Walk implements Parcelable {

    private static final long serialVersionUID = 7526472295622776147L;

    private String walkId;
    private Location orig;
    private Location dest;
    private String walker1Id;
    private String walker2Id;
    private String userId;
    private long startTime;
    private long endTime;
    private Progress progress;
    private double rating;

    public Walk(Location orig, Location dest, String walker1Id, String walker2Id, String userId) {
        this.orig = orig;
        this.dest = dest;
        this.walker1Id = walker1Id;
        this.walker2Id = walker2Id;
        this.userId = userId;
    }

    public Walk(String walkId, Location orig, Location dest, String walker1Id, String walker2Id, String userId, Long startTime, Long endTime, String progress, double rating) {
        this.walkId = walkId;
        this.orig = orig;
        this.dest = dest;
        this.walker1Id = walker1Id;
        this.walker2Id = walker2Id;
        this.userId = userId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.progress = Progress.valueOf(progress);
        this.rating = rating;
    }

    public Walk(Parcel parcel) {
        this.walkId = parcel.readString();
        this.orig = parcel.readParcelable(Location.class.getClassLoader());
        this.dest = parcel.readParcelable(Location.class.getClassLoader());
        this.walker1Id = parcel.readString();
        this.walker2Id = parcel.readString();
        this.userId = parcel.readString();
        this.startTime = parcel.readLong();
        this.endTime = parcel.readLong();
        this.progress = Progress.valueOf(parcel.readString());
        this.rating = parcel.readDouble();
    }

    public static final Parcelable.Creator<Walk> CREATOR = new Parcelable.Creator<Walk>() {
        public Walk createFromParcel(Parcel in) {
            return new Walk(in);
        }

        public Walk[] newArray(int size) {
            return new Walk[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.walkId);
        dest.writeParcelable(this.orig, flags);
        dest.writeParcelable(this.dest, flags);
        dest.writeString(this.walker1Id);
        dest.writeString(this.walker2Id);
        dest.writeString(this.userId);
        dest.writeLong(this.startTime);
        dest.writeLong(this.endTime);
        dest.writeString(this.progress.toString());
        dest.writeDouble(this.rating);
    }

    public String getDuration() {
        return calculateDuration();
    }

    public String getDistance() {
        return calculateDistance();
    }

    public String getWalkId() {
        return walkId;
    }

    public void setWalkId(String walkId) {
        this.walkId = walkId;
    }

    public Location getDest() {
        return dest;
    }

    public void setDest(Location dest) {
        this.dest = dest;
    }

    public Location getOrig() {
        return orig;
    }

    public void setOrig(Location orig) {
        this.orig = orig;
    }

    public String getWalker1Id() {
        return walker1Id;
    }

    public void setWalker1Id(String walker1Id) {
        this.walker1Id = walker1Id;
    }

    public String getWalker2Id() {
        return walker2Id;
    }

    public void setWalker2Id(String walker2Id) {
        this.walker2Id = walker2Id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public Progress getProgress() {
        return progress;
    }

    public void setProgress(Progress progress) {
        this.progress = progress;
    }

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    /**
     * Find the difference between the start time and the end time and
     * calculate the duration of the walk
     */
    private String calculateDuration() {
        long mins = TimeUnit.MINUTES.convert(endTime - startTime, TimeUnit.MILLISECONDS);
        if(mins < 1) mins = 1L;
        return mins > 60 ? String.format("%02d hr %02d mins", ((int)(mins / 60)), mins % 60) : String.format("%02d mins", mins % 60);
    }

    /**
     * Calculates the distance covered by the walkers during the walk
     *
     * @return The total distance covered during the walk in miles.
     */
    private String calculateDistance() {
        return String.format("%.2f mi", orig.distanceTo(dest) * 0.00062137);
    }
}
