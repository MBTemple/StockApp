package edu.temple.stockapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

/**
 * A simple {@link Fragment} subclass.
 */
public class PortfolioFragment extends Fragment {


    public static final String FILENAME = "portfolio_file";
    Context parent;
    ListView tickerList;
    TextView addTickerWarning;
    PortfolioAdapter adapter;

    public PortfolioFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.parent = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_portfolio, container, false);

        tickerList = v.findViewById(R.id.tickerList);
        addTickerWarning = v.findViewById(R.id.addTickersWarning);

        final ArrayList<String> stocks = new ArrayList<>();

        try {
            FileInputStream file = parent.openFileInput(FILENAME);
            InputStreamReader reader = new InputStreamReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String data;
            while ((data = bufferedReader.readLine()) != null) {
                stringBuilder.append(data);
            }
            reader.close();
            bufferedReader.close();
            String finalData = stringBuilder.toString();

            if (!finalData.equals("")) {
                String[] arr = finalData.split(",");
                Collections.addAll(stocks, arr);

            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (stocks.size() == 0) {
            addTickerWarning.setText(R.string.emptyPortfolio);
        } else {
            addTickerWarning = null;
            adapter = new PortfolioAdapter(parent, android.R.layout.simple_list_item_1, stocks);
            tickerList.setAdapter(adapter);

            tickerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> context, View view, int position, long id) {

                    String data = stocks.get(position);
                    String[] arr = data.split("\\|");
                    String stockSymbol = arr[0];
                    ((portfolioInterface) parent).selectedStock(stockSymbol);
                }
            });
        }
        return v;
    }

    interface portfolioInterface {
        void selectedStock(String stockSymbol);
    }
}