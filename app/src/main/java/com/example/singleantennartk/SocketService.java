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

import com.example.singleantennartk.BtThread.ConnectedThread;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SocketService extends Service {

    private static final String TAG = "SocketService";

    private final IBinder binder = new LocalBinder();
    private Socket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private Thread socketThread;

    private Toast currentToast;
    private Handler mainHandler = new Handler(Looper.getMainLooper());

    // 定时器变量
    private static final long TIMER_INTERVAL = 1000; // 60秒
    private Handler timerHandler;
    private Runnable timerRunnable;
    public static boolean start_flag=false;
    @Override
    public void onCreate() {
        super.onCreate();
        mainHandler = new Handler(Looper.getMainLooper());
        startForegroundService();
        // 初始化定时器处理器和运行任务
        timerHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                // 在这里执行定期任务
                // 例如，可以检查套接字连接状态
                if (socket != null && socket.isConnected() && start_flag) {
                    // 执行某些操作
                    String checkout_str = BluetoothFunActivity.ReadGGAString;
                    String str = checkout_str.substring(0,checkout_str.length()-3);

                    String s = checkout_str.substring(checkout_str.length()-2);

                    char ch=str.charAt(1);
                    int x=(int)ch;
                    int y;
                    for(int i=2;i<str.length();i++){
                        y=(int)str.charAt(i);
                        x=x^y;
                    }
                    //转换成十六进制形式
                    String check=Integer.toHexString(x);

                    if(check.equalsIgnoreCase(s)){
                        showToast(BluetoothFunActivity.ReadGGAString);
                        sendMessage(BluetoothFunActivity.ReadGGAString + "\r\n");
                    }
                }

                // 重新安排定时任务
                timerHandler.postDelayed(this, TIMER_INTERVAL);
            }
        };

        // 第一次启动定时器
        timerHandler.postDelayed(timerRunnable, TIMER_INTERVAL);
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
//                    showToast("消息已发送");
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
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    // 直接处理原始字节数据
                    // 例如，您可能希望将其转换为十六进制或Base64
                    byte[] rawMessage = Arrays.copyOf(buffer, bytesRead);

                    // 记录原始数据（可选）
//                    String rawHexData = bytesToHex(rawMessage);

                    // 通过蓝牙发送数据到RTK的示例
                    MainActivity.outputStream.write(rawMessage);

                    // 示例：Toast显示接收到的原始数据长度
//                    showToast("接收到原始数据，长度: " + rawMessage.length);
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "从套接字读取失败: " + e.getMessage());
                showToast("从套接字读取失败");
            }
        }
    }

    // 将字节数组转换为十六进制字符串表示的实用方法
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    // 在主UI线程上显示Toast消息的方法
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
        // 停止服务时移除定时器任务
        timerHandler.removeCallbacks(timerRunnable);
    }
}
