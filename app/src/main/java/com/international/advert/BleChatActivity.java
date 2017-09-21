package com.international.advert;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.international.advert.utility.App;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import java.util.ArrayList;
import java.util.List;

public class BleChatActivity extends AppCompatActivity {

    ImageView animImage;
    ImageView ivBack;

    TextView tvStatus;
    Button btnSend;
    ListView lvMsg;
    EditText etMsgBox;
    BleMessageAdapter bleMessageAdapter;

    List<String> arrMsg = new ArrayList<>();

    String strMac;

    private static final String TAG = "BleMsg";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Name of the connected device
     */
    private String mConnectedDeviceName = null;

    /**
     * String buffer for outgoing messages
     */
    private StringBuffer mOutStringBuffer;

    /**
     * Local Bluetooth adapter
     */
    private BluetoothAdapter mBluetoothAdapter = null;

    /**
     * Member object for the chat services
     */
    private BluetoothChatService mChatService = null;
    
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_chat);


        if (getIntent().getStringExtra("data") != null)
            strMac = getIntent().getStringExtra("data");
        else
            strMac = "";


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

        tvStatus = findViewById(R.id.tv_status);

        btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                sendMessage(etMsgBox.getText().toString());
            }
        });

        etMsgBox = findViewById(R.id.et_message);
        lvMsg = findViewById(R.id.lv_ble);
        bleMessageAdapter = new BleMessageAdapter();
        lvMsg.setAdapter(bleMessageAdapter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Utils.short_Toast(this, "Bluetooth is not available");
            goBack();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            // Otherwise, setup the chat session
        } else if (mChatService == null) {
            setupChat();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mChatService != null) {
            mChatService.stop();
        }
        
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (mChatService != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                // Start the Bluetooth chat services
                mChatService.start();
            }
        }
    }

    //region navigation

    void goBack()
    {
        finish();
        Utils.transferAnimation(this, false);
    }

    //endregion
    
    //region event handle

    private TextView.OnEditorActionListener mWriteListener
            = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            // If the action is a key-up event on the return key, send the message
            if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constant.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BluetoothChatService.STATE_CONNECTED:
                            tvStatus.setText(getString(R.string.title_connected_to, mConnectedDeviceName));
                            arrMsg.clear();
                            break;
                        case BluetoothChatService.STATE_CONNECTING:
                            tvStatus.setText(R.string.title_connecting);
                            break;
                        case BluetoothChatService.STATE_LISTEN:
                        case BluetoothChatService.STATE_NONE:
                            tvStatus.setText(R.string.title_not_connected);

                            break;
                    }
                    break;
                case Constant.MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMessage = new String(writeBuf);
                    arrMsg.add("Me:  " + writeMessage);
                    bleMessageAdapter.notifyDataSetChanged();
                    break;
                case Constant.MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    arrMsg.add(mConnectedDeviceName + ":  " + readMessage);
                    bleMessageAdapter.notifyDataSetChanged();
                    Log.d("message", readMessage);
                    break;
                case Constant.MESSAGE_DEVICE_NAME:
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(Constant.DEVICE_NAME);

                    Utils.short_Toast(BleChatActivity.this, "Connected to " + mConnectedDeviceName);

                    break;
                case Constant.MESSAGE_TOAST:

                    Utils.short_Toast(BleChatActivity.this, msg.getData().getString(Constant.TOAST));
                    break;
            }
        }
    };
    
    //endregion

    //region chat handle

    private void sendMessage(String message) {
        // Check that we're actually connected before trying anything
        if (mChatService.getState() != BluetoothChatService.STATE_CONNECTED) {
            Utils.short_Toast(this, "You are not connected to a device");
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            mChatService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            mOutStringBuffer.setLength(0);
            etMsgBox.setText(mOutStringBuffer);
        }
    }

    private void setupChat() {
        Log.d(TAG, "setupChat()");

        // Initialize the compose field with a listener for the return key
        etMsgBox.setOnEditorActionListener(mWriteListener);

        // Initialize the BluetoothChatService to perform bluetooth connections
        mChatService = new BluetoothChatService(this, mHandler);

        // Initialize the buffer for outgoing messages
        mOutStringBuffer = new StringBuffer("");

        if (!strMac.isEmpty())
            connectDevice(strMac, true);
    }

    /**
     * Establish connection with other device
     *
     * @param address   Mac address.
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    private void connectDevice(String address, boolean secure) {

        // Get the BluetoothDevice object
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        // Attempt to connect to the device
        mChatService.connect(device, secure);
    }

    //endregion

    private class BleMessageAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return arrMsg.size();
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

            String msg = arrMsg.get(i);

            LayoutInflater layoutInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if (msg.contains("Me")){
                view = layoutInflater.inflate(R.layout.cell_msg_me, null);
            } else {
                view = layoutInflater.inflate(R.layout.cell_msg_sender, null);
            }

            TextView tvMessage = view.findViewById(R.id.tv_message);
            TextView tvTime = view.findViewById(R.id.tv_time);
            ImageView ivContent = view.findViewById(R.id.iv_photo);

            tvTime.setVisibility(View.GONE);
            ivContent.setVisibility(View.GONE);

            tvMessage.setText(msg);

            return view;
        }
    }
}
