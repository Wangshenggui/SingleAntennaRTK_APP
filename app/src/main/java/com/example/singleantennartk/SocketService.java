package com.example.singleantennartk;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
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
        startForegroundService();
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

    // 启动前台服务的方法
    private void startForegroundService() {
        // 创建通知频道并启动前台服务以防止服务被系统杀掉
        // 创建通知频道并启动前台服务以防止服务被系统杀掉
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("socket_channel", "Socket Service", NotificationManager.IMPORTANCE_DEFAULT);
        }
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(channel);
        }

        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(this, "socket_channel")
                    .setContentTitle("Socket Service")
                    .setContentText("Running...")
                    .setSmallIcon(R.drawable.ic_back);
        }

        Notification notification = builder.build();
        startForeground(1, notification);
    }

    // 连接服务器的方法
    public void connectToServer(String ipAddress, int port) {
        socketThread = new Thread(() -> {
            try {
                socket = new Socket(ipAddress, port);
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();

                // 显示一个Toast消息
                showToast("已连接服务器");

                // 开始从套接字读取
                readFromSocket();

            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "连接失败: " + e.getMessage());
                showToast("连接失败");
            }
        });
        socketThread.start();
    }

    // 发送消息到服务器的方法
    public void sendMessage(String message) {
        if (socket != null && outputStream != null) {
            new Thread(() -> {
                try {
                    outputStream.write(message.getBytes(StandardCharsets.UTF_8));
                    outputStream.flush();
                    showToast("消息已发送");
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "消息发送失败: " + e.getMessage());
                    showToast("消息发送失败");
                }
            }).start();
        } else {
            showToast("套接字未连接");
        }
    }

    // 从服务器读取消息的方法
    private void readFromSocket() {
        if (inputStream != null) {
            try {
                byte[] buffer = new byte[2048];
                int bytes;
                while ((bytes = inputStream.read(buffer)) != -1) {
                    String message = new String(buffer, 0, bytes, StandardCharsets.UTF_8);
                    Log.d(TAG, "收到信息: " + message);
                    showToast("收到信息: " + message);

                    // 广播消息
                    Intent intent = new Intent("com.example.ble.RECEIVE_MESSAGE");
                    intent.putExtra("message", message);
                    sendBroadcast(intent);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "从套接字读取失败: " + e.getMessage());
                showToast("从套接字读取失败");
            }
        }
    }

    private void showToast(final String message) {
        // 确保Toast在主UI线程上运行
        mainHandler.post(() -> {
            // 如果前一个toast还在显示，取消它
            if (currentToast != null) {
                currentToast.cancel();
            }

            // 创建并显示一个新的toast
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
            Log.e(TAG, "关闭套接字失败: " + e.getMessage());
            showToast("关闭套接字失败");
        }
    }
}
