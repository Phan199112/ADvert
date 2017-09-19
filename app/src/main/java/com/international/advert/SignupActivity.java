package com.international.advert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.baoyz.actionsheet.ActionSheet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.international.advert.model.APHandler;
import com.international.advert.model.UserModel;
import com.international.advert.utility.App;
import com.international.advert.utility.Bitmap_utils;
import com.international.advert.utility.CircleImageView;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import java.io.File;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupActivity extends AppCompatActivity {

    ImageView animImage;
    LinearLayout llSignin;
    Button btnSignup;
    CircleImageView civAvatar;
    EditText etUsername, etEmail, etPass;


    private Bitmap imageData;
    private String filePath = "";
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        animImage = (ImageView) findViewById(R.id.iv_anim);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(animImage);
        Glide.with(this).load(Utils.waterResource).into(imageViewTarget);

        llSignin = (LinearLayout)findViewById(R.id.ll_signin);
        llSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                finish();
                Utils.transferAnimation(SignupActivity.this, false);
            }
        });

        btnSignup = (Button)findViewById(R.id.btn_register);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidate())
                    signup();
            }
        });

        civAvatar = (CircleImageView)findViewById(R.id.civ_avatar);
        civAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    showActionSheet();
            }
        });

        etEmail = findViewById(R.id.et_email);
        etUsername = findViewById(R.id.et_username);
        etPass = findViewById(R.id.et_pass);

        mAuth = FirebaseAuth.getInstance();
    }

    void goTohomePage()
    {
        startActivity(new Intent(this, HomeActivity.class));

        Utils.transferAnimation(this, true);
    }

    boolean isValidate()
    {
        if (etEmail.getText().toString().isEmpty()){
            Utils.short_Toast(this, "Please type email address.");
            return  false;
        }

        if (!Utils.isValidateEmail(etEmail.getText().toString())){
            Utils.short_Toast(this, "Invalid email format.");
            return false;
        }

        if (etUsername.getText().toString().isEmpty()){
            Utils.short_Toast(this, "Please input username.");
            return false;
        }

        if (etPass.getText().toString().isEmpty()){
            Utils.short_Toast(this, "Please input password");
            return false;
        }

        if (etPass.getText().toString().length() < 5){
            Utils.short_Toast(this, "Please set minimum 6 letters for password.");
        }

        if (imageData == null){
            Utils.short_Toast(this, "Please choose avatar.");
            return false;
        }

        return true;
    }

    void signup()
    {
        Utils.showProgressHUD(this);

        File file = new File(filePath);
        RequestBody imageFile = RequestBody.create(MediaType.parse("image/*"), file);

        APHandler.apiInterface.signup(imageFile,
                etUsername.getText().toString(),
                etEmail.getText().toString(),
                etPass.getText().toString())
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

                                firebaseSignup();

                            } else if (data.getStatus().equals("400")){
                                Utils.dismissProgressHUD();
                                Utils.short_Toast(SignupActivity.this, "Image upload failed. Try again.");
                            } else if (data.getStatus().equals("401")){
                                Utils.dismissProgressHUD();
                                Utils.short_Toast(SignupActivity.this, "Email address was duplicated. Choose another one.");
                            } else if (data.getStatus().equals("402")){
                                Utils.dismissProgressHUD();
                                Utils.short_Toast(SignupActivity.this, "Username was duplicated. Choose another one.");
                            }

                        } else {
                            Utils.dismissProgressHUD();
                            Utils.short_Toast(SignupActivity.this, "Upload Failed.");
                        }


                    }

                    @Override
                    public void onFailure(Call<UserModel> call, Throwable t) {

                        Utils.dismissProgressHUD();
                    }
                });
    }

    void firebaseSignup()
    {
        mAuth.createUserWithEmailAndPassword(etEmail.getText().toString(), "111111")
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Utils.dismissProgressHUD();

                        if (task.isSuccessful()){
                            changeUserinfo();
                        } else {
                            Log.d("faild", "failed");
                        }
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.dismissProgressHUD();
                Log.d("faild", "failed");
            }
        });
    }

    void changeUserinfo()
    {
//        FirebaseUser user = mAuth.getCurrentUser();
//
//        user.getUid();
        goTohomePage();
    }

    //region image processing

    void showActionSheet()
    {
        setTheme(R.style.ActionSheetStyleiOS7);
        ActionSheet.createBuilder(this, getSupportFragmentManager())
                .setOtherButtonTitles("Take Picture", "From Gallery")
                .setCancelableOnTouchOutside(true)
                .setCancelButtonTitle("Cancel")
                .setListener(new ActionSheet.ActionSheetListener() {
                    @Override
                    public void onDismiss(ActionSheet actionSheet, boolean isCancel) {
                        SignupActivity.this.setTheme(R.style.AppTheme);
                    }

                    @Override
                    public void onOtherButtonClick(ActionSheet actionSheet, int index) {
                        if (index == 0){
                            getPictureFromCamera();
                        } else {
                            getPictureFromGallery();
                        }

                    }
                }).show();
    }

    private void getPictureFromGallery()
    {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, Constant.FROM_GALLERY);
    }

    private void getPictureFromCamera()
    {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        Date curDate = new Date();
        String date = Long.toString(curDate.getTime());
        String uri = Environment.getExternalStorageDirectory().getPath() + App.FOLDER_NAME + date + ".jpg";
        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(uri)));
        App.appPrefs.edit().putString(Constant.CAMERA_URI, uri).apply();

        if (takePictureIntent.resolveActivity(getPackageManager()) != null)
            startActivityForResult(takePictureIntent, Constant.FROM_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        File savePath = new File(Environment.getExternalStorageDirectory().getPath() + App.FOLDER_NAME);
        if (!savePath.exists())
            savePath.mkdirs();

        if (data == null) {
            if (resultCode == AppCompatActivity.RESULT_OK && requestCode == Constant.FROM_CAMERA) {

                String path = App.appPrefs.getString(Constant.CAMERA_URI, "");

                imageData = Bitmap_utils.getImageFromOrientation(path, App.screenWidth);
                if (imageData == null)
                    return;

                filePath = path;
                imageData = Bitmap_utils.squareBitmap(imageData);
                civAvatar.setImageBitmap(imageData);
            }

            return;
        }

        switch (requestCode){

            case Constant.FROM_GALLERY:

                imageData = Bitmap_utils.getImageFromOrientation(Bitmap_utils.getPathFromGallery(this, data), App.screenWidth);
                if (imageData == null)
                    return;

                imageData = Bitmap_utils.squareBitmap(imageData);
                filePath = Bitmap_utils.getPathFromGallery(this, data);
                break;
            case Constant.FROM_CAMERA:

                String path = Bitmap_utils.getRealPathFromURI(this, data.getData());

                imageData = Bitmap_utils.getImageFromOrientation(path, App.screenWidth);
                if (imageData == null)
                    return;

                imageData = Bitmap_utils.squareBitmap(imageData);
                filePath = path;
                break;
        }
        civAvatar.setImageBitmap(imageData);
    }

    //endregion
}
