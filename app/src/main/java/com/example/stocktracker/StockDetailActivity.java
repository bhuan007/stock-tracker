package com.example.stocktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.tabs.TabLayout;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;


import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class StockDetailActivity extends AppCompatActivity {

    private static final String TAG = "StockDetailActivity";
    public final static String SYMBOL_KEY = "symbol";
    public final static String IS_EXIST_KEY = "isExist";
    public final static String TICKER_ID = "tickerId";

    CompositeDisposable disposable = new CompositeDisposable();
    WebAPI api = new WebAPI();
    String name, description, address, exchange, sector, industry;
    Toolbar toolbar;
    TextView txtSymbol, txtDate, txtName, txtRefreshSchedule, txtTracking;
    LinearLayout nextRefreshBlock;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TickerDatabase db = TickerDatabase.getInstance(this);
        setContentView(R.layout.activity_stock_detail);
        initViews();

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        StockDetailPagerAdapter adapter = new StockDetailPagerAdapter(getSupportFragmentManager(), FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT, this);



        Intent intent = getIntent();
        String symbol = intent.getExtras().getString(SYMBOL_KEY);
        Boolean isExist = intent.getBooleanExtra(IS_EXIST_KEY, false);
        Log.d(TAG, "onCreate: boolean value is " + isExist);
        int id = intent.getExtras().getInt(TICKER_ID);




        if (isExist) {
            nextRefreshBlock.setVisibility(View.VISIBLE);
            TickerModel ticker = db.tickerDao().getSingleTicker(id);
            Calendar currentTime = Calendar.getInstance();
            Long nextRefresh = ticker.getStartDate();

            while (currentTime.getTimeInMillis() > nextRefresh) {
                nextRefresh += ticker.getRepeatInterval();
                ticker.setStartDate(nextRefresh);
                db.tickerDao().updateSingleTicker(ticker);
            }
            String scheduledTime = new SimpleDateFormat("MMM dd, yyyy h:mm a").format(nextRefresh);

            txtRefreshSchedule.setText(scheduledTime);
            txtTracking.setText("TRACKING");
            txtTracking.setBackgroundColor(ContextCompat.getColor(this, R.color.dateSet));

        }



        api.getOverviewData(symbol)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<String>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull String s) {


                        try{
                            JsonElement fileElement = JsonParser.parseString(s);
                            JsonObject fileObject = fileElement.getAsJsonObject();


                            name = fileObject.get("Name").getAsString();
                            description = fileObject.get("Description").getAsString();
                            address = fileObject.get("Address").getAsString();
                            exchange = fileObject.get("Exchange").getAsString();
                            sector = fileObject.get("Sector").getAsString();
                            industry = fileObject.get("Industry").getAsString();


                            txtName.setText(name);
                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.d(TAG, symbol +"'s s on overview is: " + s);
                            checkApiMax(s, "Overview", symbol);

                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        api.getDailyData(symbol)
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(new Observer<String>() {
                                    @Override
                                    public void onSubscribe(@NonNull Disposable d) {
                                        disposable.add(d);
                                    }

                                    @Override
                                    public void onNext(@NonNull String s) {
                                        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
                                        NumberFormat numberFormat = NumberFormat.getNumberInstance();
                                        try {
                                            JsonElement fileElement = JsonParser.parseString(s);
                                            JsonObject fileObject = fileElement.getAsJsonObject();

                                            JsonObject metaData = fileObject.getAsJsonObject("Meta Data");
                                            String lastRefreshDate = metaData.get("3. Last Refreshed").getAsString();

                                            JsonObject timeSeries = fileObject.getAsJsonObject("Time Series (Daily)").getAsJsonObject(lastRefreshDate);

                                            Double closePrice = timeSeries.get("4. close").getAsDouble();

                                            String open = currencyFormat.format(timeSeries.get("1. open").getAsDouble());
                                            String high = currencyFormat.format(timeSeries.get("2. high").getAsDouble());
                                            String low = currencyFormat.format(timeSeries.get("3. low").getAsDouble());
                                            String close = currencyFormat.format(closePrice);

                                            String volume = numberFormat.format(timeSeries.get("5. volume").getAsDouble());


                                            if (isExist) {
                                                //Updating stock price and refresh date
                                                TickerModel tickerModel = db.tickerDao().getSingleTicker(id);
                                                tickerModel.setCurrentPrice(closePrice);
                                                tickerModel.setLastRefreshDate(lastRefreshDate);
                                                db.tickerDao().updateSingleTicker(tickerModel);
                                            }



                                            adapter.setDailyInfo(lastRefreshDate, open, high, low, close, volume, closePrice);
                                            adapter.setOverviewInfo(name, symbol, description, address, exchange, sector, industry);
                                            adapter.setIsExist(isExist, id);

                                            ViewPager pager = findViewById(R.id.pager);
                                            pager.setAdapter(adapter);

                                            TabLayout tabs = findViewById(R.id.tabLayout);
                                            tabs.setupWithViewPager(pager);
                                            txtDate.setText("Refreshed On: " + lastRefreshDate);
                                            txtSymbol.setText(symbol);


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                            Log.d(TAG, symbol + "'s Daily data's s is " + s);
                                            checkApiMax(s, "Daily Data", symbol);
                                        }
                                    }

                                    @Override
                                    public void onError(@NonNull Throwable e) {

                                    }

                                    @Override
                                    public void onComplete() {

                                    }
                                });
                    }
                });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(StockDetailActivity.this, MainActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onStop() {
        disposable.clear();
        super.onStop();
    }

    private void initViews() {
        txtSymbol = findViewById(R.id.txtSymbol);
        txtDate = findViewById(R.id.txtDate);
        txtName = findViewById(R.id.txtName);
        txtRefreshSchedule = findViewById(R.id.txtRefreshSchedule);
        txtTracking = findViewById(R.id.txtTracking);
        toolbar = findViewById(R.id.toolBar);

        nextRefreshBlock = findViewById(R.id.nextRefreshBlock);
    }

    private void checkApiMax(String s, String title, String symbol) {
        try {
            JsonElement fileElement = JsonParser.parseString(s);
            JsonObject fileObject = fileElement.getAsJsonObject();
            String apiMax = fileObject.get("Note").getAsString();
            if (!apiMax.isEmpty()) {
                Toast.makeText(StockDetailActivity.this, "Sorry. API calls exceeded. (" + title + ")", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(StockDetailActivity.this, "Sorry. " + symbol + "is unavailable.", Toast.LENGTH_SHORT).show();

        }

    }
}