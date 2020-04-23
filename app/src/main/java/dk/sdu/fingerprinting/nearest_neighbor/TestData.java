package dk.sdu.fingerprinting.nearest_neighbor;

import java.util.Map;

public class TestData {

    public int orientation;
    public Map<String, Double> signalStrengths;

    public TestData(int orientation, Map<String, Double> signalStrengths) {
        this.orientation = orientation;
        this.signalStrengths = signalStrengths;
    }

}
