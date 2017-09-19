package com.international.advert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.baoyz.actionsheet.ActionSheet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.international.advert.model.APHandler;
import com.international.advert.utility.App;
import com.international.advert.utility.Bitmap_utils;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import java.io.File;
import java.util.Date;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostingActivity extends AppCompatActivity {

    ImageView animImage;
    ImageView ivBack;

    ImageView ivChoose;
    EditText etTitle, et_contet;
    Button btnPost;

    private Bitmap imageData;
    private String filePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posting);

        animImage = (ImageView) findViewById(R.id.iv_anim);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(animImage);
        Glide.with(this).load(Utils.waterResource).into(imageViewTarget);

        ivBack = (ImageView)findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        etTitle = findViewById(R.id.et_title);
        et_contet = findViewById(R.id.et_content);
        ivChoose = findViewById(R.id.iv_choose);
        ivChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActionSheet();
            }
        });

        btnPost = findViewById(R.id.btn_post);
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isValidate())
                    post();
            }
        });
    }

    boolean isValidate()
    {
        if (etTitle.getText().toString().isEmpty()){
            Utils.short_Toast(this, "Please input post title.");
            return  false;
        }

        if (etTitle.getText().toString().length() > 100){
            Utils.short_Toast(this, "Maximum 100 letters.");
            return  false;
        }

        if (et_contet.getText().toString().isEmpty()){
            Utils.short_Toast(this, "Please input post content");
            return  false;
        }

        if (et_contet.getText().toString().length() > 1000){
            Utils.short_Toast(this, "Maximum 1000 letters.");
            return  false;
        }

        if (imageData == null){
            Utils.short_Toast(this, "Please choose post image.");
            return false;
        }

        return true;
    }

    void post()
    {
        Utils.showProgressHUD(this);

        File file = new File(filePath);
        RequestBody imageFile = RequestBody.create(MediaType.parse("image/*"), file);

        APHandler.apiInterface.new_poster(imageFile,
                App.appPrefs.getString(Constant.USER_ID, "0"),
                etTitle.getText().toString(),
                et_contet.getText().toString())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Utils.dismissProgressHUD();

                        if (response.isSuccessful()){
                            goBack();
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                        Utils.dismissProgressHUD();
                    }
                });

    }

    //region navigation

    void goBack()
    {
        finish();
        Utils.transferAnimation(this, false);
    }

    //endregion

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
                        PostingActivity.this.setTheme(R.style.AppTheme);
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
                ivChoose.setImageBitmap(imageData);
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
        ivChoose.setImageBitmap(imageData);
    }

    //endregion
}
