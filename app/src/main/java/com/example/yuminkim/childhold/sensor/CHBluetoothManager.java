package com.example.yuminkim.childhold.sensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.example.yuminkim.childhold.model.Child;

import java.util.ArrayList;
import java.util.List;

public class CHBluetoothManager {
    public interface DriveEndScanCallback {
        void driveEnd(boolean status);
        void scanEnd();
    }

    //TODO: scan period make infinite
    private static final long SCAN_PERIOD = 10000;
    private int REQUEST_ENABLE_BT = 1;

    private Context context;
    private BluetoothAdapter bluetoothAdapter;
    private Handler handler;
    private BluetoothLeScanner leScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;

    private static CHBluetoothManager _instance = null;
    public static CHBluetoothManager getInstance(Context context) {
        if (_instance == null) {
            _instance = new CHBluetoothManager(context);
        }
        return _instance;
    }

    CHBluetoothManager(Context context) {
        this.context = context;
        init();
    }

    private void init() {
        handler = new Handler();
        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(context, "BLE Not Supported", Toast.LENGTH_SHORT).show();
            return;
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        initScan();
    }

    private void initScan() {
        leScanner = bluetoothAdapter.getBluetoothLeScanner();
        settings = new ScanSettings.Builder()
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .build();
        filters = new ArrayList<>();
    }

    public void scanLeDevice(final boolean enable, final ScanCallback scanCallback) {
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    leScanner.stopScan(scanCallback);
                }
            }, SCAN_PERIOD);

            leScanner.startScan(filters, settings, scanCallback);
        } else {
            leScanner.stopScan(scanCallback);
        }
    }

    public void scanLeDeviceForExit(final boolean enable, final ScanCallback scanCallback, final DriveEndScanCallback stopScanCallback) {
        if (enable) {
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    leScanner.stopScan(scanCallback);
                    stopScanCallback.scanEnd();
                }
            }, SCAN_PERIOD);

            leScanner.startScan(filters, settings, scanCallback);
        } else {
            leScanner.stopScan(scanCallback);
        }
    }

    private int remainChildCount;
    public void driveEndScan(final List<Child> children, final DriveEndScanCallback callback) {
        remainChildCount = 0;
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                leScanner.stopScan(new ScanCallback() {
                    @Override
                    public void onScanResult(int callbackType, ScanResult result) {
                        super.onScanResult(callbackType, result);
                    }
                });
                callback.driveEnd(remainChildCount == 0);
            }
        }, SCAN_PERIOD);

        leScanner.startScan(filters, settings, new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                Log.d("find", result.getDevice().getAddress());
                for (Child c : children) {
                    if (c.getBeaconId().equals(result.getDevice().getAddress())) {
                        Log.d("find","++");
                        remainChildCount++;
                        break;
                    }
                }
            }
        });
    }

}
