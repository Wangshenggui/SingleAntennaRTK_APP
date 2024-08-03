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



    Button goBluetoothButton;
    Button goWebSocketButton;
    Button goSocketButton;
    public static OutputStream outputStream=null;//获取输出数据



    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 启动Socket服务
        Intent SocketserviceIntent = new Intent(this, SocketService.class);
        startService(SocketserviceIntent);

        // 启动WebSocket服务
        Intent WebserviceIntent = new Intent(this, WebSocketService.class);
        startService(WebserviceIntent);



        goBluetoothButton = findViewById(R.id.goBluetoothButton);
        goWebSocketButton = findViewById(R.id.goWebSocketButton);
        goSocketButton = findViewById(R.id.goSocketButton);



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

        goSocketButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, GetDiffDataActivity.class);
                startActivity(intent);
            }
        });
    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 软件退出后清空，断开蓝牙操作
        BluetoothFunActivity.connectThread.cancel();
        BluetoothFunActivity.connectedThread.cancel();
    }
}
