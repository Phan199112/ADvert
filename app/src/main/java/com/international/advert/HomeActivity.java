package com.international.advert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.textclassifier.TextClassification;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.firebase.iid.FirebaseInstanceId;
import com.international.advert.model.APHandler;
import com.international.advert.model.NormalResponseModel;
import com.international.advert.utility.App;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    ImageView animImage;
    Button btnForum, btnBLE, btnFriend, btnScan;
    TextView tvLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        animImage = (ImageView) findViewById(R.id.iv_anim);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(animImage);
        Glide.with(this).load(Utils.waterResource).into(imageViewTarget);

        btnForum = (Button)findViewById(R.id.btn_forum);
        btnForum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goForum();
            }
        });

        btnBLE = (Button)findViewById(R.id.btn_ble);
        btnBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBLE();
            }
        });

        btnFriend = (Button)findViewById(R.id.btn_friends);
        btnFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goFriends();
            }
        });

        btnScan = (Button)findViewById(R.id.btn_scan);
        btnScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                gotoScan();
            }
        });

        tvLogout = (TextView)findViewById(R.id.tv_logout);
        tvLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

        sendFCMToken();

        LocalBroadcastManager.getInstance(this).registerReceiver(mHandleMessageReceiver, new IntentFilter("one"));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        LocalBroadcastManager.getInstance(this).unregisterReceiver(mHandleMessageReceiver);
    }

    //region navigation

    void goForum()
    {
        startActivity(new Intent(this, ForumActivity.class));
        Utils.transferAnimation(this, true);
    }

    void goBLE()
    {
        Intent intent = new Intent(this, BleListActivity.class);
        startActivity(intent);

        Utils.transferAnimation(this, true);
    }

    void goFriends()
    {
        Intent intent = new Intent(this, FriendsListActivity.class);
        startActivity(intent);

        Utils.transferAnimation(this, true);
    }

    void logOut()
    {
        startActivity(new Intent(this, LoginActivity.class));

        Utils.transferAnimation(this, false);
    }

    void gotoScan()
    {
        startActivity(new Intent(this, ScanActivity.class));

        Utils.transferAnimation(this, true);
    }

    void sendFCMToken()
    {
        APHandler.apiInterface.update_fcm_token(App.appPrefs.getString(Constant.USER_ID, ""),
                FirebaseInstanceId.getInstance().getToken())
                .enqueue(new Callback<NormalResponseModel>() {
                    @Override
                    public void onResponse(Call<NormalResponseModel> call, Response<NormalResponseModel> response) {

                    }

                    @Override
                    public void onFailure(Call<NormalResponseModel> call, Throwable t) {

                    }
                });
    }

    //endregion

    private BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Utils.short_Toast(getApplicationContext(), intent.getStringExtra("message"));
        }
    };
}
