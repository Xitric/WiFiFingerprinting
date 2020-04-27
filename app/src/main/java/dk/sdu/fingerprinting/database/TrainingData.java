package dk.sdu.fingerprinting.database;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.TypeConverter;

import java.util.HashMap;
import java.util.Map;

@Entity(primaryKeys = {"location", "orientation"})
public class TrainingData {

    @NonNull
    public String location;

    //N=0, E=1, S=2, W=3
    public int orientation;

    public Map<String, Double> signalStrengths;

    public TrainingData(String location, int orientation, Map<String, Double> signalStrengths) {
        this.location = location;
        this.orientation = orientation;
        this.signalStrengths = signalStrengths;
    }

    public static class SignalStrengthTypeConverter {
        @TypeConverter
        public static Map<String, Double> fromString(String value) {
            Map<String, Double> result = new HashMap<>();

            if (value.length() == 0) {
                return result;
            }

            for (String station : value.split(",")) {
                String[] elements = station.split(";");
                result.put(elements[0], Double.parseDouble(elements[1]));
            }

            return result;
        }

        @TypeConverter
        public static String fromList(Map<String, Double> value) {
            StringBuilder result = new StringBuilder();
            for (Map.Entry<String, Double> station : value.entrySet()) {
                result.append(station.getKey())
                        .append(";")
                        .append(station.getValue())
                        .append(",");
            }
            if (result.length() > 0) {
                result.setLength(result.length() - 1);
            }
            return result.toString();
        }
    }
}
