package dk.sdu.fingerprinting.nearest_neighbor;

import android.content.Context;
import android.net.wifi.ScanResult;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.sdu.fingerprinting.sampling.OrientationSensor;
import dk.sdu.fingerprinting.sampling.WifiScanner;

public class TestDataSampler implements OrientationSensor.OrientationSensorListener {

    private WifiScanner wifiScanner;
    private OrientationSensor orientationSensor;
    private int orientation;

    public TestDataSampler(Context context, Lifecycle lifecycle) {
        wifiScanner = new WifiScanner(context, lifecycle);
        orientationSensor = new OrientationSensor(context, lifecycle);
        orientationSensor.addListener(this);
    }

    public LiveData<TestData> getTestData(){
        MutableLiveData<TestData> mutableLiveData = new MutableLiveData<>();
        wifiScanner.addListener(scanResults -> {
            Map<String, Double> signalStrengths = new HashMap<>();
            for (ScanResult result: scanResults) {
                signalStrengths.put(result.BSSID, (double) result.level);
            }
            TestData testData = new TestData(orientation, signalStrengths);
            mutableLiveData.postValue(testData);
        });
        wifiScanner.scan(1);
        return mutableLiveData;
    }

    @Override
    public void onOrientation(int orientation) {
        this.orientation = orientation;
    }

}
