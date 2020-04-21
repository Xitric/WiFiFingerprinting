package dk.sdu.fingerprinting.nearest_neighbor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.List;

import dk.sdu.fingerprinting.FingerprintingDatabase;

public class TrainingDataConverter {

    private final FingerprintingDatabase db;

    public TrainingDataConverter(FingerprintingDatabase db) {
        this.db = db;
    }

//    public LiveData<Void> convertToTrainingData() {
//        Transformations.map(db.sampleDao().getSamples(), result -> {
//            db.sampleDao().getLocations()
//
//            db.trainingDataDao().insert();
//        });
//    }
}
