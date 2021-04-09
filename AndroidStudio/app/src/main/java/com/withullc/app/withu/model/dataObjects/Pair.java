package com.withullc.app.withu.model.dataObjects;

/**
 * Data Model Object for Pair data
 */

public class Pair {
    private String walker1Id;
    private String walker2Id;
    private String walkId;

    public Pair(String walker1Id, String walker2Id, String walkId) {
        this.walker1Id = walker1Id;
        this.walker2Id = walker2Id;
        this.walkId = walkId;
    }

    public Pair() { }

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

    public String getWalkId() {
        return walkId;
    }

    public void setWalkId(String walkId) {
        this.walkId = walkId;
    }
}
