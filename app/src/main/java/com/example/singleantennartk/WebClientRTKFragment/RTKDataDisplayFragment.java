package com.example.singleantennartk.WebClientRTKFragment;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.example.singleantennartk.MainActivity;
import com.example.singleantennartk.R;
import com.example.singleantennartk.WebClientRTKActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link RTKDataDisplayFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class RTKDataDisplayFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private WebView webView;
    private Button WebSendButton;
    private Handler handler;
    private WebSocketServiceReceiver webSocketReceiver;

    public RTKDataDisplayFragment() {
        // Required empty public constructor
    }

    public static RTKDataDisplayFragment newInstance(String param1, String param2) {
        RTKDataDisplayFragment fragment = new RTKDataDisplayFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        handler = new Handler(Looper.getMainLooper());
        webSocketReceiver = new WebSocketServiceReceiver();
    }

    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_r_t_k_data_display, container, false);

        // Initialize WebView
        webView = view.findViewById(R.id.webView);
        WebSendButton = view.findViewById(R.id.WebSendButton);


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/BaiduMap.html"); // 载入网页的URL

        WebSendButton();

        return view;
    }

    private void WebSendButton() {
        WebSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JSONObject data = new JSONObject();
                String[] variables = {"command"};

                try {
                    // 给 n1 赋值
                    data.put(variables[0], "write");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                String jsonMessage = data.toString();

                Intent intent = new Intent("SendWebSocketMessage");
                intent.putExtra("message", "{\"command\":\"write\"}");
                requireContext().sendBroadcast(intent);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter("WebSocketMessage");
        requireContext().registerReceiver(webSocketReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
        requireContext().unregisterReceiver(webSocketReceiver);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null); // Stop periodic task
    }

    private class WebSocketServiceReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if ("WebSocketMessage".equals(intent.getAction())) {
                String message = intent.getStringExtra("message");

                try {
                    JSONObject jsonObject = new JSONObject(message);

                    Toast.makeText(getActivity(),"你干嘛",Toast.LENGTH_SHORT).show();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
