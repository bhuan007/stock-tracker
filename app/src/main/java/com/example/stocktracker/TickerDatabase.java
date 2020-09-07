package com.example.stocktracker;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.work.WorkManager;

@Database(entities = {TickerModel.class}, version = 6)
public abstract class TickerDatabase extends RoomDatabase {

    public abstract TickerDao tickerDao();

    private static TickerDatabase instance;

    public static synchronized TickerDatabase getInstance(Context context) {
        if (instance == null) {
            WorkManager.getInstance(context).cancelAllWork();
            instance = Room.databaseBuilder(context, TickerDatabase.class, "ticker_db")
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }

        return instance;
    }
}
