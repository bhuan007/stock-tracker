package com.example.stocktracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.cardview.widget.CardView;
import androidx.work.WorkManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class StockRecAdapter extends RecyclerView.Adapter<StockRecAdapter.ViewHolder> {

    private static final String TAG = "StockRecAdapter";

    ArrayList<TickerModel> tickers = new ArrayList<>();
    Context context;
    Activity activity;
    TickerDatabase db;
    DeleteInterface callback;

    public StockRecAdapter(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
    }

    public void setTickers() {

        db = TickerDatabase.getInstance(context);

        List<TickerModel> dbTicker = db.tickerDao().getAllTickers();

        Log.d(TAG, "setTickers: dbTicker length is " + dbTicker.size());
        tickers.addAll(dbTicker);

        notifyDataSetChanged();


    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.stock_card_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        db = TickerDatabase.getInstance(context);

        callback = (DeleteInterface) context;

        holder.txtTickerName.setText(tickers.get(position).getSymbol());
        holder.txtRefresh.setText("Last Refreshed: " + tickers.get(position).getLastRefreshDate());
        holder.txtClose.setText("Closed at: " + tickers.get(position).getCurrentPrice());
        holder.txtStockName.setText(tickers.get(position).getStockName());

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance();
        Double lowThreshold = tickers.get(position).getLowThreshold();
        Double highThreshold = tickers.get(position).getHighThreshold();

        Double currentPrice = tickers.get(position).getCurrentPrice();
        Log.d(TAG, "current price for " + tickers.get(position).getStockName() + " is " + currentPrice);
        Log.d(TAG, "value of contex is " + context);

        if (lowThreshold != null && highThreshold != null) {
            String low = currencyFormat.format(lowThreshold);
            String high = currencyFormat.format(highThreshold);

            holder.txtLowThreshold.setText("Low Reminder: " + low);
            holder.txtHighThreshold.setText("High Reminder: " + high);

            if (currentPrice < lowThreshold) {
                holder.txtLowIndicator.setVisibility(View.VISIBLE);
            }
            else if (currentPrice > highThreshold) {
                holder.txtHighIndicator.setVisibility(View.VISIBLE);
            }


        }
        else if (lowThreshold == null && highThreshold != null) {
            String high = currencyFormat.format(highThreshold);
            holder.txtLowThreshold.setText("Low Reminder: N/A");
            holder.txtHighThreshold.setText("High Reminder: " + high);

            if (currentPrice > highThreshold) {
                holder.txtHighIndicator.setVisibility(View.VISIBLE);
            }

        }
        else if (lowThreshold != null && highThreshold == null) {
            String low = currencyFormat.format(lowThreshold);
            holder.txtLowThreshold.setText("Low Reminder: " + low);
            holder.txtHighThreshold.setText("High Reminder: N/A");

            if (currentPrice < lowThreshold) {
                holder.txtLowIndicator.setVisibility(View.VISIBLE);
            }
        }

        holder.parent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String symbol = tickers.get(position).getSymbol();
                Intent intent = new Intent(context, StockDetailActivity.class);
                intent.putExtra(StockDetailActivity.SYMBOL_KEY, symbol);
                intent.putExtra(StockDetailActivity.IS_EXIST_KEY, true);
                intent.putExtra(StockDetailActivity.TICKER_ID, tickers.get(position).getId());
                context.startActivity(intent);
            }
        });

        holder.btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setMessage("Are you sure you want to delete " + tickers.get(position).getSymbol())
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                callback.cancelAlarm(tickers.get(position).getId());

                                db.tickerDao().deleteSingleTicker(tickers.get(position));

                                tickers = (ArrayList<TickerModel>) db.tickerDao().getAllTickers();

                                callback.showEmptyText();


                                notifyItemRemoved(position);
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                builder.create().show();
            }
        });

    }

    @Override
    public int getItemCount() {
        return tickers.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {


        TextView txtTickerName, txtClose, txtRefresh, txtLowThreshold, txtHighThreshold, txtStockName, txtHighIndicator, txtLowIndicator;
        ImageButton btnDelete;
        CardView parent;

        public ViewHolder(@NonNull View itemView) {

            super(itemView);
            parent = itemView.findViewById(R.id.parent);
            txtTickerName = itemView.findViewById(R.id.txtTickerName);
            txtClose = itemView.findViewById(R.id.txtClose);
            txtRefresh = itemView.findViewById(R.id.txtRefresh);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            txtLowThreshold = itemView.findViewById(R.id.txtLowThreshold);
            txtStockName = itemView.findViewById(R.id.txtStockName);
            txtHighThreshold = itemView.findViewById(R.id.txtHighThreshold);

            txtHighIndicator = itemView.findViewById(R.id.txtHighIndicator);
            txtLowIndicator = itemView.findViewById(R.id.txtLowIndicator);
        }
    }

    public interface DeleteInterface {
        public void showEmptyText();
        public void cancelAlarm(int id);
    }

}
