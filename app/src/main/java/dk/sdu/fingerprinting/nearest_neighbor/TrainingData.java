package dk.sdu.fingerprinting.nearest_neighbor;

import android.util.Pair;

import androidx.room.Entity;
import androidx.room.TypeConverter;

import java.util.ArrayList;
import java.util.List;

@Entity(primaryKeys = {"location", "orientation"})
public class TrainingData {

    public String location;

    //N=0, E=1, S=2, W=3
    public int orientation;

    public ArrayList<Pair<String, Integer>> signalStrengths;

    public static class SignalStregthTypeConverter {
        @TypeConverter
        public static List<Pair<String, Integer>> fromString(String value) {
            List<Pair<String, Integer>> result = new ArrayList<>();

            for (String station : value.split(",")) {
                String[] elements = station.split(";");
                result.add(new Pair<>(elements[0], Integer.parseInt(elements[1])));
            }

            return result;
        }

        @TypeConverter
        public static String fromArrayList(ArrayList<Pair<String, Integer>> value) {
            StringBuilder result = new StringBuilder();
            for (Pair<String, Integer> station : value) {
                result.append(station.first)
                        .append(";")
                        .append(station.second)
                        .append(",");
            }
            result.setLength(result.length() - 1);
            return result.toString();
        }
    }
}
