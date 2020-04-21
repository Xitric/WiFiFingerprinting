package dk.sdu.fingerprinting.sampling;

import android.net.wifi.ScanResult;

import java.util.List;
import java.util.concurrent.ExecutorService;

import dk.sdu.fingerprinting.database.FingerprintingDatabase;
import dk.sdu.fingerprinting.database.Sample;

public class SampleManager implements WifiScanner.WifiScanListener, OrientationSensor.OrientationSensorListener {

    private final FingerprintingDatabase database;
    private final ExecutorService backgroundExecutor;

    private String location;
    private int orientation;

    public SampleManager(FingerprintingDatabase database, ExecutorService backgroundExecutor) {
        this.database = database;
        this.backgroundExecutor = backgroundExecutor;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    @Override
    public void onScanResults(List<ScanResult> scanResults) {
        backgroundExecutor.submit(() -> {
            for (ScanResult result : scanResults) {
                database.sampleDao().insertSample(new Sample(
                        result.timestamp,
                        result.BSSID,
                        result.level,
                        location,
                        orientation
                ));
            }
        });
    }

    @Override
    public void onOrientation(int orientation) {
        this.orientation = orientation;
    }
}
