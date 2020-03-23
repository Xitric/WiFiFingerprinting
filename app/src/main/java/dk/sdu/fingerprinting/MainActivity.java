package dk.sdu.fingerprinting;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private EditText txt_location;
    private TextView lbl_status;

    private WifiManager wifiManager;
    private WifiScanBroadcastReceiver wifiScanBroadcastReceiver = new WifiScanBroadcastReceiver();
    private WifiManager.WifiLock wifiLock;

    private SampleDatabase database;
    private String currentLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_location = findViewById(R.id.txt_location);
        lbl_status = findViewById(R.id.lbl_status);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, MainActivity.class.getName());

        database = SampleDatabase.getInstance(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(wifiScanBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void scanAction(View sender) {
        currentLabel = txt_location.getText().toString();
        startScanning();
    }

    private void startScanning() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
            return;
        }

        wifiLock.acquire();

        final Handler wifiScanHandler = new Handler();
        Runnable wifiScanRunnable = new Runnable() {

            int sampleCount = 20;

            @Override
            public void run() {
                sampleCount--;

                if (!wifiManager.startScan()) {
                    lbl_status.setText(R.string.status_error);
                    Log.w("WiFiFingerprinting", "Couldn't start Wi-fi scan!");
                }

                if (sampleCount > 0) {
                    wifiScanHandler.postDelayed(this, 1000);
                } else {
                    wifiLock.release();
                    lbl_status.setText(R.string.status_ready);
                }
            }
        };

        lbl_status.setText(R.string.status_sampling);
        wifiScanHandler.post(wifiScanRunnable);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startScanning();
        }
    }

    public void dumpAction(View sender) {
        database.sampleDao().getSamples().observe(this, samples -> {
            for (Sample sample: samples) {
                Log.i("WiFiFingerprintingRes", sample.timestamp + "," + sample.apMac + "," + sample.ssid + "," + sample.signalStrength + "," + sample.locationLabel);
            }
        });
    }

    public void clearAction(View sender) {
        database.submit(() -> database.sampleDao().clear()).observe(this, result ->
                lbl_status.setText(R.string.status_cleared));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiScanBroadcastReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    private final class WifiScanBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (!wifiLock.isHeld() || !WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
                return;
            }

            database.submit(() -> {
                List<ScanResult> scanResults = wifiManager.getScanResults();

                for (ScanResult result : scanResults) {
                    database.sampleDao().insertSample(new Sample(
                            result.timestamp,
                            result.BSSID,
                            result.SSID,
                            result.level,
                            currentLabel
                    ));
                }
            });
        }
    }
}
