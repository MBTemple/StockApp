package edu.temple.stockapp;

import android.app.Dialog;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity implements PortfolioFragment.portfolioInterface {

    public static final String FILENAME = "portfolio_file";
    final String API = "http://dev.markitondemand.com/MODApis/Api/v2/Quote/json/?symbol=";
    boolean singlePane;
    DetailsFragment detailsVar;
    File portfolio_file;
    FloatingActionButton fab;
    FragmentManager fragManager;
    PortfolioFileObserver observer;
    RequestQueue queue;
    TextView addTickersWarning;
    Thread thread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fab = findViewById(R.id.fab);

        singlePane = findViewById(R.id.container2) == null;
        addTickersWarning = findViewById(R.id.addTickersWarning);

        queue = Volley.newRequestQueue(MainActivity.this);

        fragManager = getSupportFragmentManager();
        fragManager.beginTransaction()
                .replace(R.id.container1, new PortfolioFragment())
                .commit();
        if (!singlePane) {
            detailsVar = new DetailsFragment();
            fragManager.beginTransaction()
                    .replace(R.id.container2, detailsVar)
                    .commit();
        }

        portfolio_file = new File(getFilesDir(), FILENAME);
        if (!portfolio_file.exists()) {
            try {
                portfolio_file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        final Handler obsHandler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                String dataCollected = (String) msg.obj;
                String[] nameSplit = dataCollected.split(",");

                fragManager.beginTransaction()
                        .replace(R.id.container1, new PortfolioFragment())
                        .commit();

                if (!dataCollected.equals("")) {
                    if (singlePane) {
                        boolean detailsBool = findViewById(R.id.tickerList) == null;
                        if (detailsBool) {
                            TextView name = findViewById(R.id.companyName);
                            TextView current = findViewById(R.id.currentPrice);
                            TextView open = findViewById(R.id.openingPrice);

                            String company = name.getText().toString();
                            for (String nameSplitA : nameSplit) {
                                String[] arr = nameSplitA.split("\\|");
                                if (company.equals(arr[1])) {
                                    current.setText(String.format("Current Price: $%s", arr[2]));
                                    open.setText(String.format("Opening Price: $%s", arr[3]));
                                    break;
                                }
                            }
                        } else {
                            ListView listView = findViewById(R.id.tickerList);
                            ArrayList<String> stockArrList = new ArrayList<>();
                            Collections.addAll(stockArrList, nameSplit);
                            listView.setAdapter(new PortfolioAdapter(MainActivity.this,
                                    android.R.layout.simple_list_item_1, stockArrList));
                        }
                    } else {
                        ListView listView = findViewById(R.id.tickerList);
                        ArrayList<String> stockArrList = new ArrayList<>();
                        Collections.addAll(stockArrList, nameSplit);
                        listView.setAdapter(new PortfolioAdapter(MainActivity.this,
                                android.R.layout.simple_list_item_1, stockArrList));

                        TextView name = findViewById(R.id.companyName);
                        TextView current = findViewById(R.id.currentPrice);
                        TextView open = findViewById(R.id.openingPrice);

                        String company = name.getText().toString();
                        if (!company.equals(null)) {
                            for (String nameSplitA : nameSplit) {
                                String[] arr = nameSplitA.split("\\|");
                                if (company.equals(arr[1])) {
                                    current.setText(String.format("Current Price: $%s", arr[2]));
                                    open.setText(String.format("Opening Price: $%s", arr[3]));
                                    break;
                                }
                            }
                        }
                    }
                }
                return false;
            }
        });

        observer = new PortfolioFileObserver(portfolio_file.getPath(),
                obsHandler, MainActivity.this);

        thread = new Thread() {
            @Override
            public void run() {
                fab.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        final Dialog dialog = new Dialog(MainActivity.this);
                        dialog.setContentView(R.layout.fab_dialog);
                        dialog.show();
                        Button cancel = dialog.findViewById(R.id.cancel);
                        Button add = dialog.findViewById(R.id.add);
                        final EditText text = dialog.findViewById(R.id.addTickerText);

                        cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                dialog.hide();
                            }
                        });

                        add.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                final String symbol = text.getText().toString();

                                boolean duplicate = false;

                                String finalData = getPortfolioData();
                                final ArrayList<String> stocks = new ArrayList<>();

                                if (!finalData.equals("")) {
                                    String[] arr = finalData.split(",");

                                    Collections.addAll(stocks, arr);
                                    for (int i = 0; i < stocks.size(); i++) {
                                        String[] tickerData = stocks.get(i).split("\\|");
                                        if (tickerData[0].toUpperCase().equals(symbol.toUpperCase())) {
                                            duplicate = true;
                                        }
                                    }
                                }
                                if (!duplicate) {
                                    String url = API + symbol;
                                    observer.startWatching();
                                    JsonObjectRequest jsonObjectRequest =
                                            new JsonObjectRequest(Request.Method.GET, url,
                                                    null, new Response.Listener<JSONObject>() {

                                                @Override
                                                public void onResponse(JSONObject response) {
                                                    try {
                                                        if (!response.getString("Name").isEmpty()) {
                                                            String stockInfo = symbol
                                                                    + "|"
                                                                    + response.getString("Name")
                                                                    + "|"
                                                                    + response.getDouble("LastPrice")
                                                                    + "|"
                                                                    + response.getDouble("Open")
                                                                    + ",";
                                                            try {

                                                                FileOutputStream stream =
                                                                        new FileOutputStream(portfolio_file,
                                                                                true);
                                                                OutputStreamWriter writer =
                                                                        new OutputStreamWriter(stream);
                                                                writer.append(stockInfo);
                                                                writer.flush();
                                                                observer.stopWatching();
                                                            } catch (IOException e) {
                                                                e.printStackTrace();
                                                            }
                                                            dialog.hide();
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                    try {
                                                        if (!response.getString("Message").isEmpty()) {
                                                            Toast.makeText(MainActivity.this,
                                                                    R.string.noSymbol, Toast.LENGTH_LONG).show();
                                                        }
                                                    } catch (JSONException e) {
                                                        e.printStackTrace();
                                                    }
                                                }
                                            }, new Response.ErrorListener() {

                                                @Override
                                                public void onErrorResponse(VolleyError error) {
                                                }
                                            });

                                    queue.add(jsonObjectRequest);
                                } else {
                                    Toast.makeText(MainActivity.this,
                                            R.string.stockExists, Toast.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                });
            }
        };
        thread.start();
    }

    @Override
    public void selectedStock(String stockSymbol) {
        if (singlePane) {
            DetailsFragment detailsInstance = DetailsFragment.newInstance(stockSymbol);
            fragManager.beginTransaction()
                    .replace(R.id.container1, detailsInstance)
                    .addToBackStack(null)
                    .commit();
        } else {
            DetailsFragment detailsInstance = DetailsFragment.newInstance(stockSymbol);
            fragManager.beginTransaction()
                    .replace(R.id.container2, detailsInstance)
                    .addToBackStack(null)
                    .commit();
            detailsVar.changeStock(stockSymbol);
        }
    }

    @Override
    protected void onDestroy() {
        thread.interrupt();
        super.onDestroy();
    }

    public String getPortfolioData() {
        String string = "";

        try {
            FileInputStream file = openFileInput(FILENAME);
            InputStreamReader reader = new InputStreamReader(file);
            BufferedReader bufferedReader = new BufferedReader(reader);
            StringBuilder stringBuilder = new StringBuilder();
            String data;
            while ((data = bufferedReader.readLine()) != null) {
                stringBuilder.append(data);
            }
            reader.close();
            bufferedReader.close();
            string = stringBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return string;
    }
}