package com.example.yuminkim.childhold.sensor;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.widget.Toast;

import com.example.yuminkim.childhold.util.ByteUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CHBluetoothManager {
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

    /**
     * 4.3 BLE 스캔시 128-bit UUID를 가지고 필터링하는 기능이 불안정하기 때문에 읽어 온 UUID byte array를 변환하여 사용
     * convert byte array to 128-bit uuid
     * @return UUID
     */
    private List<UUID> parseUUIDs(byte[] advertisedData) {
        List<UUID> uuidList = new ArrayList<>();

        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        while (buffer.remaining() > 2) {
            byte length = buffer.get();
            if (length == 0) break;

            byte type = buffer.get();
            switch (type) {
                case 0x02: // Partial list of 16-bit UUIDs
                case 0x03: // Complete list of 16-bit UUIDs
                    while (length >= 2) {
                        uuidList.add(UUID.fromString(String.format(
                                "%08x-0000-1000-8000-00805f9b34fb", buffer.getShort())));
                        length -= 2;
                    }
                    break;

                case 0x06: // Partial list of 128-bit UUIDs
                case 0x07: // Complete list of 128-bit UUIDs
                    while (length >= 16) {
                        long lsb = buffer.getLong();
                        long msb = buffer.getLong();
                        uuidList.add(new UUID(msb, lsb));
                        length -= 16;
                    }
                    break;

                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }
        return uuidList;
    }

    public static String getDeviceUID(byte[] advertisedData) {
        ByteBuffer buffer = ByteBuffer.wrap(advertisedData).order(ByteOrder.LITTLE_ENDIAN);
        byte[] mfgData = null;
        while (buffer.remaining() > 2) {
            byte length = buffer.get(); // 데이터의 길이 타입까지 같이있어서 실제 데이터길이는 한개 짧음
            if (length == 0) break;

            byte type = buffer.get();  // 데이터의 타입

            switch (type) {
                case (byte)0xFF:  // Manufacturer Specific Data
                    length--;
                    mfgData = new byte[length];
                    for(int i = 0;  i<length; i++) {
                        mfgData[i] = buffer.get();
                    }
                    break;
                default:
                    buffer.position(buffer.position() + length - 1);
                    break;
            }
        }
        if(mfgData == null) {
            return null;
        } else {
            return Integer.toHexString(ByteUtil.byteToInt(mfgData));
        }
    }

}
