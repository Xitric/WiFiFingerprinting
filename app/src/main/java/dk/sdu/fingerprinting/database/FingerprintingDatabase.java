package dk.sdu.fingerprinting.database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

@androidx.room.Database(entities = {Sample.class, TrainingData.class}, version = 1, exportSchema = false)
@TypeConverters({TrainingData.SignalStrengthTypeConverter.class})
public abstract class FingerprintingDatabase extends RoomDatabase {

    private static FingerprintingDatabase instance;

    public static FingerprintingDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, FingerprintingDatabase.class, "fingerprinting").build();
        }

        return instance;
    }

    public abstract SampleDao sampleDao();

    public abstract TrainingDataDao trainingDataDao();
}
