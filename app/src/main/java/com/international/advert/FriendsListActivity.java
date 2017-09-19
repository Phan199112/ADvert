package com.international.advert;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import com.international.advert.model.UserModel;
import com.international.advert.utility.App;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsListActivity extends AppCompatActivity {

    ImageView animImage;
    ImageView ivBack;
    FriendsAdapter friendsAdapter;
    ListView lvFriends;
    ImageView ivPlus;
    TextView tvTemper;

    List<UserModel> myFriends = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);

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

        ivPlus = findViewById(R.id.iv_add);
        ivPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goAddFriend();
            }
        });

        tvTemper = findViewById(R.id.tv_temper);

        lvFriends = (ListView)findViewById(R.id.lv_friends);
        friendsAdapter = new FriendsAdapter();
        lvFriends.setAdapter(friendsAdapter);

        if (myFriends.size() == 0){
            tvTemper.setVisibility(View.VISIBLE);
        } else {
            tvTemper.setVisibility(View.GONE);
        }

        lvFriends.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                goChat(i);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        getMyFriends();
    }

    void getMyFriends()
    {
        Utils.showProgressHUD(this);

        APHandler.apiInterface.get_friend_profile(App.appPrefs.getString(Constant.USER_ID, "0"))
                .enqueue(new Callback<NormalResponseModel>() {
                    @Override
                    public void onResponse(Call<NormalResponseModel> call, Response<NormalResponseModel> response) {
                        Utils.dismissProgressHUD();

                        if (response.isSuccessful()){

                            NormalResponseModel data = response.body();
                            if (data.getStatus().equals("200")){
                                myFriends = data.getFriend_profile();
                                friendsAdapter.notifyDataSetChanged();

                                if (myFriends.size() == 0){
                                    tvTemper.setVisibility(View.VISIBLE);
                                } else {
                                    tvTemper.setVisibility(View.GONE);
                                }
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<NormalResponseModel> call, Throwable t) {
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

    void goChat(int index)
    {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("data", myFriends.get(index).getUsername());
        startActivity(intent);

        Utils.transferAnimation(this, true);
    }

    void goAddFriend()
    {
        Intent intent = new Intent(this, AddFriendActivity.class);
        startActivity(intent);

        Utils.transferAnimation(this, true);
    }

    //endregion

    class FriendsAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return myFriends.size();
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
        public View getView(int i, View view, ViewGroup viewGroup) {

            if (view == null){
                LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.cell_friend, null);
            }

            TextView tvName = view.findViewById(R.id.tv_name);
            final ImageView ivAvatar = view.findViewById(R.id.iv_avatar);

            tvName.setText(myFriends.get(i).getUsername());
            Glide.with(FriendsListActivity.this)
                    .load(Constant.AVATAR_IMG + myFriends.get(i).getPath())
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

            return view;
        }
    }
}
