package com.international.advert.utility;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

/**
 * Created by softm on 15-Sep-17.
 */

public class App extends Application {

    private DisplayMetrics displayMetrics;

    public static App app;
    public static SharedPreferences appPrefs;
    public static int screenWidth;
    public static int screenHeight;
    public static final String FOLDER_NAME = "/ADvert/";

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        displayMetrics = getResources().getDisplayMetrics();
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;
    }
}
