package com.example.singleantennartk;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketService extends Service {

    private static final String TAG = "SocketService";

    private final IBinder binder = new LocalBinder();
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread socketThread;

    private Toast currentToast;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    public void onCreate() {
        super.onCreate();
        mainHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public class LocalBinder extends Binder {
        SocketService getService() {
            return SocketService.this;
        }
    }

    // Method to connect to the server
    public void connectToServer(String ipAddress, int port) {
        socketThread = new Thread(() -> {
            try {
                socket = new Socket(ipAddress, port);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();

                // Show a toast message
                showToast("已连接服务器");

                // Start reading from the socket
                readFromSocket();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Connection failed: " + e.getMessage());
                showToast("连接失败");
            }
        });
        socketThread.start();
    }

    // Method to send messages to the server
    public void sendMessage(String message) {
        if (socket != null && outputStream != null) {
            new Thread(() -> {
                try {
                    outputStream.write(message.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    showToast("消息已发送");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Failed to send message: " + e.getMessage());
                    showToast("发送消息失败");
                }
            }).start();
        } else {
            showToast("套接字未被连接");
        }
    }

    // Method to read messages from the server
    private void readFromSocket() {
        if (inputStream != null) {
            try {
                byte[] buffer = new byte[2048];
                int bytes;
                while ((bytes = inputStream.read(buffer)) != -1) {
                    String message = new String(buffer, 0, bytes, StandardCharsets.UTF_8);
                    Log.d(TAG, "Message received: " + message);
                    showToast("收到信息: " + message);

                    // Broadcast the message
                    Intent intent = new Intent("com.example.ble.RECEIVE_MESSAGE");
                    intent.putExtra("message", message);
                    sendBroadcast(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to read from socket: " + e.getMessage());
                showToast("从套接字读取失败");
            }
        }
    }

    private void showToast(final String message) {
        // Ensure Toast runs on the main UI thread
        mainHandler.post(() -> {
            // Cancel the previous toast if it's still being displayed
            if (currentToast != null) {
                currentToast.cancel();
            }

            // Create and show a new toast
            currentToast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
            currentToast.show();
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (socket != null) {
                socket.close();
                showToast("连接已关闭");
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to close socket: " + e.getMessage());
            showToast("关闭套接字失败");
        }
    }
}
