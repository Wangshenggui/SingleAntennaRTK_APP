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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.singleantennartk.BtThread.ConnectedThread;

import org.json.JSONException;
import org.json.JSONObject;

public class GetDiffDataActivity extends AppCompatActivity {

    private SocketService socketService;
    private EditText ip, port;
    private TextView receive1;
    private Button connect, send, sendgga;
    private boolean isBound = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_diff_data);

        ip = findViewById(R.id.CORSip);
        port = findViewById(R.id.CORSport);
        receive1 = findViewById(R.id.CORSreceive1);
        connect = findViewById(R.id.CORSconnect);
        send = findViewById(R.id.CORSsend);
        sendgga = findViewById(R.id.CORSsendgga);

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ipAddress = ip.getText().toString();
                String portString = port.getText().toString();
                if (ipAddress.isEmpty() || portString.isEmpty()) {
                    Toast.makeText(GetDiffDataActivity.this, "请输入IP和端口", Toast.LENGTH_SHORT).show();
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
//                    socketService.sendMessage(message);
                    socketService.sendMessage(ConnectedThread.globalString + "\r\n");
                    receive1.setText(ConnectedThread.globalString + "\r\n");
                }
            }
        });
    }

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
//            receive1.setText(message);
//            ConnectedThread.btWriteString(message);
        }
    };

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
}