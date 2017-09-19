package com.international.advert.model;

import com.international.advert.utility.Constant;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

/**
 * Created by softm on 15-Sep-17.
 */

public interface APInterface {


    @GET("mobile/login")
    Call<UserModel> login(@Query(Constant.USER_NAME) String username,
                          @Query(Constant.USER_PASSWORD) String passw);


    @Multipart
    @POST("mobile/register")
    Call<UserModel> signup(@Part("file\"; filename=\"avatar.png\"; name=\"avatar\" ") RequestBody file ,
                           @Query(Constant.USER_NAME) String username,
                           @Query(Constant.USER_EMAIL) String email,
                           @Query(Constant.USER_PASSWORD) String passw);


    @Multipart
    @POST("mobile/new_poster")
    Call<ResponseBody> new_poster(@Part("file\"; filename=\"avatar.png\"; name=\"avatar\" ") RequestBody file ,
                                  @Query(Constant.USER_ID) String userid,
                                  @Query(Constant.POSTER_TITLE) String title,
                                  @Query(Constant.POSTER_CONTENT) String content);



    @GET("mobile/get_posts")
    Call<NormalResponseModel> get_posts();

    @GET("mobile/retrieve_update_online_status")
    Call<NormalResponseModel> retrieve_update_online_status(@Query(Constant.ONLINE_SENDERNAME) String senderName,
                                                  @Query(Constant.ONLINE_SENDERID) String senderId,
                                                  @Query(Constant.ONLINE_RECEIVERNAME) String receiverName,
                                                  @Query(Constant.ONLINE_IS_ENTER) String isEnter);

    @GET("mobile/get_friend_profile")
    Call<NormalResponseModel> get_friend_profile(@Query(Constant.USER_ID) String userid);

    @GET("mobile/get_all_user")
    Call<NormalResponseModel> get_all_user(@Query(Constant.USER_ID) String userid);

    @GET("mobile/add_friend")
    Call<NormalResponseModel> add_friend(@Query(Constant.USER_ID) String userId,
                                         @Query("new_friend") String friendId);

    @GET("mobile/update_fcm_token")
    Call<NormalResponseModel> update_fcm_token(@Query(Constant.USER_ID) String userId,
                                         @Query("str_token") String token);

    @GET("mobile/send_push")
    Call<NormalResponseModel> send_push(@Query(Constant.USER_NAME) String username,
                                        @Query(Constant.USER_TOKEN) String token);

}
