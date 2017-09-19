package com.international.advert.model;

/**
 * Created by softm on 16-Sep-17.
 */

public class MessageModel {

    public String type;
    public String message;
    public String timeStamp;
    public String sendername;
    public String sender_avatar;
    public String senderid;

    public MessageModel(){}

    public MessageModel(String type, String message, String timeStamp, String sendername, String sender_avatar, String senderid) {
        this.type = type;
        this.message = message;
        this.timeStamp = timeStamp;
        this.sendername = sendername;
        this.sender_avatar = sender_avatar;
        this.senderid = senderid;
    }
}
