package com.international.advert;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.international.advert.utility.Constant;
import com.international.advert.utility.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class BleListActivity extends AppCompatActivity {


    //================= chat part ==================================================

    ImageView ivBack1;

    TextView tvStatus;
    Button btnSend;
    ListView lvMsg;
    EditText etMsgBox;
    BleMessageAdapter bleMessageAdapter;

    List<String> arrMsg = new ArrayList<>();

    String strMac = "";

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


    //================list part ====================================================


    ImageView animImage;
    ImageView ivBack, ivTimer;
    ListView lvPaired;
    ListView lvUnpair;
    BleAdapter bleAdapter;
    UnPaireDeviceAdapter unPaireDeviceAdapter;
    TextView tvScan, tvpairField;

    List<String> arrPairedDevices = new ArrayList<>();
    List<String> arrUnpairDevices = new ArrayList<>();


    BluetoothAdapter mBtAdapter;

    RelativeLayout rlChat;
    RelativeLayout rlList;





    boolean isServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_list);

        rlList = findViewById(R.id.rl_list);
        rlChat = findViewById(R.id.rl_chat);
        rlChat.setVisibility(View.GONE);

        animImage = (ImageView) findViewById(R.id.iv_anim);
        GlideDrawableImageViewTarget imageViewTarget = new GlideDrawableImageViewTarget(animImage);
        Glide.with(this).load(Utils.waterResource).into(imageViewTarget);

        //======================  chat part  ==============================================
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (mBluetoothAdapter == null) {
            Utils.short_Toast(this, "Bluetooth is not available");
            goBack();
        }

        ivBack1 = findViewById(R.id.iv_back1);
        ivBack1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mChatService != null) {
                    mChatService.stop();
                }

                rlChat.setVisibility(View.GONE);
                rlList.setVisibility(View.VISIBLE);

                if (mChatService != null) {
                    // Only if the state is STATE_NONE, do we know that we haven't started already
                    if (mChatService.getState() == BluetoothChatService.STATE_NONE) {
                        // Start the Bluetooth chat services
                        mChatService.start();
                    }
                }
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

        //=======================  List part================================================

        ivBack = (ImageView)findViewById(R.id.iv_back);
        ivBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goBack();
            }
        });

        tvScan = findViewById(R.id.tv_scan);
        tvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                doDiscoverty();
            }
        });

        tvpairField = findViewById(R.id.tv_pair);
        tvpairField.setVisibility(View.GONE);

        ivTimer = findViewById(R.id.iv_timer);
        ivTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ensureDiscoverable();
            }
        });

        lvPaired = (ListView)findViewById(R.id.lv_pair);
        bleAdapter = new BleAdapter();
        lvPaired.setAdapter(bleAdapter);
        lvPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                goChat(i, true);
            }
        });
        lvPaired.setVisibility(View.GONE);

        lvUnpair = findViewById(R.id.lv_unpair);
        unPaireDeviceAdapter = new UnPaireDeviceAdapter();
        lvUnpair.setAdapter(unPaireDeviceAdapter);

        lvUnpair.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                goChat(i, false);
            }
        });


        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        //Register for checking connect status
        filter = new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED);
        this.registerReceiver(mReceiver, filter);

        // Get the local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        if (!mBtAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, 120);

        }

        // Get a set of currently paired devices
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            lvPaired.setVisibility(View.VISIBLE);
            tvpairField.setVisibility(View.VISIBLE);
            for (BluetoothDevice device : pairedDevices) {
                arrPairedDevices.add(device.getName() + "\n" + device.getAddress());
            }

            bleAdapter.notifyDataSetChanged();
        } else {
            lvPaired.setVisibility(View.VISIBLE);
            tvpairField.setVisibility(View.VISIBLE);
            arrPairedDevices.add("No devices have been paired.");
            bleAdapter.notifyDataSetChanged();
        }

    //==================================================================

    }

    @Override
    protected void onStart() {
        super.onStart();

        //======  chat part ===========

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

        // Make sure we're not doing discovery anymore
        if (mBtAdapter != null) {
            mBtAdapter.cancelDiscovery();
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver);



        //================ chat part ==============


    }

    @Override
    protected void onResume() {
        super.onResume();

        //==================chat part ================
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

    void goChat(int index, boolean fromPair)
    {
        if (fromPair){
            if (arrPairedDevices.get(index).contains("devices"))
                return;
        } else {
            if (arrUnpairDevices.get(index).contains("devices"))
                return;
        }

        mBtAdapter.cancelDiscovery();

        isServer = true;

        String info = fromPair ? arrPairedDevices.get(index) : arrUnpairDevices.get(index);
        strMac = info.substring(info.length() - 17);

        if (!strMac.isEmpty())
            connectDevice(strMac, true);

        rlChat.setVisibility(View.VISIBLE);
        rlList.setVisibility(View.GONE);
    }

    void goChatByGuest()
    {
        mBtAdapter.cancelDiscovery();

        rlChat.setVisibility(View.VISIBLE);
        rlList.setVisibility(View.GONE);

    }

    //endregion

    //region adapter handle

    class BleAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return arrPairedDevices.size();
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
                view = inflater.inflate(R.layout.cell_ble, null);
            }

            TextView tvName = view.findViewById(R.id.tv_device_name);
            tvName.setText(arrPairedDevices.get(i));

            return view;
        }
    }

    class UnPaireDeviceAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return arrUnpairDevices.size();
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
                view = inflater.inflate(R.layout.cell_ble, null);
            }

            TextView tvName = view.findViewById(R.id.tv_device_name);
            tvName.setText(arrUnpairDevices.get(i));


            return view;
        }
    }

    //endregion

    //region BLE handle

    private void doDiscoverty() {
        Log.d("*****", "doDiscovery()");

        // Indicate scanning in the title
        arrUnpairDevices = new ArrayList<>();
        tvScan.setText("Scanning..");
        tvScan.setEnabled(false);

        // Turn on sub-title for new devices

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }

    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    arrUnpairDevices.add(device.getName() + "\n" + device.getAddress());
                    unPaireDeviceAdapter.notifyDataSetChanged();
                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {

                tvScan.setText("Scan");
                tvScan.setEnabled(true);
                if (arrUnpairDevices.size() == 0) {

                    arrUnpairDevices.add("No devices found.");
                    unPaireDeviceAdapter.notifyDataSetChanged();
                }
            } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)){

                if (!isServer)
                    goChatByGuest();

                isServer = false;
            }
        }
    };


    /**
     * Makes this device discoverable for 300 seconds (5 minutes).
     */
    private void ensureDiscoverable() {
        if (mBtAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 120);
            startActivity(discoverableIntent);
        }
    }

    //endregion


    //=============================================== chat part  =======================

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

                    Utils.short_Toast(BleListActivity.this, "Connected to " + mConnectedDeviceName);

                    break;
                case Constant.MESSAGE_TOAST:

                    Utils.short_Toast(BleListActivity.this, msg.getData().getString(Constant.TOAST));
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

    class BleMessageAdapter extends BaseAdapter
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
