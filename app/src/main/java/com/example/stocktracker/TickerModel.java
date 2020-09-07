package com.example.stocktracker;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;


@Entity(tableName = "tickers")
public class TickerModel {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private String symbol;
    @ColumnInfo(name = "stock_name")
    private String stockName;
    @ColumnInfo(name = "low_threshold")
    private Double lowThreshold;
    @ColumnInfo(name = "high_threshold")
    private Double highThreshold;
    @ColumnInfo(name = "current_price")
    private Double currentPrice;
    @ColumnInfo(name = "start_date")
    private Long startDate;
    @ColumnInfo(name = "last_refresh_date")
    private String lastRefreshDate;
    @ColumnInfo(name = "repeat_interval")
    private Long repeatInterval;
    @ColumnInfo(name = "interval_type")
    private String intervalType;

    @Ignore
    public TickerModel(String symbol) {
        this.symbol = symbol;
    }

    @Ignore
    public TickerModel() {
    }

    public TickerModel(String symbol, String stockName, Double lowThreshold, Double highThreshold, Double currentPrice, Long startDate, String lastRefreshDate, Long repeatInterval, String intervalType) {
        this.symbol = symbol;
        this.stockName = stockName;
        this.lowThreshold = lowThreshold;
        this.highThreshold = highThreshold;
        this.currentPrice = currentPrice;
        this.startDate = startDate;
        this.lastRefreshDate = lastRefreshDate;
        this.repeatInterval = repeatInterval;
        this.intervalType = intervalType;
    }

    public String getIntervalType() {
        return intervalType;
    }

    public void setIntervalType(String intervalType) {
        this.intervalType = intervalType;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Double getLowThreshold() {
        return lowThreshold;
    }

    public void setLowThreshold(Double lowThreshold) {
        this.lowThreshold = lowThreshold;
    }

    public Double getHighThreshold() {
        return highThreshold;
    }

    public void setHighThreshold(Double highThreshold) {
        this.highThreshold = highThreshold;
    }

    public Double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(Double currentPrice) {
        this.currentPrice = currentPrice;
    }

    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public String getLastRefreshDate() {
        return lastRefreshDate;
    }

    public void setLastRefreshDate(String lastRefreshDate) {
        this.lastRefreshDate = lastRefreshDate;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getStockName() {
        return stockName;
    }

    public void setStockName(String stockName) {
        this.stockName = stockName;
    }
}
