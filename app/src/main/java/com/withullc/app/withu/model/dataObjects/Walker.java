package com.withullc.app.withu.model.dataObjects;

/**
 * Data Model Object for the Walker data
 */

public class Walker {

    private String walkerId;
    private boolean isActive;
    private String pairId;
    private long numWalks;
    private long totalRating;

    public Walker(String walkerId, boolean isActive, String pairId,
                  long numWalks, long totalRating) {
        this.walkerId = walkerId;
        this.isActive = isActive;
        this.pairId = pairId;
        this.numWalks = numWalks;
        this.totalRating = totalRating;
    }

    public String getWalkerId() {
        return walkerId;
    }

    public void setWalkerId(String walkerId) {
        this.walkerId = walkerId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getPairId() {
        return pairId;
    }

    public void setPairId(String pairId) {
        this.pairId = pairId;
    }

    public long getNumWalks() {
        return numWalks;
    }

    public void setNumWalks(long numWalks) {
        this.numWalks = numWalks;
    }

    public long getTotalRating() {
        return totalRating;
    }

    public void setTotalRating(long totalRating) {
        this.totalRating = totalRating;
    }
}
