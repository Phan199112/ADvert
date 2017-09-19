package com.international.advert;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.international.advert.model.APHandler;
import com.international.advert.model.NormalResponseModel;
import com.international.advert.model.UserModel;
import com.international.advert.utility.App;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    ImageView animImage;
    LinearLayout llSignup;
    Button btnLogin;

    EditText etUsername;
    EditText etPass;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        animImage = (ImageView) findViewById(R.id.iv_anim);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(animImage);
        Glide.with(this).load(Utils.waterResource).into(imageViewTarget);

        llSignup = (LinearLayout)findViewById(R.id.ll_signup);
        llSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                goToRegister();
            }
        });

        btnLogin = (Button)findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isValidate())
                    doLogin();
            }
        });

        etUsername = (EditText)findViewById(R.id.et_username);
        etPass = findViewById(R.id.et_pass);

        mAuth = FirebaseAuth.getInstance();
    }

    void goToRegister()
    {
        startActivity(new Intent(this, SignupActivity.class));

        Utils.transferAnimation(this, true);
    }

    void goTohomePage()
    {
        startActivity(new Intent(this, HomeActivity.class));

        Utils.transferAnimation(this, true);
    }

    boolean isValidate()
    {
        if (etUsername.getText().toString().isEmpty()){
            Utils.short_Toast(this, "Please type email address.");
            return  false;
        }

        if (etPass.getText().toString().isEmpty()){
            Utils.short_Toast(this, "Please input password");
            return false;
        }

        return true;
    }

    void doLogin()
    {
        Utils.showProgressHUD(this);

        APHandler.apiInterface.login(etUsername.getText().toString(), etPass.getText().toString())
                .enqueue(new Callback<UserModel>() {
                    @Override
                    public void onResponse(Call<UserModel> call, Response<UserModel> response) {

                        if (response.isSuccessful()){
                            UserModel data = response.body();

                            if (data.getStatus().equals("200")){
                                App.appPrefs.edit().putBoolean(Constant.AUTO_LOGIN, true)
                                        .putString(Constant.USER_ID, data.getUserid())
                                        .putString(Constant.USER_EMAIL, data.getEmail())
                                        .putString(Constant.USER_PASSWORD, data.getPassword())
                                        .putString(Constant.USER_NAME, data.getUsername())
                                        .putString(Constant.USER_PATH, data.getPath())
                                        .apply();

                                firebaseLogin(data.getEmail());
                            } else {
                                Utils.dismissProgressHUD();
                                Utils.short_Toast(LoginActivity.this, "Login failed. Try again.");

                            }

                        } else {
                            Utils.dismissProgressHUD();
                            Utils.short_Toast(LoginActivity.this, "Connection Error");
                        }
                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {
                        Utils.dismissProgressHUD();
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
                            goTohomePage();
                        } else {
                            Log.d("faild", "failed");
                        }
                    }
                });
    }
}
