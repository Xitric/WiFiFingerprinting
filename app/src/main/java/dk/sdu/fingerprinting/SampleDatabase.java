package dk.sdu.fingerprinting;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {Sample.class}, version = 1, exportSchema = false)
public abstract class SampleDatabase extends RoomDatabase {

    private static SampleDatabase instance;
    private ExecutorService databaseExecutor;

    public abstract SampleDao sampleDao();

    public static SampleDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, SampleDatabase.class, "database-name").build();
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
