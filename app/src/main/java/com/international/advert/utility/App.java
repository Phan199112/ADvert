package com.international.advert.utility;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;

import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.internal.RxBleLog;

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

    private RxBleClient rxBleClient;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;
        appPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        displayMetrics = getResources().getDisplayMetrics();
        screenHeight = displayMetrics.heightPixels;
        screenWidth = displayMetrics.widthPixels;

        rxBleClient = RxBleClient.create(this);
        RxBleClient.setLogLevel(RxBleLog.DEBUG);
    }

    public static RxBleClient getRxBleClient(Context context) {
        App application = (App) context.getApplicationContext();
        return application.rxBleClient;
    }
}
