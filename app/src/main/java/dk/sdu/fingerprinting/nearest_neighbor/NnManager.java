package dk.sdu.fingerprinting.nearest_neighbor;

import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;

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
        return Transformations.map(database.trainingDataDao().getAll(), (trainingDatas) -> knn(trainingDatas, testData, k));
    }

    private String knn(List<TrainingData> trainingDatas, TestData testData, int k) {
        // trainingDatas:   Combination of classifiers and features
        // testData:        An instance to be classified

        // For each classifier in the training data, calculate its distance to the test sample
        Set<Pair<String, Double>> locationDistances = new TreeSet<>(new PairComparator());
        for (TrainingData trainingData : trainingDatas) {
            double distance = distance(trainingData, testData);
            String location = trainingData.location;

            // Order the distances from lowest to highest
            locationDistances.add(new Pair<>(location, distance));
        }

        // Select the k nearest instances
        Set<String> kLocations = take(k, locationDistances);

        // Use the most frequent classifier among the k instances
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
        double signalDistance = signalDistance(trainingData, testData);
        double orientationDistance = orientationDistance(trainingData, testData);
        return signalDistance + orientationDistance;
    }

    private double signalDistance(TrainingData trainingData, TestData testData) {
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

    private double orientationDistance(TrainingData trainingData, TestData testData) {
        //                                        N       E       S         W
        double[][] coordinates = new double[][]{{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        double[] trainingCoordinates = coordinates[trainingData.orientation];
        double[] testCoordinates = coordinates[testData.orientation];

        double angleRad = Math.atan2(testCoordinates[1], testCoordinates[0]) - Math.atan2(trainingCoordinates[1], trainingCoordinates[0]);
        double angleDeg = Math.toDegrees(angleRad);
        return Math.sqrt(Math.abs(angleDeg)) * 2;
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
