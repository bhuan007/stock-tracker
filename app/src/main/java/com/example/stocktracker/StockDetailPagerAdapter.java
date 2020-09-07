package com.example.stocktracker;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.Calendar;

public class StockDetailPagerAdapter extends FragmentPagerAdapter {

    private static final String TAG = "StockDetailPagerAdapter";
    Context context;
    //Daily Series
    String open, high, low, close, volume;
    Boolean isExist;
    int id;
    Double closePrice;

    //Overview
    String symbol, description, address, exchange, sector, industry, lastRefresh, stockName;
    Calendar calendar = Calendar.getInstance();

    public StockDetailPagerAdapter(@NonNull FragmentManager fm, int behavior, Context context) {
        super(fm, behavior);
        this.context = context;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "Overview";
            case 1:
                return "Tracking Settings";
            case 2:
                return "Description";
        }
        return null;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                Bundle overviewBundle = new Bundle();
                overviewBundle.putString("open", open);
                overviewBundle.putString("high", high);
                overviewBundle.putString("low", low);
                overviewBundle.putString("close", close);
                overviewBundle.putString("volume", volume);

                Log.d(TAG, "open: " + open);


                OverviewDetailFragment overviewFrag = new OverviewDetailFragment(context);
                overviewFrag.setArguments(overviewBundle);

                return overviewFrag;

            case 1:
                Bundle trackerBundle = new Bundle();
                trackerBundle.putString("lastRefresh", lastRefresh);
                trackerBundle.putString("symbol", symbol);
                trackerBundle.putDouble("currentPrice", closePrice);
                trackerBundle.putBoolean("isExist", isExist);
                trackerBundle.putInt("id", id);
                trackerBundle.putString("stockName", stockName);
                TrackDetailFragment trackFrag = new TrackDetailFragment(context);
                trackFrag.setArguments(trackerBundle);

                return trackFrag;

            case 2:
                Bundle descriptionBundle = new Bundle();

                Log.d(TAG, "getItem: address is " + address);
                descriptionBundle.putString("description", description);
                descriptionBundle.putString("address", address);
                descriptionBundle.putString("exchange", exchange);
                descriptionBundle.putString("sector", sector);
                descriptionBundle.putString("industry", industry);

                DescriptionDetailFragment descriptionFrag = new DescriptionDetailFragment();
                descriptionFrag.setArguments(descriptionBundle);

                return descriptionFrag;

        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    public void setIsExist (Boolean isExist, int id) {
        this.isExist = isExist;
        this.id = id;
    }

    public void setDailyInfo(String lastRefresh, String open, String high, String low, String close, String volume, Double closePrice) {
        this.closePrice = closePrice;
        this.lastRefresh = lastRefresh;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        Log.d(TAG, "open: " + this.open);
    }

    public void setOverviewInfo(String stockName, String symbol, String description, String address, String exchange, String sector, String industry) {
        this.symbol = symbol;
        this.description = description;
        this.address = address;
        this.exchange = exchange;
        this.sector = sector;
        this.industry = industry;
        this.stockName = stockName;

    }
}
