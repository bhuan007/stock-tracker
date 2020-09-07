package com.example.stocktracker;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class OverviewDetailFragment extends Fragment {


    Context context;
    TextView txtClose, txtOpen, txtHigh, txtLow, txtVolume;
    String open, high, low, close, volume;

    WebAPI api = new WebAPI();


    public OverviewDetailFragment(Context context) {
        this.context = context;
    }
    public OverviewDetailFragment() {}
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        ConstraintLayout layout = (ConstraintLayout) inflater.inflate(R.layout.fragment_overview_detail, container, false);
        initViews(layout);

        //Write more code below
        if (getArguments() != null) {
            open = getArguments().getString("open");
            high = getArguments().getString("high");
            low = getArguments().getString("low");
            close = getArguments().getString("close");
            volume = getArguments().getString("volume");


            txtOpen.setText(open);
            txtHigh.setText(high);
            txtLow.setText(low);
            txtClose.setText(close);
            txtVolume.setText(volume);


        }
        else {
            Toast.makeText(context, "Sorry, there was an error.", Toast.LENGTH_SHORT).show();
        }




        return layout;
    }

    private void initViews(ConstraintLayout layout) {

        txtClose = layout.findViewById(R.id.txtClose);
        txtHigh = layout.findViewById(R.id.txtHigh);
        txtOpen = layout.findViewById(R.id.txtOpen);
        txtLow = layout.findViewById(R.id.txtLow);
        txtVolume = layout.findViewById(R.id.txtVolume);
    }
}