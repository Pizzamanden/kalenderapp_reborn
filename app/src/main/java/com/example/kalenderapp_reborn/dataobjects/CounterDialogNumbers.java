package com.example.kalenderapp_reborn.dataobjects;

public class CounterDialogNumbers {

    private int days;
    private int hours;
    private int mins;

    public CounterDialogNumbers(int days, int hours, int mins){
        this.days = days;
        this.hours = hours;
        this.mins = mins;
    }

    public int getDays(){ return this.days; }
    public int getHours(){ return this.hours; }
    public int getMins(){ return this.mins; }
}
