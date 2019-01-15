package edu.temple.stockapp;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
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
public class DetailsFragment extends Fragment {

    public static final String FILENAME = "portfolio_file";
    public static final String SYMBOL = "Symbol";

    final ArrayList<String> stocks = new ArrayList<>();

    View fragView;
    WebView webviewChart;
    TextView companyName;
    TextView currentPrice;
    TextView openingPrice;
    Context parent;

    public DetailsFragment() {
        // Required empty public constructor
    }

    public static DetailsFragment newInstance(String stockSymbol) {
        DetailsFragment detailsFragment = new DetailsFragment();
        Bundle args = new Bundle();
        args.putString(SYMBOL, stockSymbol);
        detailsFragment.setArguments(args);
        return detailsFragment;
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        this.parent = context;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_details, container, false);
        fragView = view;
        companyName = view.findViewById(R.id.companyName);
        currentPrice = view.findViewById(R.id.currentPrice);
        openingPrice = view.findViewById(R.id.openingPrice);
        webviewChart = view.findViewById(R.id.webviewChart);
        webviewChart.getSettings().setJavaScriptEnabled(true);

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
            String dataObtained = stringBuilder.toString();

            if (!dataObtained.equals("")) {
                String[] arr = dataObtained.split(",");
                Collections.addAll(stocks, arr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (getArguments() != null) {
            String symbol = getArguments().getString(SYMBOL);

            for (int i = 0; i < stocks.size(); i++) {
                String[] tickerData1 = stocks.get(i).split("\\|");
                if (tickerData1[0].equals(symbol)) {
                    companyName.setText(String.format("Company Name: %s", tickerData1[1]));
                    currentPrice.setText(String.format("Current Price: $%s", tickerData1[2]));
                    openingPrice.setText(String.format("Opening Price: $%s", tickerData1[3]));
                    webviewChart.loadUrl("https://macc.io/lab/cis3515/?symbol=" + symbol);
                    break;
                }
            }
        } else {
            return null;
        }
        return view;
    }

    public void changeStock(String symbol) {

        for (int j = 0; j < stocks.size(); j++) {
            String[] tickerData2 = stocks.get(j).split("\\|");
            if (tickerData2[0].equals(symbol)) {
                companyName.setText(String.format("Company Name: %s", tickerData2[1]));
                currentPrice.setText(String.format("Current Price: $%s", tickerData2[2]));
                openingPrice.setText(String.format("Opening Price: $%s", tickerData2[3]));
                webviewChart.loadUrl("https://macc.io/lab/cis3515/?symbol=" + symbol);
                break;
            }
        }
    }
}
