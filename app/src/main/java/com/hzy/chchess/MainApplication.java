package com.hzy.chchess;

import android.app.Application;

import com.hzy.chchess.xqwlight.Position;

import java.io.IOException;


public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        loadGameBook();
    }

    private void loadGameBook() {
        try {
            Position.loadBook(getAssets().open(Configs.ASSETS_BOOK_PATH));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
