package com.international.advert;

import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.international.advert.model.PostModel;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

public class ForumDetailActivity extends AppCompatActivity {

    ImageView animImage;
    ImageView ivBack;
    Button btnChat;
    ImageView ivAvatar, ivGoods;
    TextView tvName;
    TextView tvTitle;
    TextView tvContent;

    PostModel data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum_detail);

        data = (PostModel) getIntent().getSerializableExtra("data");

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

        btnChat = (Button)findViewById(R.id.btn_chat);
        btnChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goChat();
            }
        });

        ivAvatar = findViewById(R.id.iv_avatar);
        ivGoods = findViewById(R.id.iv_goods);
        tvName = findViewById(R.id.tv_name);
        tvTitle = findViewById(R.id.tv_goods_title);
        tvContent = findViewById(R.id.tv_content);

        showInfor();
    }

    void showInfor()
    {
        tvName.setText(data.getPost_title());
        tvContent.setText(data.getPost_content());
        tvTitle.setText(data.getPost_title());

        Glide.with(this)
                .load(Constant.AVATAR_IMG + data.getPath())
                .asBitmap()
                .centerCrop()
                .into(new BitmapImageViewTarget(ivAvatar){

                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable circularBitmapDrawable =
                                RoundedBitmapDrawableFactory.create(getResources(), resource);
                        circularBitmapDrawable.setCircular(true);
                        ivAvatar.setImageDrawable(circularBitmapDrawable);
                    }
                });

        Glide.with(this)
                .load(Constant.POST_IMG + data.getPost_path())
                .centerCrop()
                .into(ivGoods);
    }

    //region navigation

    void goBack()
    {
        finish();

        Utils.transferAnimation(this, false);
    }

    void goChat()
    {
        Intent intent = new Intent(this, ChatActivity.class);
        startActivity(intent);

        Utils.transferAnimation(this, true);
    }

    //endregion
}
