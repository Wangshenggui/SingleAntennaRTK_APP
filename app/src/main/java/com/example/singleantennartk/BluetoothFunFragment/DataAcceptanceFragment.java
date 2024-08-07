package com.example.singleantennartk.BluetoothFunFragment;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.singleantennartk.BluetoothFunActivity;
import com.example.singleantennartk.BtThread.ConnectedThread;
import com.example.singleantennartk.MainActivity;
import com.example.singleantennartk.R;
import com.example.singleantennartk.SocketService;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import android.os.Handler;

public class DataAcceptanceFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static TextView ReceiveGGATextView=null;
    private static TextView ReceiveRMCTextView=null;

    private Handler handler;
    private Runnable runnable;
    private static final long TIMER_DELAY = 500; // 定时器延迟执行时间，单位为毫秒

    TextView lonText=null;
    TextView latText=null;
    TextView distanceText=null;

    Button RecordTestPointButton;


    double wgs84lon,wgs84lat;
    double lastwgs84lon=0,lastwgs84lat=0;


    public DataAcceptanceFragment() {
        // Required empty public constructor
    }

    public static DataAcceptanceFragment newInstance(String param1, String param2) {
        DataAcceptanceFragment fragment = new DataAcceptanceFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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
        View view = inflater.inflate(R.layout.fragment_data_acceptance, container, false);

        ReceiveGGATextView=view.findViewById(R.id.ReceiveGGATextView);
        ReceiveRMCTextView=view.findViewById(R.id.ReceiveRMCTextView);

        lonText=view.findViewById(R.id.lonText);
        latText=view.findViewById(R.id.latText);
        distanceText=view.findViewById(R.id.distanceText);

        RecordTestPointButton = view.findViewById(R.id.RecordTestPointButton);

        //记录测试点按钮
        RecordTestPointButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lastwgs84lon = wgs84lon;
                lastwgs84lat = wgs84lat;
            }
        });

        // 初始化 Handler
        handler = new Handler();

        // 创建一个定时执行的 Runnable
        runnable = new Runnable() {
            String nmeaSentence = "$GNGGA,031813.000,2623.010190,N,10636.513526,E,5,37,0.74,1207.5,M,-26.1,M,10.0,0451*77";
            @SuppressLint("SetTextI18n")
            @Override
            public void run() {
                // 在这里执行定时任务的操作
                // 例如更新 UI 或执行其他逻辑

                // 示例：每隔一定时间更新一次文本
                ReceiveGGATextView.setText(BluetoothFunActivity.ReadGGAString);
                ReceiveRMCTextView.setText(BluetoothFunActivity.ReadRMCString);




                if(BluetoothFunActivity.ReadGGAString.length()>50){
                    nmeaSentence = BluetoothFunActivity.ReadGGAString;
                }


                // Split the sentence into parts
                String[] parts = nmeaSentence.split(",");

                // Extract specific parts based on their position
//                String sentenceType = parts[0];  // $GNGGA
//                String UTCTime = parts[1];       // 031813.000
                String latitude = parts[2];      // 2623.010190,N
                String longitude = parts[4];     // 10636.513526,E
//                String qualityIndicator = parts[6]; // 5
//                String satellitesTracked = parts[7]; // 37
//                String HDOP = parts[8];          // 0.74c
//                String altitude = parts[9];      // 1207.5,M
//                String heightGeoid = parts[11];  // -26.1,M
//                String timeSinceUpdate = parts[13]; // 10.0
//                String stationID = parts[14];    // 0451



                wgs84lon = dms_to_degrees(Double.parseDouble(longitude));
                wgs84lat = dms_to_degrees(Double.parseDouble(latitude));
                lonText.setText(" " + wgs84lon);
                latText.setText(" " + wgs84lat);

                distanceText.setText(" " + linearDistance(wgs84lat,wgs84lon,lastwgs84lat,lastwgs84lon)*1000 + "m");

//                linearDistance(lastwgs84lat,lastwgs84lon,wgs84lat,wgs84lon);

                // 再次调度定时任务
                handler.postDelayed(this, TIMER_DELAY);
            }
        };

        // 第一次调度定时任务
        handler.postDelayed(runnable, TIMER_DELAY);
        return view;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // 移除所有未执行的回调以防止内存泄漏
        handler.removeCallbacks(runnable);
    }

    public static double dms_to_degrees(double dms) {
        double degrees = (int)(dms / 100); // 取整数部分作为度数
        double minutes = dms - degrees * 100; // 取小数部分作为分数
        double decimal_degrees = degrees + minutes / 60.0; // 转换为十进制度数
        return decimal_degrees;
    }
    public static double linearDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        // 使用简单的勾股定理计算直线距离
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = 6371.0 * c; // 地球半径取6371.0公里

        return distance;
    }
}