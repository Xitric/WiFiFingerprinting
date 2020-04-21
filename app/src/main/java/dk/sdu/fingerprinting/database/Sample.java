package dk.sdu.fingerprinting.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

@Entity(primaryKeys = {"timestamp", "ap_mac"})
public class Sample {

    public long timestamp;

    @NonNull
    @ColumnInfo(name = "ap_mac")
    public String apMac;

    @ColumnInfo(name = "strength")
    public int signalStrength;

    @ColumnInfo(name = "location")
    public String locationLabel;

    public int orientation;

    public Sample(long timestamp, @NonNull String apMac, int signalStrength, String locationLabel, int orientation) {
        this.timestamp = timestamp;
        this.apMac = apMac;
        this.signalStrength = signalStrength;
        this.locationLabel = locationLabel;
        this.orientation = orientation;
    }
}
