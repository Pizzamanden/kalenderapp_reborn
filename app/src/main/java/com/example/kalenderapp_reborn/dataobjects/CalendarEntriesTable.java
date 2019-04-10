package com.example.kalenderapp_reborn.dataobjects;

public class CalendarEntriesTable {

    // This class holds all data for/from the Calendar_entries table
    // Can be used for everything, as it is only for holding the data

    private int eventID;
    private String eventName;
    private String eventStartTime;
    private String eventEndTime;
    private String eventTimeZone;
    private int eventType;
    private boolean eventAlarmStatus;
    private String eventAlarmTime;

    public CalendarEntriesTable(String eventName, String eventStartTime, String eventEndTime, String eventTimeZone, int eventType, boolean eventAlarmStatus, String eventAlarmTime){
        this.eventName = eventName;
        this.eventStartTime = eventStartTime;
        this.eventEndTime = eventEndTime;
        this.eventTimeZone = eventTimeZone;
        this.eventType = eventType;
        this.eventAlarmStatus = eventAlarmStatus;
        this.eventAlarmTime = eventAlarmTime;
    }

    public int getEventID() {
        return eventID;
    }
    public String getEventName() {
        return this.eventName;
    }
    public String getEventStartTime() {
        return this.eventStartTime;
    }
    public String getEventEndTime() {
        return this.eventEndTime;
    }
    public String getEventTimeZone() {
        return this.eventTimeZone;
    }
    public int getEventType() {
        return this.eventType;
    }
    public boolean getEventAlarmStatus() {
        return this.eventAlarmStatus;
    }
    public String getEventAlarmTime() {
        return this.eventAlarmTime;
    }
}
