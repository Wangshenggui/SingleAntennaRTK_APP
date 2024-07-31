package com.example.singleantennartk;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.annotation.SuppressLint;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    private EditText ip, port, out;
    private TextView receive1, receive2;
    private Button connect, send, sendgga;
    private SocketService socketService;
    private boolean isBound = false;
    Button goBluetoothButton;
    Button goWebSocketButton;
    public static OutputStream outputStream=null;//获取输出数据

    // 服务连接
    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SocketService.LocalBinder binder = (SocketService.LocalBinder) service;
            socketService = binder.getService();
            isBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBound = false;
        }
    };

    // 广播接收器，接收来自SocketService的消息
    private final BroadcastReceiver messageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String message = intent.getStringExtra("message");
            receive1.setText(message);
            if (message != null) {
                try {
                    JSONObject jsonObject = new JSONObject(message);
                    if (jsonObject.has("lon")) {
                        String lon = jsonObject.getString("lon");
                        String lat = jsonObject.getString("lat");
                        receive1.setText(lon + "   " + lat);
                    } else if (jsonObject.has("S1")) {
                        String S1 = jsonObject.getString("S1");
                        String S2 = jsonObject.getString("S2");
                        receive2.setText(S1 + "   " + S2);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 启动WebSocket服务
        Intent WebSocketserviceIntent = new Intent(this, WebSocketService.class);
        startService(WebSocketserviceIntent);

        // 启动Socket服务
        Intent SocketserviceIntent = new Intent(this, SocketService.class);
        startService(SocketserviceIntent);

        ip = findViewById(R.id.ip);
        port = findViewById(R.id.port);
        out = findViewById(R.id.out);
        receive1 = findViewById(R.id.receive1);
        receive2 = findViewById(R.id.receive2);
        connect = findViewById(R.id.connect);
        send = findViewById(R.id.send);
        sendgga = findViewById(R.id.sendgga);

        goBluetoothButton = findViewById(R.id.goBluetoothButton);
        goWebSocketButton = findViewById(R.id.goWebSocketButton);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = ip.getText().toString();
                String portString = port.getText().toString();
                if (ipAddress.isEmpty() || portString.isEmpty()) {
                    Toast.makeText(MainActivity.this, "请输入IP和端口", Toast.LENGTH_SHORT).show();
                } else {
                    int portNumber = Integer.parseInt(portString);
                    if (isBound) {
                        socketService.connectToServer(ipAddress, portNumber);
                    }
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "GET /" +
                        "RTCM33_GRCEpro" +
                        " HTTP/1.0\r\nUser-Agent: NTRIP GNSSInternetRadio/1.4.10\r\nAccept: */*\r\nConnection: close\r\nAuthorization: Basic " +
                        "Y2VkcjIxNTEzOmZ5eDY5NzQ2" +
                        "\r\n\r\n";

                if (isBound) {
                    socketService.sendMessage(message);
                }
            }
        });

        sendgga.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = "$GNGGA,054704.00,2623.00740643,N,10636.51117030,E,1,08,1.9,1218.1202,M,-29.1518,M,,*65\r\n";

                if (isBound) {
                    socketService.sendMessage(message);
                }
            }
        });

        goBluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, BluetoothFunActivity.class);
                startActivity(intent);
            }
        });

        goWebSocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, WebClientRTKActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SocketService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
        registerReceiver(messageReceiver, new IntentFilter("com.example.ble.RECEIVE_MESSAGE"));
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isBound) {
            unbindService(serviceConnection);
            isBound = false;
        }
        unregisterReceiver(messageReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 软件退出后清空，断开蓝牙操作
        BluetoothFunActivity.connectThread.cancel();
        BluetoothFunActivity.connectedThread.cancel();
    }
}
