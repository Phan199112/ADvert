package com.international.advert;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.bumptech.glide.util.Util;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.international.advert.model.APHandler;
import com.international.advert.model.UserModel;
import com.international.advert.utility.App;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SplashActivity extends AppCompatActivity {

    ImageView animImage;
    ImageView logoImage;

    private FirebaseAuth mAuth;

    private static final int REQUEST_PERMISSIONS = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.NFC,
            Manifest.permission.VIBRATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(PERMISSIONS, REQUEST_PERMISSIONS);
        }

        mAuth = FirebaseAuth.getInstance();

        animImage = (ImageView) findViewById(R.id.iv_anim);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(animImage);
        Glide.with(this).load(Utils.waterResource).into(imageViewTarget);

        logoImage = (ImageView)findViewById(R.id.iv_logo);
        logoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goLoginScreen();
            }
        });

        if (App.appPrefs.getBoolean(Constant.AUTO_LOGIN, false)){
            doLogin();
        }
    }

    void goLoginScreen()
    {
        startActivity(new Intent(this, LoginActivity.class));
        Utils.transferAnimation(this, true);
        finish();
    }

    void goHomeScreen()
    {
        startActivity(new Intent(this, HomeActivity.class));
        Utils.transferAnimation(this, true);
        finish();
    }

    void doLogin()
    {
        Utils.showProgressHUD(this);

        APHandler.apiInterface.login(App.appPrefs.getString(Constant.USER_NAME, "aaa"), App.appPrefs.getString(Constant.USER_PASSWORD, "111111"))
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {

                        if (response.isSuccessful()) {
                            UserModel data = response.body();

                            if (data.getStatus().equals("200")) {

                                firebaseLogin(data.getEmail());
                                return;
                            }
                        }
                        Utils.dismissProgressHUD();
                        Utils.short_Toast(SplashActivity.this, "Login failed. Try again.");
                        App.appPrefs.edit().putBoolean(Constant.AUTO_LOGIN, false).apply();
                        goLoginScreen();
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        Utils.dismissProgressHUD();
                        Utils.short_Toast(SplashActivity.this, "Login failed. Try again.");
                        App.appPrefs.edit().putBoolean(Constant.AUTO_LOGIN, false).apply();
                        goLoginScreen();
                    }
                });
    }

    void firebaseLogin(String email)
    {
        mAuth.signInWithEmailAndPassword(email, "111111")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Utils.dismissProgressHUD();

                        if (task.isSuccessful()){
                            goHomeScreen();
                        } else {
                            Log.d("faild", "failed");
                        }
                    }
                });
    }

    //region permission handle

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case REQUEST_PERMISSIONS:
                if (hasAllPermissions(PERMISSIONS)) {
                    Log.d("cool", "All permissions granted!");
                } else {
                    Toast.makeText(
                            this,
                            "Cannot continue running ADvert without all required permissions.",
                            Toast.LENGTH_SHORT
                    ).show();

                    finish();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    boolean hasAllPermissions(String[] permissions) {
        // Only Marshmallow and higher needs to check permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }

        for (String permission : permissions) {
            final int result = checkSelfPermission(permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

//endregion


}
