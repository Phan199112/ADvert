package com.international.advert;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AlertDialog;
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

public class AddFriendActivity extends AppCompatActivity {

    ImageView animImage;
    ImageView ivBack;
    ListView lvUsers;
    UserAdapter userAdapter;
    List<UserModel> allUsers = new ArrayList<>();
    List<String> arrRelation = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

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

        lvUsers = findViewById(R.id.lv_users);
        lvUsers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                isValidate(i);
            }
        });

        userAdapter = new UserAdapter();
        lvUsers.setAdapter(userAdapter);

        getAllUsers();

    }

    //region navigation

    void goBack()
    {
        finish();
        Utils.transferAnimation(this, false);
    }

    void gotoChat(int index)
    {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra("data", allUsers.get(index).getUsername());
        startActivity(intent);

        Utils.transferAnimation(this, true);
    }

    //endregion

    //region api handle

    void getAllUsers()
    {
        Utils.showProgressHUD(this);

        APHandler.apiInterface.get_all_user(App.appPrefs.getString(Constant.USER_ID, "0"))
                .enqueue(new Callback<NormalResponseModel>() {
                    @Override
                    public void onResponse(Call<NormalResponseModel> call, Response<NormalResponseModel> response) {

                        Utils.dismissProgressHUD();

                        if (response.isSuccessful()){

                            NormalResponseModel data = response.body();

                            if (data.getStatus().equals("200")){
                                allUsers = data.getAll_users();
                                if (data.getMy_friends() != null && !data.getMy_friends().equals("")){
                                    String[] strReltaion = data.getMy_friends().split(" ");
                                    for ( String cell : strReltaion) {
                                        arrRelation.add(cell);
                                    }

                                }
                            }
                        }

                        userAdapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onFailure(Call<NormalResponseModel> call, Throwable t) {

                        Utils.dismissProgressHUD();
                    }
                });
    }

    void isValidate(final int index)
    {
        String chosenID = allUsers.get(index).getUserid();

        if (arrRelation.contains(chosenID)){
            Utils.short_Toast(this, "Already added in your friend list.");
            return;
        }

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                addFriend(index);
            }
        });
        alertDialogBuilder.setNegativeButton("Discard", null);
        alertDialogBuilder.setCancelable(false);
        alertDialogBuilder.setMessage("Would you add him in your friend list?");

        alertDialogBuilder.create().show();

    }

    void addFriend(int index)
    {
        arrRelation.add(allUsers.get(index).getUserid());

        Utils.showProgressHUD(this);

        APHandler.apiInterface.add_friend(App.appPrefs.getString(Constant.USER_ID, "0"),
                allUsers.get(index).getUserid())
                .enqueue(new Callback<NormalResponseModel>() {
                    @Override
                    public void onResponse(Call<NormalResponseModel> call, Response<NormalResponseModel> response) {

                        Utils.dismissProgressHUD();
                        Utils.short_Toast(AddFriendActivity.this, "successfully added.");

                    }

                    @Override
                    public void onFailure(Call<NormalResponseModel> call, Throwable t) {
                        Utils.dismissProgressHUD();
                    }
                });
    }

    //endregion


    class UserAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return allUsers.size();
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
                view = inflater.inflate(R.layout.cell_friend, null);
            }

            TextView tvName = view.findViewById(R.id.tv_name);
            final ImageView ivAvatar = view.findViewById(R.id.iv_avatar);

            tvName.setText(allUsers.get(i).getUsername());

            TextView tvChat = view.findViewById(R.id.btn_chat);
            tvChat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    gotoChat(i);
                }
            });

            Glide.with(AddFriendActivity.this)
                    .load(Constant.AVATAR_IMG + allUsers.get(i).getPath())
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
