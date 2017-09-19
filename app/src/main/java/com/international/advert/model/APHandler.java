package com.international.advert.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.international.advert.utility.Constant;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by softm on 15-Sep-17.
 */

public class APHandler {

    private static Gson gson = new GsonBuilder()
            .setLenient()
            .create();

    private static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(Constant.SERVER_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();

    public static APInterface apiInterface = retrofit.create(APInterface.class);
}
