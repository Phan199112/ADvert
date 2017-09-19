package com.international.advert;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.baoyz.actionsheet.ActionSheet;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.international.advert.model.APHandler;
import com.international.advert.model.MessageModel;
import com.international.advert.model.NormalResponseModel;
import com.international.advert.model.UserModel;
import com.international.advert.utility.App;
import com.international.advert.utility.Bitmap_utils;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    ImageView animImage;
    ImageView ivBack, ivAvatar;
    ImageView ivCamera;
    Button btnSend;
    TextView tvUsername;
    ListView lvMsg;
    EditText etMsgBox;

    private String receiverUsername;

    private String receiverID;
    private String receiverAvatar;
    private String receiverOnline;
    private String receiverToken;

    private Bitmap imageData;
    private String filePath = "";

    List<MessageModel> arrMsgs = new ArrayList<>();
    MessageAdapter messageAdapter;

    TimerTask mTimerTask;
    final Handler handler = new Handler();
    Timer t = new Timer();

    FirebaseStorage storage = FirebaseStorage.getInstance();
    DatabaseReference mDatabaseForMe;
    DatabaseReference mDatabaseForReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        receiverUsername = getIntent().getStringExtra("data");

        mDatabaseForMe = FirebaseDatabase.getInstance().getReference("chat/" + App.appPrefs.getString(Constant.USER_NAME, "a") + "_" + receiverUsername);
        mDatabaseForReceiver = FirebaseDatabase.getInstance().getReference("chat/" + receiverUsername + "_" + App.appPrefs.getString(Constant.USER_NAME, "a"));


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

        ivAvatar = findViewById(R.id.iv_avatar);
        ivCamera = findViewById(R.id.iv_camera);
        btnSend = findViewById(R.id.btn_send);
        etMsgBox = findViewById(R.id.et_message);
        tvUsername = findViewById(R.id.tv_username);
        lvMsg = findViewById(R.id.lv_ble);


        lvMsg.setStackFromBottom(true);
        lvMsg.setTranscriptMode(AbsListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
        messageAdapter = new MessageAdapter();
        lvMsg.setAdapter(messageAdapter);

        ivCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showActionSheet();
            }
        });

        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isValidate())
                    sendMessage(false, etMsgBox.getText().toString());
            }
        });

        tvUsername.setText(receiverUsername);

        loadReceiverInfoAndCheckOnline("yes", true);
        doTimerTask();

        mDatabaseForMe.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                MessageModel data = dataSnapshot.getValue(MessageModel.class);
                arrMsgs.add(data);
                messageAdapter.notifyDataSetChanged();
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopTask();
        loadReceiverInfoAndCheckOnline("no", false);
    }

    void refreshUI()
    {
        Glide.with(this)
                .load(Constant.AVATAR_IMG + receiverAvatar)
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
    }

    //region navigation

    void goBack()
    {
        finish();
        Utils.transferAnimation(this, false);
    }

    //endregion

    //region send image, message

    void uploadImage()
    {
        Utils.showProgressHUD(this);

        Uri file = Uri.fromFile(new File(filePath));
        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child("images/"+ App.appPrefs.getString(Constant.USER_NAME, "a")+ file.getLastPathSegment());

        UploadTask uploadTask = imgRef.putFile(file);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Utils.dismissProgressHUD();
                Utils.short_Toast(ChatActivity.this, "Failed, Try again.");

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                Utils.dismissProgressHUD();
                sendMessage(true, taskSnapshot.getDownloadUrl().toString());
            }
        });


    }

    boolean isValidate()
    {

        if (etMsgBox.getText().toString().isEmpty())
            return false;

        return true;
    }

    void sendMessage(boolean isImage, String msg)
    {
        Map<String, String> map = new HashMap<String, String>();

        Long tsLong = System.currentTimeMillis()/1000;
        String ts = tsLong.toString();

        map.put("type", isImage? "2" : "1");
        map.put("message", msg);
        map.put("timeStamp", ts);
        map.put("sendername", App.appPrefs.getString(Constant.USER_NAME, "a"));
        map.put("sender_avatar", App.appPrefs.getString(Constant.USER_PATH, "a"));
        map.put("senderid", App.appPrefs.getString(Constant.USER_ID, "0"));

        mDatabaseForMe.push().setValue(map);
        mDatabaseForReceiver.push().setValue(map);

        etMsgBox.setText("");

        if (!receiverOnline.equals("yes"))
            sendPush();

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    void sendPush()
    {
        APHandler.apiInterface.send_push(App.appPrefs.getString(Constant.USER_NAME, ""),
                receiverToken)
                .enqueue(new Callback<NormalResponseModel>() {
                    @Override
                    public void onResponse(Call<NormalResponseModel> call, Response<NormalResponseModel> response) {
                        Log.d("sent", "push sent");
                    }

                    @Override
                    public void onFailure(Call<NormalResponseModel> call, Throwable t) {

                    }
                });
    }

    //endregion

    //region gita

    void loadReceiverInfoAndCheckOnline(final String isEnter, final boolean isFirst)
    {
        APHandler.apiInterface.retrieve_update_online_status(App.appPrefs.getString(Constant.USER_NAME, "a"),
                App.appPrefs.getString(Constant.USER_ID, "0"),
                receiverUsername,
                isEnter)
                .enqueue(new Callback<NormalResponseModel>() {
                    @Override
                    public void onResponse(Call<NormalResponseModel> call, Response<NormalResponseModel> response) {
                        if (response.isSuccessful()) {

                            NormalResponseModel data = response.body();

                            if (isEnter.equals("yes")){

                                receiverAvatar = data.getReceiver_avatar();
                                receiverOnline = data.getIs_online();
                                receiverID = data.getReceiver_id();
                                receiverToken = data.getToken();

                                if (isFirst){
                                    refreshUI();
                                }

                                return;
                            }
                        }

                        receiverOnline = "no";
                    }

                    @Override
                    public void onFailure(Call<NormalResponseModel> call, Throwable t) {
                        receiverOnline = "no";
                    }
                });
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
                        ChatActivity.this.setTheme(R.style.AppTheme);
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
        super.onActivityResult(requestCode, resultCode, data);File savePath = new File(Environment.getExternalStorageDirectory().getPath() + App.FOLDER_NAME);
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

                uploadImage();
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

        uploadImage();
    }

    //endregion

    //region timer api

    public void doTimerTask(){

        mTimerTask = new TimerTask() {
            public void run() {
                handler.post(new Runnable() {
                    public void run() {

                        loadReceiverInfoAndCheckOnline("yes", false);

                        Log.d("TIMER", "TimerTask run");
                    }
                });
            }};


        t.schedule(mTimerTask, 5000, 5000);  //

    }

    public void stopTask(){

        if(mTimerTask!=null){

            Log.d("TIMER", "timer canceled");
            mTimerTask.cancel();
        }

    }

    //endregion


    class MessageAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return arrMsgs.size();
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

            MessageModel data = arrMsgs.get(i);

            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            if (data.sendername.equals(App.appPrefs.getString(Constant.USER_NAME, "a"))){
                view = layoutInflater.inflate(R.layout.cell_msg_me, null);
            } else {
                view = layoutInflater.inflate(R.layout.cell_msg_sender, null);
            }

            TextView tvMessage = view.findViewById(R.id.tv_message);
            TextView tvTime = view.findViewById(R.id.tv_time);
            ImageView ivContent = view.findViewById(R.id.iv_photo);

            if (data.type.equals("1")){

                tvMessage.setVisibility(View.VISIBLE);
                ivContent.setVisibility(View.GONE);

                tvMessage.setText(data.message);

            } else {

                tvMessage.setVisibility(View.GONE);
                ivContent.setVisibility(View.VISIBLE);

                Glide.with(ChatActivity.this)
                        .load(data.message)
                        .centerCrop()
                        .into(ivContent);
            }

            Calendar cal = Calendar.getInstance();
            TimeZone tz = cal.getTimeZone();

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
            sdf.setTimeZone(tz);

            String localTime = sdf.format(new Date(Long.parseLong(data.timeStamp) * 1000));
            tvTime.setText(localTime);

            return view;
        }
    }

}
