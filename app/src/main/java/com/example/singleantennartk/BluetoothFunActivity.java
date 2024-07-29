package com.example.singleantennartk;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.singleantennartk.BtThread.ConnectThread;
import com.example.singleantennartk.BluetoothFunFragment.BluetoothConnectionFragment;
import com.example.singleantennartk.BluetoothFunFragment.DataAcceptanceFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.UUID;

public class BluetoothFunActivity extends AppCompatActivity {

    public static ConnectThread connectThread;
    public static ConnectThread connectedThread;
    private BottomNavigationView mNavigationView;
    private Fragment[] fragments;
    private FragmentManager mFragmentManager;
    private int lastFragment;
    public static UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");//符合UUID格式就行。
    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_fun);

        mNavigationView = findViewById(R.id.bluetoothfunction_navigation_bar);
        initFragment();
        initListener();

        // Delay loading other fragments to avoid crash
        new Handler().postDelayed(this::loadOtherFragments, 500);
    }
    private void initFragment() {
        BluetoothConnectionFragment mBluetoothConnectionFragment = new BluetoothConnectionFragment();
        DataAcceptanceFragment mDataAcceptanceFragment = new DataAcceptanceFragment();

        fragments = new Fragment[]{mBluetoothConnectionFragment, mDataAcceptanceFragment};
        mFragmentManager = getSupportFragmentManager();
//        // 默认显示HomeFragment
//        lastFragment = 0;
//        mFragmentManager.beginTransaction()
//                .replace(R.id.bluetoothconnection, mBluetoothConnectionFragment)
//                .show(mBluetoothConnectionFragment)
//                .commit();
        switchFragment(lastFragment, 0);
    }
    private void initListener() {
        mNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int i = item.getItemId();
                if (i == R.id.bluetoothconnection) {
                    if (lastFragment != 0) {
                        switchFragment(lastFragment, 0);
                        lastFragment = 0;
                    }
                    return true;
                } else if (i == R.id.dataacceptance) {
                    if (lastFragment != 1) {
                        switchFragment(lastFragment, 1);
                        lastFragment = 1;
                    }
                    return true;
                }
                return false;
            }
        });
    }
    private void switchFragment(int lastFragment, int index) {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.hide(fragments[lastFragment]);
        if (!fragments[index].isAdded()) {
            transaction.add(R.id.bluetoothfunction_page_controller, fragments[index]);
        }
        transaction.show(fragments[index]).commitAllowingStateLoss();
    }
    private void loadOtherFragments() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        for (int i = 1; i < fragments.length; i++) {
            if (!fragments[i].isAdded()) {
                transaction.add(R.id.bluetoothfunction_page_controller, fragments[i]);
                transaction.hide(fragments[i]);
            }
        }
        transaction.commitAllowingStateLoss();
    }
}