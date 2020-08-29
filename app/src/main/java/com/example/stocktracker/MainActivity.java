package com.example.stocktracker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.CursorAdapter;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.annotations.NonNull;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import io.reactivex.rxjava3.core.*;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    Toolbar toolbar;
    RecyclerView mainRecView;
    SearchView search;

    Handler handler = new Handler();
    long delay = 500;
    long last_edit = 0;
    String searchText;

    private CompositeDisposable disposable = new CompositeDisposable();
    private StockRecAdapter adapter = new StockRecAdapter(this);
    private WebAPI api = new WebAPI();
    private TickerDatabase db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        adapter.setTickers();
        initViews();

        setSupportActionBar(toolbar);
        db = TickerDatabase.getInstance(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_toolbar, menu);
        search = (SearchView) menu.findItem(R.id.searchTicker).getActionView();
        searchCode();




        return super.onCreateOptionsMenu(menu);
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        disposable.clear();
    }

    @Override
    public void onBackPressed() {
        if (!search.isIconified()) {
            search.setIconified(true);
        } else {
            super.onBackPressed();
        }

    }




    private Runnable input_check = new Runnable() {
        @Override
        public void run() {
            if (System.currentTimeMillis() > (last_edit + delay)) {
                apiSearch();
            }
        }
    };

    private void searchCode() {

        search.setQueryHint("Search a Symbol: TSLA");
        //Setting search text threshold
        int autoCompleteID = getResources().getIdentifier("search_src_text", "id", getPackageName());
        AutoCompleteTextView autoCompleteTextView = search.findViewById(autoCompleteID);
        autoCompleteTextView.setThreshold(1);

        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(
                MainActivity.this,
                R.layout.search_list_item,
                null, new String[] {SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_TEXT_2},
                new int[] {R.id.search_symbol, R.id.search_name},
                CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);

        search.setSuggestionsAdapter(cursorAdapter);


        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchText = query;
                apiSearch();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                if (newText.length() > 0) {
                    searchText = newText;
                    last_edit = System.currentTimeMillis();

                    handler.postDelayed(input_check, delay);
                }
                else {
                    search.getSuggestionsAdapter().changeCursor(null);
                }

                return true;
            }
        });

        // Listening to user selecting from suggestions list
        search.setOnSuggestionListener(new SearchView.OnSuggestionListener() {
            @Override
            public boolean onSuggestionSelect(int position) {
                Cursor cursor = (Cursor) search.getSuggestionsAdapter().getItem(position);
                String symbol = cursor.getString(cursor.getColumnIndex(SearchManager.SUGGEST_COLUMN_TEXT_1));

                Intent intent = new Intent(MainActivity.this, StockDetailActivity.class);
                intent.putExtra(StockDetailActivity.SYMBOL_KEY, symbol);

                TickerModel tickerCheck = db.tickerDao().getSingleTickerByName(symbol);

                if (tickerCheck != null) {
                    Log.d(TAG, "onSuggestionSelect: symbol is " + symbol);
                    Log.d(TAG, "onSuggestionSelect: tickerCheck symbol is " + tickerCheck.getSymbol());


                    if (tickerCheck.getSymbol().equals(symbol)) {
                        intent.putExtra(StockDetailActivity.TICKER_ID, tickerCheck.getId());
                        intent.putExtra(StockDetailActivity.IS_EXIST_KEY, true);
                    }
                }


                startActivity(intent);
                cursor.close();

                search.setIconified(true);


                return true;
            }

            @Override
            public boolean onSuggestionClick(int position) {
                return onSuggestionSelect(position);
            }
        });
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(this.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = this.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void apiSearch() {
        api.getSymbols(searchText)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Cursor>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {
                        disposable.add(d);
                    }

                    @Override
                    public void onNext(@NonNull Cursor cursor) {
                        search.getSuggestionsAdapter().changeCursor(cursor);
                        search.getSuggestionsAdapter().notifyDataSetChanged();

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolBar);
        mainRecView = findViewById(R.id.mainRecView);

        mainRecView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
        mainRecView.setAdapter(adapter);

    }
}