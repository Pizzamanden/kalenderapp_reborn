package com.example.kalenderapp_reborn.room;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class CalendarEntries {
    @NonNull
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String name;
    private String startDate;
    private String endDate;
    private String type;
    private String alarmset;
    private String alarmDate;






    public CalendarEntries()
    {

    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getStartDate()
    {
        return startDate;
    }

    public void setStartDate (String startDate)
    {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getAlarmset() {
        return alarmset;
    }

    public void setAlarmset(String alarmset) {
        this.alarmset = alarmset;
    }

    public String getAlarmDate() {
        return alarmDate;
    }

    public void setAlarmDate(String alarmDate) {
        this.alarmDate = alarmDate;
    }
}
