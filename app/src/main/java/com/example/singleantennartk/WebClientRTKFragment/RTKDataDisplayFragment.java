package com.example.singleantennartk.WebClientRTKFragment;

import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.example.singleantennartk.MainActivity;
import com.example.singleantennartk.R;
import com.example.singleantennartk.WebClientRTKActivity;

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
    private Button gotoDiffCccountSettingButton;

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
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_r_t_k_data_display, container, false);

        // Initialize WebView
        webView = view.findViewById(R.id.webView);
        gotoDiffCccountSettingButton = view.findViewById(R.id.gotoDiffCccountSettingButton);


        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("file:///android_asset/BaiduMap.html"); // 载入网页的URL

        gotoDiffCccountSettingButton();

        return view;
    }

    private void gotoDiffCccountSettingButton() {
        gotoDiffCccountSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), DiffAccountActivity.class);
//                startActivity(intent);
            }
        });
    }
}
