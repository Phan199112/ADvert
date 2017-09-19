package com.international.advert;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.international.advert.model.APHandler;
import com.international.advert.model.NormalResponseModel;
import com.international.advert.model.PostModel;
import com.international.advert.utility.App;
import com.international.advert.utility.CircleImageView;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForumActivity extends AppCompatActivity {

    ImageView animImage;
    ImageView ivBack;
    ListView lvPost;
    ImageView ivAdd;
    TextView tvTemp;

    ForumAdapter forumAdapter;
    List<PostModel> arrPosts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forum);

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

        lvPost = (ListView)findViewById(R.id.lv_post);

        arrPosts = new ArrayList<>();

        forumAdapter = new ForumAdapter();
        lvPost.setAdapter(forumAdapter);
        lvPost.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                goForumDetail(i);
            }
        });

        tvTemp = findViewById(R.id.tv_temp);

        ivAdd = (ImageView)findViewById(R.id.iv_add);
        ivAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goAdd();
            }
        });


    }

    @Override
    protected void onStart() {
        super.onStart();

        downloadPostData();
    }

    void downloadPostData()
    {
        Utils.showProgressHUD(this);

        arrPosts = new ArrayList<>();

        APHandler.apiInterface.get_posts()
                .enqueue(new Callback<NormalResponseModel>() {
                    @Override
                    public void onResponse(Call<NormalResponseModel> call, Response<NormalResponseModel> response) {
                        Utils.dismissProgressHUD();

                        if (response.isSuccessful()){
                           NormalResponseModel data = response.body();

                            if (data.getStatus().equals("200")){
                                arrPosts = data.getAll_post();
                            }
                        }

                        refreshTable();
                    }

                    @Override
                    public void onFailure(Call<NormalResponseModel> call, Throwable t) {
                        Utils.dismissProgressHUD();
                        refreshTable();
                    }
                });
    }

    void refreshTable()
    {
        if (arrPosts.size() == 0)
            tvTemp.setVisibility(View.VISIBLE);
        else
            tvTemp.setVisibility(View.GONE);

        forumAdapter.notifyDataSetChanged();
    }

    //region navigation

    void goBack()
    {
        finish();
        Utils.transferAnimation(this, false);
    }

    void goForumDetail(int index)
    {
        Intent intent = new Intent(this, ForumDetailActivity.class);
        intent.putExtra("data", arrPosts.get(index));
        startActivity(intent);

        Utils.transferAnimation(this, true);
    }

    void goChat(int index)
    {
        if (arrPosts.get(index).getUsername().equals(App.appPrefs.getString(Constant.USER_NAME, "")))
            return;

        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("data", arrPosts.get(index).getUsername());
        startActivity(intent);

        Utils.transferAnimation(this, true);
    }

    void goAdd()
    {
        startActivity(new Intent(this, PostingActivity.class));

        Utils.transferAnimation(this, true);
    }

    //endregion

    class ForumAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return arrPosts.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(final int i, View view, ViewGroup viewGroup) {

            if (view == null){
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.cell_forum, null);
            }

            ImageView ivPost = view.findViewById(R.id.iv_goods);
            final ImageView civAvatar = view.findViewById(R.id.civ_avatar);
            TextView tvUsername = view.findViewById(R.id.tv_name);

            TextView tvChat = (TextView)view.findViewById(R.id.btn_chat);
            tvChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    goChat(i);
                }
            });

            tvUsername.setText(arrPosts.get(i).getUsername());

            Glide.with(ForumActivity.this)
                    .load(Constant.AVATAR_IMG + arrPosts.get(i).getPath())
                    .asBitmap()
                    .centerCrop()
                    .into(new BitmapImageViewTarget(civAvatar){

                        @Override
                        protected void setResource(Bitmap resource) {
                            RoundedBitmapDrawable circularBitmapDrawable =
                                    RoundedBitmapDrawableFactory.create(getResources(), resource);
                            circularBitmapDrawable.setCircular(true);
                            civAvatar.setImageDrawable(circularBitmapDrawable);
                        }
                    });

            Glide.with(ForumActivity.this)
                    .load(Constant.POST_IMG + arrPosts.get(i).getPost_path())
                    .centerCrop()
                    .into(ivPost);


            return view;
        }
    }
}
