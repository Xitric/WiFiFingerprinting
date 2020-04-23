package dk.sdu.fingerprinting.nearest_neighbor;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import dk.sdu.fingerprinting.database.FingerprintingDatabase;
import dk.sdu.fingerprinting.database.TrainingData;

@SuppressWarnings("ConstantConditions")
public class NnManager {

    private FingerprintingDatabase database;

    public NnManager(FingerprintingDatabase database) {
        this.database = database;
    }

    public LiveData<String> getLocation(TestData testData, int k) {
        // Location = classifier

        return Transformations.map(database.trainingDataDao().getAll(), (trainingDatas) -> knn(trainingDatas, testData, k));
    }

    private String knn(List<TrainingData> trainingDatas, TestData testData, int k) {
        Set<Pair<String, Double>> locationDistances = new TreeSet<>(new PairComparator());
        for (TrainingData trainingData : trainingDatas) {
            double distance = distance(trainingData, testData);
            String location = trainingData.location;
            locationDistances.add(new Pair<>(location, distance));
        }

        Set<String> kLocations = take(k, locationDistances);
        Map<String, Integer> locationCount = new HashMap<>();
        for (String location : kLocations) {
            if (locationCount.containsKey(location)) {
                locationCount.put(location, locationCount.get(location) + 1);
            } else {
                locationCount.put(location, 1);
            }
        }

        String location = "";
        int max = 0;
        for (Map.Entry<String, Integer> entry : locationCount.entrySet()) {
            int value = entry.getValue();
            if (value > max) {
                max = value;
                location = entry.getKey();
            }
        }

        return location;
    }

    private double distance(TrainingData trainingData, TestData testData) {
        double distance = 0;
        for (Map.Entry<String, Double> entry : testData.signalStrengths.entrySet()) {
            double trainingSignal = -100;
            if (trainingData.signalStrengths.containsKey(entry.getKey())) {
                trainingSignal = trainingData.signalStrengths.get(entry.getKey());
            }

            distance += Math.pow(trainingSignal - entry.getValue(), 2);
        }
        return distance;
    }

    private Set<String> take(int k, Set<Pair<String, Double>> locationDistances) {
        Set<String> locations = new HashSet<>();
        Iterator<Pair<String, Double>> iterator = locationDistances.iterator();
        int i = 0;
        while (i < k && iterator.hasNext()) {
            Pair<String, Double> pair = iterator.next();
            locations.add(pair.first);
            i++;
        }
        return locations;
    }

    @SuppressWarnings("ConstantConditions")
    private static class PairComparator implements Comparator<Pair<String, Double>> {

        @Override
        public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
            return Double.compare(o1.second, o2.second);
        }
    }

}
