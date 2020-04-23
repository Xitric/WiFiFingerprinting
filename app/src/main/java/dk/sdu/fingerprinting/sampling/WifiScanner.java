package dk.sdu.fingerprinting.sampling;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Handler;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.List;

public class WifiScanner extends BroadcastReceiver implements LifecycleObserver {

    private final Context context;
    private final WifiManager wifiManager;
    private final WifiManager.WifiLock wifiLock;

    private WifiScanListener listener;

    public WifiScanner(Context context, Lifecycle lifecycle) {
        this.context = context.getApplicationContext();
        this.wifiManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        this.wifiLock = wifiManager.createWifiLock(WifiManager.WIFI_MODE_SCAN_ONLY, SamplingActivity.class.getName());

        lifecycle.addObserver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    protected void register() {
        context.registerReceiver(this, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    protected void deregister() {
        context.unregisterReceiver(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    protected void cleanup() {
        if (wifiLock.isHeld()) {
            wifiLock.release();
        }
    }

    public LiveData<Boolean> scan(int samples) {
        MutableLiveData<Boolean> status = new MutableLiveData<>();
        wifiLock.acquire();

        final Handler wifiScanHandler = new Handler();
        Runnable wifiScanRunnable = new Runnable() {

            int sampleCount = samples;

            @Override
            public void run() {
                if (sampleCount > 0) {
                    sampleCount--;
                    if (!wifiManager.startScan()) {
                        status.postValue(false);
                        wifiLock.release();
                    } else {
                        wifiScanHandler.postDelayed(this, 3000);
                    }
                } else {
                    status.postValue(true);
                }
            }
        };

        wifiScanHandler.post(wifiScanRunnable);
        return status;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!wifiLock.isHeld() || !WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(intent.getAction())) {
            return;
        }

        wifiLock.release();
        this.listener.onScanResults(wifiManager.getScanResults());
    }

    public void addListener(WifiScanListener listener) {
        this.listener = listener;
    }

    public interface WifiScanListener {
        void onScanResults(List<ScanResult> scanResults);
    }
}
