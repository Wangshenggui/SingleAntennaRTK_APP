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

import com.example.singleantennartk.BtThread.ConnectedThread;
import com.example.singleantennartk.R;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DataAcceptanceFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DataAcceptanceFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private static TextView ReceiveTextView=null;
    static int text_n=0;

    private static Button SendTestButton=null;

    public DataAcceptanceFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DataAcceptanceFragment.
     */
    // TODO: Rename and change types and number of parameters
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

        ReceiveTextView=view.findViewById(R.id.ReceiveTextView);
        SendTestButton=view.findViewById(R.id.SendTestButton);

        SendTestButton();

        return view;
    }




    private void SendTestButton() {
        SendTestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"草泥马",Toast.LENGTH_SHORT).show();

                ConnectedThread.btWriteString("nimade");
            }
        });
    }

    // 定义处理接收到的数据的方法
    @SuppressLint("SetTextI18n")
    public static void processReceivedData(byte[] buffer, int bytes) {

        String string = new String(buffer);

        if (string.startsWith("$GNGGA")) {
            text_n++;
            ReceiveTextView.setText(text_n + string);
        } else {
            // Handle case when data does not start with "$GNGGA"
            // For example, log or ignore this data
//            ReceiveTextView.setText(text_n + string);
        }
    }

    public static int findCharacter(byte[] data, byte target) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == target) {
                return i; // 找到目标字符，返回其索引
            }
        }
        return -1; // 如果没有找到目标字符，返回 -1
    }
    public static double bytesToDouble(byte[] bytes, int index) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes, index, 8);
        buffer.order(ByteOrder.LITTLE_ENDIAN); // 如果数据是小端字节序
        return buffer.getDouble();
    }
}