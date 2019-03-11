package com.example.vijay.contactsuggestionframework.DataCollection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataInterface {

    private int day = 0;
    private int hour = 0;
    private double lat = 0.0;
    private double lon = 0.0;
    private int CallType = 0;
    private String contactName = null;
    private String phoneNumber = null;
    private long duration = 0;
    private List<String> packageName= null;

    public DataInterface(){
    }

    public void setDay(int day){
        this.day = day;
    }
    public int getDay() {
        return day;
    }

    public void setHour(int hour) {
        this.hour = hour;
    }

    public int getHour() {
        return hour;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }
    public double getLat() {
        return lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public void setContactName(String contactName) {

        this.contactName = contactName;
    }

    public String getContactName() {
        return contactName;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public void setCallType(int setCallType) {
        this.CallType = setCallType;
    }

    public int getGetCallType() {
        return CallType;
    }


    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPackage(List<String> packages){
     this.packageName = packages;
    }

    public List<String> getPackageName() {
        return packageName;
    }


}
