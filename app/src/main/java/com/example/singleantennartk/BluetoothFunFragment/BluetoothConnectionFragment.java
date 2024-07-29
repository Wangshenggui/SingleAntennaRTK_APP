package com.example.singleantennartk.BluetoothFunFragment;

import static com.example.singleantennartk.BtThread.ConnectThread.bluetoothSocket;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.example.singleantennartk.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BluetoothConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class BluetoothConnectionFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private String mParam1;
    private String mParam2;


    //    public static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//符合UUID格式就行。
    ListView BtList = null;
    Intent intent = null;
    //蓝牙操作
    BluetoothAdapter bluetoothAdapter = null;
    List<String> devicesNames = new ArrayList<>();
    ArrayList<BluetoothDevice> readyDevices = null;
    ArrayAdapter<String> btNames = null;

    //自定义线程类的初始化
    static com.example.singleantennartk.BtThread.ConnectThread connectThread = null;
    static com.example.singleantennartk.BtThread.ConnectedThread connectedThread = null;

    public static BluetoothConnectionFragment newInstance(String param1, String param2) {
        BluetoothConnectionFragment fragment = new BluetoothConnectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public BluetoothConnectionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bluetooth_connection, container, false);

        BtList = view.findViewById(R.id.BtList);


        //获取本地蓝牙适配器的信息
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        //先打开蓝牙
        if (!bluetoothAdapter.isEnabled()) {
            intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, 1);//不用管，填1就好，表示打开蓝牙的
        }
        @SuppressLint("MissingPermission")
        Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
        readyDevices = new ArrayList();
        if (pairedDevices != null && pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                readyDevices.add(device);
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
//                    return TODO;
                }
                devicesNames.add(device.getName());
            }
            // Create the ArrayAdapter and set it to the ListView
            ArrayAdapter<String> btNames = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, devicesNames);
            BtList.setAdapter(btNames);
        } else {
            Toast.makeText(getActivity(), "没有设备已经配对！", Toast.LENGTH_SHORT).show();
        }

        //列表项目点击事件，点击蓝牙设备名称，然后连接
        BtList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //看看点击蓝牙设备是否可行
                //Toast.makeText(BluetoothActivity.this, "点击了"+readyDevices.get(position).getName(), Toast.LENGTH_SHORT).show();

                //做连接操作
                //先判断是否有连接，我们只要一个连接，在这个软件内只允许有一个连接
                if (connectThread != null) {//如果不为空，就断开这个连接
                    connectThread.cancel();
                    connectThread = null;
                }
                //开始连接新的设备对象
                connectThread = new com.example.singleantennartk.BtThread.ConnectThread(readyDevices.get(position));
                connectThread.start();//start（）函数开启线程，执行操作

                int delayCount = 0;
                while (true) {
                    try {
                        Thread.sleep(100); // 延时100ms
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    //在返回主界面的操作中开启发送数据的线程
                    if (bluetoothSocket != null && bluetoothSocket.isConnected()) {//先判断连接上了
                        connectedThread = new com.example.singleantennartk.BtThread.ConnectedThread(bluetoothSocket);
                        connectedThread.start();
                        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        readyDevices.get(position).getName();
                        Toast.makeText(getActivity(),"已连接"+readyDevices.get(position).getName()+"\r\n开启数据线程",Toast.LENGTH_SHORT).show();
                        break;
                    }
                    delayCount++;
                    if (delayCount >= 50) {
                        // 如果延时超过100次，退出循环
                        break;
                    }
                }
            }
        });

        return view;
    }
}