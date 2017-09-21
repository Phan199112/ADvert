package com.international.advert;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.GlideDrawableImageViewTarget;
import com.international.advert.utility.App;
import com.international.advert.utility.Utils;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.scan.ScanFilter;
import com.polidea.rxandroidble.scan.ScanResult;
import com.polidea.rxandroidble.scan.ScanSettings;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;

public class BleListActivity extends AppCompatActivity {

    ImageView animImage;
    ImageView ivBack;
    ListView lvBle;
    BleAdapter bleAdapter;
    TextView tvScan;

    private RxBleClient rxBleClient;
    Subscription scanSubscription;

    List<String> arrDevices = new ArrayList<>();

    int REQUEST_ENABLE_BT = 110;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_list);

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

        lvBle = (ListView)findViewById(R.id.lv_ble);
        bleAdapter = new BleAdapter();
        lvBle.setAdapter(bleAdapter);
        lvBle.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                goChat();
            }
        });

        tvScan = findViewById(R.id.iv_scan);
        tvScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                scanBLEDevices();

            }
        });

        rxBleClient = App.getRxBleClient(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isScanning()) {
            /*
             * Stop scanning in onPause callback. You can use rxlifecycle for convenience. Examples are provided later.
             */
            scanSubscription.unsubscribe();
        }

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

    void goBLESetting()
    {
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);

        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
    }

    //endregion

    //region BLE handle

    void scanBLEDevices()
    {

        scanSubscription = rxBleClient.scanBleDevices(
                new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
                        .build(),
                new ScanFilter.Builder()
                        // add custom filters if needed
                        .build()
        )
                .observeOn(AndroidSchedulers.mainThread())
                .doOnUnsubscribe(this::clearSubscription)
                .subscribe(this::addScanResult, this::onScanFailure);
    }


    private void addScanResult(ScanResult result)
    {
        arrDevices.add(result.getBleDevice().getName() != null ? result.getBleDevice().getName() + "|" + result.getBleDevice().getMacAddress() : result.getBleDevice().getMacAddress() + result.toString());
        bleAdapter.notifyDataSetChanged();

        Utils.short_Toast(this, result.getBleDevice().getName() + " | " + result.getBleDevice().getMacAddress() + result.getBleDevice().toString());
        Log.d("scanning", "analyze");
    }

    private void onScanFailure(Throwable throwable) {

        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    private void clearSubscription() {
        scanSubscription = null;
    }

    private boolean isScanning() {
        return scanSubscription != null;
    }

    private void handleBleScanException(BleScanException bleScanException) {
        final String text;

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                text = "Bluetooth is not available";
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                text = "Enable bluetooth and try again";
                goBLESetting();
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                text = "On Android 6.0 location permission is required. Implement Runtime Permissions";
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                text = "Location services needs to be enabled on Android 6.0";
                break;
            case BleScanException.SCAN_FAILED_ALREADY_STARTED:
                text = "Scan with the same filters is already started";
                break;
            case BleScanException.SCAN_FAILED_APPLICATION_REGISTRATION_FAILED:
                text = "Failed to register application for bluetooth scan";
                break;
            case BleScanException.SCAN_FAILED_FEATURE_UNSUPPORTED:
                text = "Scan with specified parameters is not supported";
                break;
            case BleScanException.SCAN_FAILED_INTERNAL_ERROR:
                text = "Scan failed due to internal error";
                break;
            case BleScanException.SCAN_FAILED_OUT_OF_HARDWARE_RESOURCES:
                text = "Scan cannot start due to limited hardware resources";
                break;
            case BleScanException.UNDOCUMENTED_SCAN_THROTTLE:
                text = String.format(
                        Locale.getDefault(),
                        "Android 7+ does not allow more scans. Try in %d seconds",
                        secondsTill(bleScanException.getRetryDateSuggestion())
                );
                break;
            case BleScanException.UNKNOWN_ERROR_CODE:
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                text = "Unable to start scanning";
                break;
        }
        Log.w("EXCEPTION", text, bleScanException);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    private long secondsTill(Date retryDateSuggestion) {
        return TimeUnit.MILLISECONDS.toSeconds(retryDateSuggestion.getTime() - System.currentTimeMillis());
    }

    //endregion


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK){
            if (requestCode == REQUEST_ENABLE_BT){
                scanBLEDevices();
            }
        }
    }

    class BleAdapter extends BaseAdapter
    {

        @Override
        public int getCount() {
            return arrDevices.size();
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
            tvName.setText(arrDevices.get(i));

            return view;
        }
    }
}
