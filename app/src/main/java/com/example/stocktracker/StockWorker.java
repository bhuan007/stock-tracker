package com.example.stocktracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Calendar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StockWorker extends Worker {
    private static final String TAG = "StockWorker";
    public static final String STOCK_TICKER = "stockTicker";
    public static final String STOCK_LOW_THRESHOLD = "stockLowThreshold";
    public static final String STOCK_HIGH_THRESHOLD = "stockHighThreshold";
    public static final String STOCK_NAME = "stockName";
    public static final String STOCK_ID = "stockId";

    private CompositeDisposable disposable = new CompositeDisposable();

    private WebAPI api = new WebAPI();
    private String notiTitle, notiText;
    private Boolean sendNotification = false;


    public StockWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // TODO: Make api call and check current stock price against user set thresholds.
        //  If threshold is passed, send a notification. Update the current price.

        Calendar calendar = Calendar.getInstance();
        DateFormat format = DateFormat.getDateInstance();

        Log.d(TAG, "Background service started at: " + format.format(calendar.getTime()));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("threshold_notification", "stock_notification", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getApplicationContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // We need to receive the id
        Data inputData = getInputData();
        String symbol = inputData.getString(STOCK_TICKER);
        String stockName = inputData.getString(STOCK_NAME);
        Double lowThreshold = inputData.getDouble(STOCK_LOW_THRESHOLD, -1);
        Double highThreshold = inputData.getDouble(STOCK_HIGH_THRESHOLD, -1);
        int stockId = inputData.getInt(STOCK_ID, -1);

        final Double[] closePrice = new Double[1];

        if (symbol != null) {
            api.getDailyData(symbol)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<String>() {
                        @Override
                        public void onSubscribe(@io.reactivex.rxjava3.annotations.NonNull Disposable d) {

                            disposable.add(d);
                        }

                        @Override
                        public void onNext(@io.reactivex.rxjava3.annotations.NonNull String s) {
                            JsonElement fileElement = JsonParser.parseString(s);
                            JsonObject fileObject = fileElement.getAsJsonObject();

                            JsonObject metaData = fileObject.getAsJsonObject("Meta Data");
                            String lastRefreshDate = metaData.get("3. Last Refreshed").getAsString();

                            JsonObject timeSeries = fileObject.getAsJsonObject("Time Series (Daily)").getAsJsonObject(lastRefreshDate);

                            closePrice[0] = timeSeries.get("4. close").getAsDouble();
                            Log.d(TAG, "current closePrice is " + closePrice[0]);

                            NumberFormat formatter = NumberFormat.getCurrencyInstance();

                            String closePriceString = formatter.format((Double)closePrice[0]);
                            String lowThreshString = formatter.format(lowThreshold);
                            String highThreshString = formatter.format(highThreshold);

                            String lowThreshTitle = stockName + " (" + symbol + ") " + "Low Threshold Alert!";
                            String highThreshTitle = stockName + " (" + symbol + ") " + "High Threshold Alert!";
                            // LOW AND HIGH THRESHOLD
                            if (lowThreshold != -1 && highThreshold != -1) {

                                if (closePrice[0] < lowThreshold) {
                                    notiTitle = lowThreshTitle;
                                    notiText = symbol + "'s price is now at " + closePriceString + ". \nYour low threshold is currently set at " + lowThreshString + ".";
                                    sendNotification = true;
                                }
                                else if (closePrice[0] > highThreshold) {
                                    notiTitle = highThreshTitle;
                                    notiText = symbol + "'s price is now at " + closePriceString + ". \nYour high threshold is currently set at " + highThreshString + ".";
                                    sendNotification = true;
                                }
                            }
                            // HIGH THRESHOLD ONLY
                            else if (lowThreshold == -1 && highThreshold != -1) {
                                if (closePrice[0] > highThreshold) {
                                    notiTitle = highThreshTitle;
                                    notiText = symbol + "'s price is now at " + closePriceString + ". \nYour high threshold is currently set at " + highThreshString + ".";
                                    sendNotification = true;
                                }

                            }
                            // LOW THRESHOLD ONLY
                            else if (highThreshold == -1 && lowThreshold != -1) {
                                if (closePrice[0] < lowThreshold) {
                                    notiTitle = lowThreshTitle;
                                    notiText = symbol + "'s price is now at " + closePriceString + ". \nYour low threshold is currently set at " + lowThreshString + ".";
                                    sendNotification = true;
                                }

                            }

                            if (sendNotification) {
                                NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "threshold_notification")
                                        .setSmallIcon(R.drawable.stock_tracker_logo)
                                        .setContentTitle(notiTitle)
                                        .setStyle(new NotificationCompat.BigTextStyle()
                                        .bigText(notiText))
                                        .setAutoCancel(true);

                                Intent actionIntent = new Intent(getApplicationContext(), StockDetailActivity.class);
                                actionIntent.putExtra(StockDetailActivity.SYMBOL_KEY, symbol);
                                actionIntent.putExtra(StockDetailActivity.IS_EXIST_KEY, true);
                                // TODO: put an ID here
                                actionIntent.putExtra(StockDetailActivity.TICKER_ID, stockId);
                                PendingIntent actionPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, actionIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                                builder.setContentIntent(actionPendingIntent);

                                NotificationManager notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                notificationManager.notify(0, builder.build());
                                Log.d(TAG, "Notification sent");
                            }

                            disposable.clear();

                        }

                        @Override
                        public void onError(@io.reactivex.rxjava3.annotations.NonNull Throwable e) {

                        }

                        @Override
                        public void onComplete() {

                        }
                    });

        }
        return Result.success();
    }
}
