package com.international.advert.model;

import java.util.List;

/**
 * Created by softm on 15-Sep-17.
 */

public class NormalResponseModel {

    String status;
    List<PostModel> all_post;

    String receiver_avatar;
    String receiver_id;
    String is_online;
    String token;

    List<UserModel> all_users;
    String my_friends;
    List<UserModel> friend_profile;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<PostModel> getAll_post() {
        return all_post;
    }

    public void setAll_post(List<PostModel> all_post) {
        this.all_post = all_post;
    }

    public String getReceiver_avatar() {
        return receiver_avatar;
    }

    public void setReceiver_avatar(String receiver_avatar) {
        this.receiver_avatar = receiver_avatar;
    }

    public String getReceiver_id() {
        return receiver_id;
    }

    public void setReceiver_id(String receiver_id) {
        this.receiver_id = receiver_id;
    }

    public String getIs_online() {
        return is_online;
    }

    public void setIs_online(String is_online) {
        this.is_online = is_online;
    }

    public List<UserModel> getAll_users() {
        return all_users;
    }

    public void setAll_users(List<UserModel> all_users) {
        this.all_users = all_users;
    }

    public String getMy_friends() {
        return my_friends;
    }

    public void setMy_friends(String my_friends) {
        this.my_friends = my_friends;
    }

    public List<UserModel> getFriend_profile() {
        return friend_profile;
    }

    public void setFriend_profile(List<UserModel> friend_profile) {
        this.friend_profile = friend_profile;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
