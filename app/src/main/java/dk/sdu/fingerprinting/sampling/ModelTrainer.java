package dk.sdu.fingerprinting.sampling;

import android.os.AsyncTask;
import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dk.sdu.fingerprinting.database.FingerprintingDatabase;
import dk.sdu.fingerprinting.database.Sample;
import dk.sdu.fingerprinting.database.TrainingData;

public class ModelTrainer {

    private final FingerprintingDatabase database;

    public ModelTrainer(FingerprintingDatabase database) {
        this.database = database;
    }

    public LiveData<Boolean> convertToTrainingData() {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        ConverterTask task = new ConverterTask(database, result);
        task.execute();
        return result;
    }

    private static class ConverterTask extends AsyncTask<Void, Void, Void> {

        private final FingerprintingDatabase database;
        private final MutableLiveData<Boolean> onComplete;

        ConverterTask(FingerprintingDatabase database, MutableLiveData<Boolean> onComplete) {
            this.database = database;
            this.onComplete = onComplete;
        }

        @Override
        protected void onPreExecute() {
            onComplete.postValue(false);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            database.trainingDataDao().clear();

            List<String> locations = database.sampleDao().getLocations();
            List<TrainingData> trainingData = new ArrayList<>();

            for (String location : locations) {
                for (int orientation = 0; orientation < 4; orientation++) {
                    List<String> macs = database.sampleDao().getMacAddresses(location, orientation);
                    Map<String, Double> stations = new HashMap<>();

                    for (String mac : macs) {
                        List<Sample> samples = database.sampleDao().getSamples(location, mac, orientation);
                        double totalSignalStrength = 0;
                        for (Sample sample : samples) {
                            totalSignalStrength += sample.signalStrength;
                        }
                        double meanSignalStrength = totalSignalStrength / samples.size();
                        stations.put(mac, meanSignalStrength);
                    }

                    trainingData.add(new TrainingData(location, orientation, stations));
                }
            }

            database.trainingDataDao().insert(trainingData);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            onComplete.postValue(true);
        }
    }
}
