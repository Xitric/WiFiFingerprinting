package dk.sdu.fingerprinting;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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

import java.util.Arrays;
import java.util.List;

public class SamplingActivity extends AppCompatActivity implements SensorEventListener {

    private EditText txt_location;
    private TextView lbl_status;

    private WifiManager wifiManager;
    private WifiScanBroadcastReceiver wifiScanBroadcastReceiver = new WifiScanBroadcastReceiver();
    private WifiManager.WifiLock wifiLock;

    private FingerprintingDatabase database;
    private String currentLabel;

    private SensorManager sensorManager;
    private final float[] orientationAngles = new float[3];


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txt_location = findViewById(R.id.txt_location);
        lbl_status = findViewById(R.id.lbl_status);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, SamplingActivity.class.getName());

        database = FingerprintingDatabase.getInstance(this);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    @Override
    protected void onResume() {
        super.onResume();

        registerReceiver(wifiScanBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        if (accel != null) {
            sensorManager.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }

        Sensor mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        if (mag != null) {
            sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_NORMAL, SensorManager.SENSOR_DELAY_UI);
        }
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
                    wifiScanHandler.postDelayed(this, 3000);
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
                Log.i("WiFiFingerprintingRes", sample.timestamp + "," + sample.apMac + "," + sample.signalStrength + "," + sample.locationLabel);
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
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] accelerometerReading = new float[3];
        float[] magnetometerReading = new float[3];

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading,
                    0, accelerometerReading.length);
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading,
                    0, magnetometerReading.length);
        }

        float[] rotationMatrix = new float[9];
        SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading);
        SensorManager.getOrientation(rotationMatrix, orientationAngles);

        Log.i("HelloWorld", Arrays.toString(orientationAngles));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        //Ignore
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
                            result.level,
                            currentLabel
                    ));
                }
            });
        }
    }
}
