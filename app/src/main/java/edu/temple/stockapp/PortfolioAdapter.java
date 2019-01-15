package edu.temple.stockapp;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class PortfolioAdapter extends ArrayAdapter {

    Context context;
    private int count;
    private List stocks;

    PortfolioAdapter(Context context, int resource, List objects) {
        super(context, resource, objects);
        this.stocks = objects;
        this.context = context;
        this.count = objects.size();
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public Object getItem(int i) {
        return stocks.get(i);
    }

    @NonNull
    @Override
    public View getView(int i, View view, @NonNull ViewGroup viewGroup) {

        String data = stocks.get(i).toString();

        String[] arr = data.split("\\|");

        String symbol = arr[0].toUpperCase();
        double current = Double.parseDouble(arr[2]);
        double open = Double.parseDouble(arr[3]);

        TextView textTickerName;

        if (view == null) {
            view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, null);
            textTickerName = view.findViewById(android.R.id.text1);
        } else {
            textTickerName = view.findViewById(android.R.id.text1);
        }

        if (current < open) {
            view.setBackgroundColor(Color.RED);
        } else {
            view.setBackgroundColor(Color.GREEN);
        }
        textTickerName.setText(String.format("%s: $%s", symbol, current));
        return view;
    }
}