package com.example.kalenderapp_reborn.interfaces;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.example.kalenderapp_reborn.room.CalendarEntries;

import java.util.List;

@Dao
public interface DaoCalendarEntries {

    @Insert
    void insertSingle(CalendarEntries entry);

    @Insert
    void insertMultiple (List<CalendarEntries> entry);

    @Query("SELECT * FROM CalendarEntries WHERE id = :id")
    CalendarEntries fetchById (int id);

    @Query("SELECT * FROM CalendarEntries")
    List<CalendarEntries> fetchAll();

    @Update
    void update (CalendarEntries id);

    @Delete
    void delete (CalendarEntries id);
}