package com.example.stocktracker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {TickerModel.class}, version = 5)
public abstract class TickerDatabase extends RoomDatabase {

    public abstract TickerDao tickerDao();

    private static TickerDatabase instance;

    public static synchronized TickerDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, TickerDatabase.class, "ticker_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }

        return instance;
    }
}
