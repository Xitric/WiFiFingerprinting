package dk.sdu.fingerprinting.database;

import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

@Entity(primaryKeys = {"location", "orientation"})
public class TrainingData {

    @NonNull
    public String location;

    //N=0, E=1, S=2, W=3
    public int orientation;

    public List<Pair<String, Double>> signalStrengths;

    public TrainingData(String location, int orientation, List<Pair<String, Double>> signalStrengths) {
        this.location = location;
        this.orientation = orientation;
        this.signalStrengths = signalStrengths;
    }

    public static class SignalStrengthTypeConverter {
        @TypeConverter
        public static List<Pair<String, Double>> fromString(String value) {
            List<Pair<String, Double>> result = new ArrayList<>();

            for (String station : value.split(",")) {
                String[] elements = station.split(";");
                result.add(new Pair<>(elements[0], Double.parseDouble(elements[1])));
            }

            return result;
        }

        @TypeConverter
        public static String fromList(List<Pair<String, Double>> value) {
            StringBuilder result = new StringBuilder();
            for (Pair<String, Double> station : value) {
                result.append(station.first)
                        .append(";")
                        .append(station.second)
                        .append(",");
            }
            if (result.length() > 0) {
                result.setLength(result.length() - 1);
            }
            return result.toString();
        }
    }
}
