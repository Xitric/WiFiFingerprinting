package dk.sdu.fingerprinting.nearest_neighbor;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.sdu.fingerprinting.database.FingerprintingDatabase;
import dk.sdu.fingerprinting.database.TrainingData;

public class NnManager {

    private FingerprintingDatabase database;

    public NnManager(FingerprintingDatabase database) {
        this.database = database;
    }

    private LiveData<String> getLocation() {
        // Location = classifier

        return Transformations.map(database.trainingDataDao().getAll(), this::knn);
    }

    private String knn(List<TrainingData> trainingData) {
        // Mac address with a list of signals strengths
        Map<String, List<Integer>> macSignalStrengths = new HashMap<>();
        for (TrainingData data : trainingData) {
            for (Pair macStrength: data.signalStrengths) {
                String macAddress = (String) macStrength.first;
                int signalStrength = (int) macStrength.second;
                if (!macSignalStrengths.containsKey(macAddress)){
                    macSignalStrengths.put(macAddress, Arrays.asList(signalStrength));
                } else {
                    macSignalStrengths.get(macAddress).add(signalStrength);
                }
            }
        }
        return "location";
    }


}
