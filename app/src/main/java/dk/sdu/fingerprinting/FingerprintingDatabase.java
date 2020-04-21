package dk.sdu.fingerprinting;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import dk.sdu.fingerprinting.nearest_neighbor.TrainingData;
import dk.sdu.fingerprinting.nearest_neighbor.TrainingDataDao;

@Database(entities = {Sample.class}, version = 1, exportSchema = false)
@TypeConverters({TrainingData.SignalStregthTypeConverter.class})
public abstract class FingerprintingDatabase extends RoomDatabase {

    private static FingerprintingDatabase instance;
    private ExecutorService databaseExecutor;

    public abstract SampleDao sampleDao();

    public abstract TrainingDataDao trainingDataDao();

    public static FingerprintingDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, FingerprintingDatabase.class, "database-name").build();
            instance.databaseExecutor = Executors.newSingleThreadExecutor();
        }

        return instance;
    }

    public LiveData<Boolean> submit(final Runnable job) {
        final MutableLiveData<Boolean> result = new MutableLiveData<>();

        databaseExecutor.submit(() -> {
            job.run();
            result.postValue(true);
        });

        return result;
    }
}
