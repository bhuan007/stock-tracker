package com.example.stocktracker;

import android.app.SearchManager;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;

import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


public class WebAPI {
    private static final String TAG = "WebAPI";

    public Observable<Cursor> getSymbols(String ticker) {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url("https://www.alphavantage.co/query?function=SYMBOL_SEARCH&keywords=" + ticker + "&apikey=XYMPL5T1DU2XPZ8P").build();

        final String[] sAutocompleteNames = new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2};

        return Observable.create(new ObservableOnSubscribe<Cursor>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Cursor> emitter) throws Throwable {
                MatrixCursor cursor = new MatrixCursor(sAutocompleteNames);

                try {
                    Log.d(TAG, "getSymbols: called");
                    Response response = client.newCall(request).execute();
                    String info = response.body().string();

                    JsonElement fileElement = JsonParser.parseString(info);
                    JsonArray fileArray = fileElement.getAsJsonObject().get("bestMatches").getAsJsonArray();


                    for (int i = 0; i < fileArray.size(); i++) {
                        JsonObject stock = fileArray.get(i).getAsJsonObject();
                        String stockSymbol = stock.get("1. symbol").getAsString();
                        String stockName = stock.get("2. name").getAsString();
                        Object[] row = new Object[] {i, stockSymbol, stockName};
                        cursor.addRow(row);
                    }
                    emitter.onNext(cursor);
                    emitter.onComplete();
                } catch (IOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }

            }
        });
    }

    // TODO: Remove parameter, add a foreach loop that loops through the database in the background
    public Observable<String> getDailyData(String ticker) {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=" + ticker + "&apikey=XYMPL5T1DU2XPZ8P").build();

        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
                try {
                    Log.d(TAG, "getDailyData: called");
                    Response response = client.newCall(request).execute();
                    String info = response.body().string();
                    emitter.onNext(info);
                    emitter.onComplete();
                } catch (IOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }
        });
    }

    public Observable<String> getOverviewData(String ticker) {
        final OkHttpClient client = new OkHttpClient();
        final Request request = new Request.Builder().url("https://www.alphavantage.co/query?function=OVERVIEW&symbol=" + ticker + "&apikey=XYMPL5T1DU2XPZ8P").build();

        return Observable.create(new ObservableOnSubscribe<String>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<String> emitter) throws Throwable {
                try{
                    Log.d(TAG, "getOverviewData: called");
                    Response response = client.newCall(request).execute();
                    String info = response.body().string();
                    emitter.onNext(info);
                    emitter.onComplete();
                } catch( IOException e) {
                    e.printStackTrace();
                    emitter.onError(e);
                }
            }
        });
    }
}
