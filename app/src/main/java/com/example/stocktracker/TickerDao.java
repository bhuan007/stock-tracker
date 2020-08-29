package com.example.stocktracker;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface TickerDao {

    @Insert
    void insertSingleTicker(TickerModel ticker);

    @Delete
    void deleteSingleTicker(TickerModel ticker);

    @Update
    void updateSingleTicker(TickerModel ticker);

    @Query("SELECT * FROM tickers")
    List<TickerModel> getAllTickers();

    @Query("SELECT * FROM tickers WHERE id=:id")
    TickerModel getSingleTicker(int id);

    @Query("SELECT * FROM tickers WHERE symbol=:symbol")
    TickerModel getSingleTickerByName(String symbol);


}
