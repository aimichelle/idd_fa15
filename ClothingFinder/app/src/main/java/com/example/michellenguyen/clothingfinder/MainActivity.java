package com.example.michellenguyen.clothingfinder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private final static int REQUEST_ENABLE_BT = 1;

    BluetoothDevice bluetoothDevice;
    BluetoothAdapter btAdapter;
    BluetoothGatt bluetoothGatt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BluetoothManager btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);

        btAdapter = btManager.getAdapter();
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }
        // Needs to connect to sensor tags as well and relay all information to server
        btAdapter.startLeScan(leScanCallback);
    }

    private BluetoothAdapter.LeScanCallback leScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
            if (device.getName() != null) {
                Log.d("BLE", "Found device");
                Log.d("Found", device.getName());
                if (device.getName().equals("ClothingDevice")) {
                    Log.d("BLE", "Found ClothingDevice");
                    bluetoothDevice = device;
                    bluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, false, btleGattCallback);
                    btAdapter.stopLeScan(leScanCallback);
                }

            }
        }
    };

    private final BluetoothGattCallback btleGattCallback = new BluetoothGattCallback() {
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS)  {
                int val = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT8, 1);
                Log.d("Read BLE", Integer.toString(val));

            }
        }
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, final int status, final int newState) {
            // this will get called when a device connects or disconnects
            if (newState == (BluetoothProfile.STATE_CONNECTED)) {
                bluetoothGatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
            // this will get called after the client initiates a BluetoothGatt.discoverServices() call
            List<BluetoothGattService> services = bluetoothGatt.getServices();
            for (BluetoothGattService service : services) {
//                Log.d("service", service.getUuid().toString());
                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    Iterator<BluetoothGattCharacteristic> characteristicIterator = characteristics.iterator();
                while (characteristicIterator.hasNext()) {

                    BluetoothGattCharacteristic characteristic = characteristicIterator.next();
                    if (characteristic.getUuid().toString().equals("0000a002-0000-1000-8000-00805f9b34fb")) {
                        byte[] value = new byte[2];
                        value[0] = (byte) (0x00);
                        value[1] = (byte) (0x01);

                        characteristic.setValue(value);
                        boolean stat = gatt.writeCharacteristic(characteristic);
                        Log.d("BLE", Boolean.toString(stat));
                    }
                    if (characteristic.getUuid().toString().equals("0000a001-0000-1000-8000-00805f9b34fb")) {
                            gatt.readCharacteristic(characteristic);

                    }
                }

            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
