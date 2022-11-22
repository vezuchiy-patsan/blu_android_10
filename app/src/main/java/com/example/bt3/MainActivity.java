package com.example.bt3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    final String LOG_TAG = "myLogs";
    Button on, off, scan, scanBLE, button; //кнопки

    private static final int REQUEST_ENABLE_BT = 1; //константа для запуска модуля
    BluetoothAdapter bt = BluetoothAdapter.getDefaultAdapter(); //получение адаптера

    ArrayAdapter<String> adapter; // адаптер массива
    ArrayList<String> word;
    ArrayList<BluetoothDevice> blUUID; // массив
    ListView wordList; // область вывода массива

    List<String[]> blArr; // массив с данными об устройстве для окна с info
    List<String[]> blphone; //лист для создания многомерного массив, туда пишем результаты сканирования

    private BluetoothLeScanner btLe ;
    private boolean scanning; //переменная для проверки условия сканирования
    private Handler handler = new Handler();


    //переиод сканирования 10 сек
    public static final long SCAN_PERIOD = 10000;

    @Override
    protected void onStart() {
        super.onStart();


    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wordList = findViewById(R.id.listView);
        word = new ArrayList<String>();
        blUUID = new ArrayList<BluetoothDevice>();
        blArr = new ArrayList<String[]>();
        blphone = new ArrayList<String[]>();

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, word);
        //установка списка
        wordList.setAdapter(adapter);

        on = findViewById(R.id.bton);
        off = findViewById(R.id.btoff);
        scan = findViewById(R.id.scan);
        scanBLE = findViewById(R.id.scanBLE);


        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        //включить
        on.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("MissingPermission")
            @Override
            public void onClick(View view) {

                if (bt.isEnabled()) {
                    Toast.makeText(MainActivity.this, "Включено уже", Toast.LENGTH_SHORT).show();

                } else {
                    Intent btOn = new Intent(bt.ACTION_REQUEST_ENABLE);
                    startActivityForResult(btOn, REQUEST_ENABLE_BT);

                    Toast.makeText(MainActivity.this, "Вкл", Toast.LENGTH_SHORT).show();

                }
            }
        });
        //проверка
       /* button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });*/
        //выключить
        off.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(wordList.getCount() != 0){
                    word.clear();
                    adapter.notifyDataSetChanged();
                }

                if (bt.isEnabled()) {
                    bt.disable();
                    Toast.makeText(MainActivity.this, "выкл", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, "Уже Выкл", Toast.LENGTH_SHORT).show();
                }

            }
        });
        //поиск bluetooth
        scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                blphone.clear();

                if(bt.isDiscovering()){
                    bt.cancelDiscovery();
                    Toast.makeText(MainActivity.this, "Поиск остановлен", Toast.LENGTH_SHORT).show();
                }else if(bt.isEnabled()){
                    Toast.makeText(MainActivity.this, "Начат поиск", Toast.LENGTH_SHORT).show();
                    word.clear();

                    bt.startDiscovery();
                    //Регистрация модуля broadcasts
                    IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                    filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    registerReceiver(receiver, filter);
                }

//                Set<BluetoothDevice> pairedDevices = bt.getBondedDevices();
//
//                word.clear();
//
//                if (pairedDevices.size() > 0) {
//                    // There are paired devices. Get the name and address of each paired device.
//                    for (BluetoothDevice device : pairedDevices) {
//                        String deviceName = device.getName();
//                        String deviceHardwareAddress = device.getAddress(); // MAC address
//
//                        word.add(deviceName + " (" + deviceHardwareAddress + ")");
//                    }
//                }
//
//                adapter.notifyDataSetChanged();
            }
        });
        //поиск BLE
        scanBLE.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                btLe = bt.getBluetoothLeScanner();

                word.clear();
                blphone.clear();
                scanLeDevice();

            }
        });
        //Дополнительная информация об устройстве
        wordList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if(!blphone.isEmpty()){
                    Log.d(LOG_TAG, "itemClick: position = " + position + ", id = " + id);
                    Intent intent = new Intent(MainActivity.this,InfoActivity.class);
                    if(!blphone.isEmpty()){

                        blArr.add(blphone.get(position));

                        intent.putExtra("blphone", blArr.get(0));
                    }
                    startActivity(intent);
                }

            }
        });

    }

    
    //поиск и запись найденных устройств
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);


                blUUID.add(device);
                word.add(device.getName() + " (" + device.getAddress() + ")");
                adapter.notifyDataSetChanged();

                blphone.add(new String[] {device.getName(), device.getAddress(), String.valueOf(device.getBluetoothClass()), device.getAlias(), String.valueOf(device.getBondState()), String.valueOf(device.getType())});
                device.fetchUuidsWithSdp();
            }
            else{
                //попытка достать uuid
                if(BluetoothDevice.ACTION_UUID.equals(action)) {
                    BluetoothDevice device_uuid = intent.getParcelableExtra(BluetoothDevice.EXTRA_UUID);

                    Log.d(LOG_TAG, "Есть UUID");
                }
            }if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //когда сканирование закончено
                Log.d(LOG_TAG, "End");
            }
        }
    };

    //le сканирование
    private void scanLeDevice(){
        if(!scanning){
            //остановить сканирование после SCAN_PERIOD
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    scanning = false;
                    btLe.stopScan(leScanCallback);
                }
            }, SCAN_PERIOD);

            scanning = true;
            btLe.startScan(leScanCallback);
        }else {
            scanning = false;
            btLe.stopScan(leScanCallback);
        }
    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            BluetoothDevice ledevice = result.getDevice();
            Log.d(LOG_TAG, ledevice.getName() + " " + ledevice.getAddress() + " /" + result.getScanRecord().getServiceUuids());
            word.add(ledevice.getName() + " (" + ledevice.getAddress() + ")");
            adapter.notifyDataSetChanged();
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
        }
    };

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {

        super.onSaveInstanceState(outState);
        System.out.println("Save");
        outState.putStringArrayList("saveList", word);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        System.out.println("restore");

    }

    @Override
    protected void onStop() {
        super.onStop();
        System.out.println("stop");
        blArr.clear();
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("pause");
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        //снять регистрацию
        unregisterReceiver(receiver);
    }




}
