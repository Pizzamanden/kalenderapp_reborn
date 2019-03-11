package com.example.kalenderapp_reborn.room;

import android.arch.persistence.room.Database;
import android.arch.persistence.room.RoomDatabase;

import com.example.kalenderapp_reborn.interfaces.DaoCalendarEntries;

@Database(entities = {CalendarEntries.class}, version = 2)
public abstract class DatabaseCalendarEntries extends RoomDatabase {
    public abstract DaoCalendarEntries daoCalendarEntries();
}
