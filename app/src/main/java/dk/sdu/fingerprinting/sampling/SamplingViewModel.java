package dk.sdu.fingerprinting.sampling;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.sdu.fingerprinting.R;
import dk.sdu.fingerprinting.database.FingerprintingDatabase;

public class SamplingViewModel extends AndroidViewModel {

    private final ExecutorService backgroundExecutor;
    private final FingerprintingDatabase database;
    private final SampleManager sampleManager;
    private final ModelTrainer modelTrainer;

    private WifiScanner wifiScanner;
    private OrientationSensor orientationSensor;
    private MediatorLiveData<Integer> status;

    public SamplingViewModel(@NonNull Application application) {
        super(application);
        backgroundExecutor = Executors.newSingleThreadExecutor();
        database = FingerprintingDatabase.getInstance(application);
        sampleManager = new SampleManager(database, backgroundExecutor);
        modelTrainer = new ModelTrainer(database);

        status = new MediatorLiveData<>();
        status.postValue(R.string.status_ready);
    }

    void init(Lifecycle lifecycle) {
        if (this.wifiScanner == null) {
            this.wifiScanner = new WifiScanner(this.getApplication().getApplicationContext(), lifecycle);
            this.orientationSensor = new OrientationSensor(this.getApplication().getApplicationContext(), lifecycle);

            this.wifiScanner.addListener(sampleManager);
            this.orientationSensor.addListener(sampleManager);
        }
    }

    LiveData<Integer> getStatus() {
        return status;
    }

    LiveData<Float> getOrientation() {
        return Transformations.map(orientationSensor.getOrientation(), rawOrientation -> {
            if (rawOrientation < 0) {
                return (float) ((rawOrientation + 2 * Math.PI) * 2 / Math.PI);
            }

            return (float) (rawOrientation * 2 / Math.PI);
        });
    }

    void startScanning(String location) {
        sampleManager.setLocation(location);

        LiveData<Boolean> scanStatus = wifiScanner.scan(20);
        status.addSource(scanStatus, success -> {
            status.setValue(success ? R.string.status_ready : R.string.status_error);
            status.removeSource(scanStatus);
        });
        status.postValue(R.string.status_sampling);
    }

    void clear(String name) {
        backgroundExecutor.submit(() -> {
            database.sampleDao().clear(name);
            status.postValue(R.string.status_cleared);
        });
    }

    void train() {
        LiveData<Boolean> trainStatus = modelTrainer.convertToTrainingData();
        status.addSource(trainStatus, success -> {
            status.setValue(success ? R.string.status_ready : R.string.status_error);
            status.removeSource(trainStatus);
        });
        status.postValue(R.string.status_training);
    }

    LiveData<Integer> count() {
        return database.sampleDao().count();
    }
}
