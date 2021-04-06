package com.withullc.app.withu.model.dataObjects;

import android.app.Application;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.maps.model.LatLng;
import com.withullc.app.withu.model.service.PairService;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Model Object for User information
 */

public class User implements Serializable{

    private String email;
    private Map<String,Double> location;
    private String name;
    private String pictureRef;
    private String gender;
    private String pairID;
    private Boolean isWalker;
    private Boolean isActive;
    private String phoneNumb;
    private String walkID;

    //used for retrieveUser
    public User (){
    }

    public User(String email, String name, String pictureRef, String gender, boolean isWalker, String phoneNumb) {
        this.email = email;
        this.name = name;
        this.pictureRef = pictureRef;
        this.gender = gender;
        this.isWalker = isWalker;
        this.phoneNumb = phoneNumb;
        this.walkID = "";

        this.location = new HashMap<String, Double>();

        location.put("latitude", 0.0);
        location.put("longitude", 0.0);

        //not sure how we want to handle this, do we default it to yes upon sign up then have
        //it continuously checking if app is still active?
        this.isActive = false;

        //default it to null at start to handle if user chooses to not be a walker or
        // handles since wont be in a pair right away
        this.pairID = null;
    }

    public String getWalkID() {return this.walkID; }
    public void setWalkID(String walkID) { this.walkID = walkID;}


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Map<String,Double> getLocation() {
        return location;
    }

    public void setLocation(Map<String,Double> location) {
        this.location = location;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPictureRef() {
        return pictureRef;
    }

    public void setPictureRef(String pictureRef) {
        this.pictureRef = pictureRef;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPairID() {
        return pairID;
    }

    public void setPairID(String pairID) {
        this.pairID = pairID;
    }

    public Boolean getWalker() {
        return isWalker;
    }

    public void setWalker(Boolean walker) {
        isWalker = walker;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public String getPhoneNumb() {
        return phoneNumb;
    }

    public void setPhoneNumb(String phoneNumb) {
        this.phoneNumb = phoneNumb;
    }
}
