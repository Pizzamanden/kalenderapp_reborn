package com.example.kalenderapp_reborn.dataobjects;

public class DataJsonSQLInsertCalendarEntries {

    // This class is for making an object with data for table at www.folderol.dk
    // Inserts only

    private String userID;
    private String request;
    private DataJsonCalendarEntries calendarEntriesData;
    private String tokenString;


    DataJsonSQLInsertCalendarEntries(String userID, String request, DataJsonCalendarEntries calendarEntriesData, String tokenString){
        this.userID = userID;
        this.request = request;
        this.calendarEntriesData = calendarEntriesData;
        this.tokenString = tokenString;
    }

    public String getUserID() {
        return userID;
    }

    public String getRequest() {
        return request;
    }

    public DataJsonCalendarEntries getCalendarEntriesData() {
        return calendarEntriesData;
    }
}