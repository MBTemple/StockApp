package edu.temple.stockapp;

import android.content.Context;
import android.os.FileObserver;
import android.os.Handler;
import android.os.Message;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

public class PortfolioFileObserver extends FileObserver {

    private static final String FILENAME = "portfolio_file";
    Handler handler;
    Context parent;

    PortfolioFileObserver(String path, Handler handler, Context parent) {
        super(path);
        this.handler = handler;
        this.parent = parent;
    }

    @Override
    public void onEvent(int event, String path) {

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
            Message message = Message.obtain();
            message.obj = dataObtained;
            handler.sendMessage(message);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
