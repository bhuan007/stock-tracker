package com.example.stocktracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class AlertReceiver extends BroadcastReceiver {

    private static final String TAG = "AlertReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {


        Log.d(TAG, "onReceive: Alarm activated");
        TickerDatabase db = TickerDatabase.getInstance(context);
        int id = intent.getIntExtra("id", -1);

        // Replace the following for each tickerModel
        TickerModel ticker = db.tickerDao().getSingleTicker(id);
        // Tag format: id_ticker
        String tag = ticker.getId() + "_" + ticker.getSymbol();
        Calendar currentDate = Calendar.getInstance();
        DateFormat dateFormat = DateFormat.getDateInstance();
        String stockName = ticker.getStockName();

        String currentDateString = dateFormat.format(currentDate.getTime());

        Log.d(TAG, "initWorker: currentDateString is " + currentDateString);

        Data data = null;

        //pass stock name into data, so we can show it on Notification
        if (ticker.getHighThreshold() != null && ticker.getLowThreshold() != null) {
            data = new Data.Builder()
                    .putString(StockWorker.STOCK_TICKER, ticker.getSymbol())
                    .putString(StockWorker.STOCK_NAME, stockName)
                    .putDouble(StockWorker.STOCK_LOW_THRESHOLD, ticker.getLowThreshold())
                    .putDouble(StockWorker.STOCK_HIGH_THRESHOLD, ticker.getHighThreshold())
                    .putInt(StockWorker.STOCK_ID, ticker.getId())
                    .build();
        }
        else if (ticker.getHighThreshold() != null) {
            data = new Data.Builder()
                    .putString(StockWorker.STOCK_TICKER, ticker.getSymbol())
                    .putString(StockWorker.STOCK_NAME, stockName)
                    .putDouble(StockWorker.STOCK_HIGH_THRESHOLD, ticker.getHighThreshold())
                    .putInt(StockWorker.STOCK_ID, ticker.getId())
                    .build();
        }
        else if (ticker.getLowThreshold() != null) {
            data = new Data.Builder()
                    .putString(StockWorker.STOCK_TICKER, ticker.getSymbol())
                    .putString(StockWorker.STOCK_NAME, stockName)
                    .putDouble(StockWorker.STOCK_LOW_THRESHOLD, ticker.getLowThreshold())
                    .putInt(StockWorker.STOCK_ID, ticker.getId())
                    .build();
        }

        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(StockWorker.class)
                .setInputData(data)
                .setConstraints(constraints)
                .addTag(tag)
                .build();

        //TODO: Might need mainactivity context
        WorkManager.getInstance(context).enqueue(workRequest);
        Log.d(TAG, "initWorker: enqued worker thread");
    }
}
